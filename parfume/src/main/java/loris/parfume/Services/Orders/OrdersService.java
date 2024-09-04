package loris.parfume.Services.Orders;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.Telegram.MainTelegramBot;
import loris.parfume.Controllers.Orders.WebSocketController;
import loris.parfume.DTOs.Requests.NearestBranchRequest;
import loris.parfume.DTOs.Requests.Orders.OrdersRequest;
import loris.parfume.DTOs.Requests.Orders.Orders_Items_Request;
import loris.parfume.DTOs.returnDTOs.OrdersDTO;
import loris.parfume.Models.Branches;
import loris.parfume.Models.Items.*;
import loris.parfume.Models.Orders.Orders;
import loris.parfume.Models.Orders.Orders_Items;
import loris.parfume.Models.Orders.Uzum_Nasiya_Clients;
import loris.parfume.Models.Users;
import loris.parfume.Repositories.BasketsRepository;
import loris.parfume.Repositories.BranchesRepository;
import loris.parfume.Repositories.Items.*;
import loris.parfume.Repositories.Orders.OrdersRepository;
import loris.parfume.Repositories.Orders.Orders_Items_Repository;
import loris.parfume.Repositories.Orders.Uzum_Nasiya_Clients_Repository;
import loris.parfume.Repositories.UsersRepository;
import loris.parfume.Services.BranchesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

import static loris.parfume.Configurations.JWT.AuthorizationMethods.USER_ID;
import static loris.parfume.Controllers.Orders.ClickOrdersController.orderDetailsMessage;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;

    private final Collections_Items_Repository collectionsItemsRepository;
    private final Orders_Items_Repository ordersItemsRepository;
    private final UsersRepository usersRepository;
    private final SizesRepository sizesRepository;
    private final Sizes_Items_Repository sizesItemsRepository;
    private final BranchesRepository branchesRepository;
    private final WebSocketController webSocketController;
    private final BasketsRepository basketsRepository;
    private final BranchesService branchesService;
    private final Uzum_Nasiya_Clients_Repository uzumNasiyaClientsRepository;
    private final MainTelegramBot mainTelegramBot;

    @Value("${pageSize}")
    private Integer pageSize;

    @Value("${clickMerchantId}")
    private Integer clickMerchantId;

    @Value("${clickServiceId}")
    private Integer clickServiceId;

    @Value("${paymeBusinessId}")
    private String paymeBusinessId;

    @Value("${payment.chat.id}")
    private String paymentChatId;

    private static final String[] paymentTypesList = {"CLICK", "PAYME", "CASH", "UZUM", "UZUM NASIYA"};

    @Transactional
    @CacheEvict(value = "ordersCache", allEntries = true)
    public OrdersDTO create(OrdersRequest ordersRequest) {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Branches branch = branchesRepository.findById(ordersRequest.getBranchId())
                .orElseThrow(() -> new EntityNotFoundException("Branch Not Found"));

        NearestBranchRequest nearestBranchRequest =
                new NearestBranchRequest(ordersRequest.getLongitude(), ordersRequest.getLatitude());

        BigDecimal calculatedSum = BigDecimal.valueOf(branchesService.calculateDeliverySum(nearestBranchRequest, branch)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedSum = BigDecimal.valueOf(ordersRequest.getDeliverySum()).setScale(2, RoundingMode.HALF_UP);

        if (calculatedSum.compareTo(expectedSum) != 0) {

            throw new IllegalArgumentException("Delivery Sum Is Incorrect. It Should Be " +
                    calculatedSum + " Instead Of " + expectedSum);
        }

        if (!Arrays.asList(paymentTypesList).contains(ordersRequest.getPaymentType().toUpperCase())) {

            throw new IllegalArgumentException("Invalid payment type: " + ordersRequest.getPaymentType());
        }

        Orders order = Orders.builder()
                .createdTime(LocalDateTime.now())
                .address(ordersRequest.getAddress())
                .addressLocationLink(ordersRequest.getAddressLocationLink())
                .distance(ordersRequest.getDistance())
                .fullName(ordersRequest.getFullName())
                .phone(ordersRequest.getPhone())
                .comments(ordersRequest.getComment())
                .sumForDelivery(ordersRequest.getDeliverySum())
                .isDelivered(false)
                .isDelivery(ordersRequest.getIsDelivery())
                .isSoonDeliveryTime(ordersRequest.getIsSoonDeliveryTime())
                .scheduledDeliveryTime(ordersRequest.getScheduledDeliveryTime())
                .user(user)
                .branch(branch)
                .build();

        ordersRepository.save(order);

        List<Orders_Items> saveAllOrderItemsList = new ArrayList<>();
        double totalSum = 0.0;

        Map<Long, Integer> collectionItemCountMap = new HashMap<>();

        for (Orders_Items_Request ordersItemsRequest : ordersRequest.getOrdersItemsList()) {

            Collections_Items collectionsItem = collectionsItemsRepository
                    .findByCollectionIdAndItemId(ordersItemsRequest.getCollectionId(), ordersItemsRequest.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("Item ID: " + ordersItemsRequest.getItemId() + " Does Not Belong To This Collection!"));

            double itemPrice = collectionsItem.getItem().getPrice();
            Integer discountPercent = collectionsItem.getItem().getDiscountPercent();

            if (!collectionsItem.getItem().getSizesItemsList().isEmpty() && ordersItemsRequest.getSizeId() == null) {
                throw new IllegalArgumentException("Specify Item's Size!");
            }

            Sizes size;

            if (ordersItemsRequest.getSizeId() != null) {

                size = sizesRepository.findById(ordersItemsRequest.getSizeId())
                        .orElseThrow(() -> new EntityNotFoundException("Size " + ordersItemsRequest.getSizeId() + " Not Found"));

                Sizes_Items sizesItem = sizesItemsRepository.findByItemAndSize(collectionsItem.getItem(), size);

                if (sizesItem == null) {
                    throw new EntityNotFoundException("Size " + ordersItemsRequest.getSizeId() +
                            " For Item " + collectionsItem.getItem().getId() + " Not Found");
                }

                itemPrice = sizesItem.getPrice();
                discountPercent = sizesItem.getDiscountPercent();
            }
            else {

                size = sizesRepository.findByIsDefaultNoSize(true);
            }

            if (discountPercent != null && discountPercent != 0) {

                itemPrice = itemPrice * (1 - discountPercent / 100.0);
            }

            collectionItemCountMap.putIfAbsent(collectionsItem.getCollection().getId(), 0);
            int currentCount = collectionItemCountMap.get(collectionsItem.getCollection().getId());

            double totalItemPrice = 0.0;
            int remainingQuantity = ordersItemsRequest.getQuantity();

            while (remainingQuantity > 0) {

                if (currentCount % 2 == 0) {

                    totalItemPrice += itemPrice;
                }
                else {

                    totalItemPrice += itemPrice * 0.5;
                }

                currentCount++;
                remainingQuantity--;
            }

            Orders_Items ordersItem = new Orders_Items(order, collectionsItem.getItem(), size, collectionsItem.getCollection(), totalItemPrice, ordersItemsRequest.getQuantity());

            totalSum += totalItemPrice;
            saveAllOrderItemsList.add(ordersItem);
            collectionItemCountMap.put(collectionsItem.getCollection().getId(), currentCount);
        }

        if (totalSum >= 500000) {

            order.setTotalSum(totalSum);
        }
        else {

            order.setTotalSum(totalSum + ordersRequest.getDeliverySum());
            order.setSumForDelivery(0.0);
        }

        if (ordersRequest.getTotalSum() != totalSum) {

            throw new IllegalArgumentException("Total Sum Should Be " + totalSum + ", Not " + ordersRequest.getTotalSum());
        }

        ordersItemsRepository.saveAll(saveAllOrderItemsList);

        order.setItemsList(saveAllOrderItemsList);

        basketsRepository.deleteAllByUser(user);
        ordersRepository.save(order);

        if (ordersRequest.getPaymentType().equalsIgnoreCase("CLICK")) {

            order.setPaymentLink("https://my.click.uz/services/pay?service_id=" + clickServiceId + "&merchant_id=" + clickMerchantId +
                    "&return_url=" + ordersRequest.getReturnUrl() +
                    "&amount=" + order.getTotalSum() +
                    "&transaction_param=" + order.getId());
            order.setIsPaid(false);
            order.setPaymentType("CLICK");
        }
        else if (ordersRequest.getPaymentType().equalsIgnoreCase("PAYME")) {

            order.setIsPaid(false);
            order.setPaymentType("PAYME");

            String paymeUrl = "https://checkout.paycom.uz";
            long amount = (long) (order.getTotalSum() * 100); // тиины

            String orderId = order.getId().toString();

            String data = "m=" + paymeBusinessId + ";ac.orderId=" + orderId + ";a=" + amount + ";c=" + ordersRequest.getReturnUrl();
            String encodedData = Base64.getEncoder().encodeToString(data.getBytes());

            String url = paymeUrl + "/" + encodedData;

            order.setPaymentLink(url);
        }
        else if (ordersRequest.getPaymentType().equalsIgnoreCase("CASH")) {

            order.setPaymentLink("CASH");
            order.setIsPaid(false);
            order.setPaymentType("CASH");

            SendMessage message = new SendMessage();
            message.setChatId(paymentChatId);
            message.setText(orderDetailsMessage(order, "CASH"));
            mainTelegramBot.sendMessage(message);
        }
        else if (ordersRequest.getPaymentType().equalsIgnoreCase("UZUM NASIYA")) {

            order.setPaymentLink("UZUM NASIYA");
            order.setIsPaid(false);
            order.setPaymentType("UZUM NASIYA");

            Uzum_Nasiya_Clients uzumNasiyaClient = Uzum_Nasiya_Clients.builder()
                    .phone(ordersRequest.getPhone())
                    .user(user)
                    .order(order)
                    .sum(order.getTotalSum())
                    .build();

            uzumNasiyaClientsRepository.save(uzumNasiyaClient);

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(paymentChatId);
            sendMessage.setText("UZUM NASIYA\n----------------\nOrder ID: " + order.getId() +
                    "\nPhone: " + order.getPhone() + "\nFull Name: " + user.getFullName() +
                    "\nOrder Sum: " + order.getTotalSum());

            InlineKeyboardButton confirmButton = new InlineKeyboardButton();
            confirmButton.setText("Подтвердить");
            confirmButton.setCallbackData("confirm_" + order.getId());

            InlineKeyboardButton rejectButton = new InlineKeyboardButton();
            rejectButton.setText("Отказать");
            rejectButton.setCallbackData("reject_" + order.getId());

            List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
            keyboardButtonsRow1.add(confirmButton);
            keyboardButtonsRow1.add(rejectButton);

            List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
            keyboardRows.add(keyboardButtonsRow1);

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(keyboardRows);
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);

            mainTelegramBot.sendMessage(sendMessage);
        }

        ordersRepository.save(order);

        OrdersDTO orderDTO = new OrdersDTO(order);

        webSocketController.sendOrderUpdate(orderDTO);

        return orderDTO;
    }

    @Cacheable(
            value = "ordersCache",
            key = "T(String).valueOf('page-').concat(T(String).valueOf(#page))"
    )
    public Page<OrdersDTO> all(Integer page) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdTime").descending());

        return ordersRepository.findAll(pageable).map(OrdersDTO::new);
    }

    public OrdersDTO getById(Long id) {

        return ordersRepository.findById(id).map(OrdersDTO::new).orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    @Transactional
    public OrdersDTO update(Long id, OrdersRequest ordersRequest) {

        Orders order = ordersRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Order Not Found"));

        Optional.ofNullable(ordersRequest.getAddress()).ifPresent(order::setAddress);
        Optional.ofNullable(ordersRequest.getAddressLocationLink()).ifPresent(order::setAddressLocationLink);
        Optional.ofNullable(ordersRequest.getDistance()).ifPresent(order::setDistance);
        Optional.ofNullable(ordersRequest.getFullName()).ifPresent(order::setFullName);
        Optional.ofNullable(ordersRequest.getPhone()).ifPresent(order::setPhone);
        Optional.ofNullable(ordersRequest.getComment()).ifPresent(order::setComments);
        Optional.ofNullable(ordersRequest.getIsDelivery()).ifPresent(order::setIsDelivery);
        Optional.ofNullable(ordersRequest.getIsSoonDeliveryTime()).ifPresent(order::setIsSoonDeliveryTime);
        Optional.ofNullable(ordersRequest.getScheduledDeliveryTime()).ifPresent(order::setScheduledDeliveryTime);
        Optional.ofNullable(ordersRequest.getDeliverySum()).ifPresent(order::setSumForDelivery);
        Optional.ofNullable(ordersRequest.getTotalSum()).ifPresent(order::setTotalSum);
        Optional.ofNullable(ordersRequest.getPaymentType()).ifPresent(order::setPaymentType);

        if (ordersRequest.getBranchId() != null) {

            Branches branch = branchesRepository.findById(ordersRequest.getBranchId()).orElseThrow(() -> new EntityNotFoundException("Branch Not Found"));
            order.setBranch(branch);
        }

        if (ordersRequest.getOrdersItemsList() != null && !ordersRequest.getOrdersItemsList().isEmpty()) {

            List<Orders_Items> saveAllOrderItemsList = new ArrayList<>();
            double totalSum = 0.0;

            Map<Long, Integer> collectionItemCountMap = new HashMap<>();

            ordersItemsRepository.deleteAllByOrder(order);

            for (Orders_Items_Request ordersItemsRequest : ordersRequest.getOrdersItemsList()) {

                Collections_Items collectionsItem = collectionsItemsRepository
                        .findByCollectionIdAndItemId(ordersItemsRequest.getCollectionId(), ordersItemsRequest.getItemId())
                        .orElseThrow(() -> new EntityNotFoundException("Item ID: " + ordersItemsRequest.getItemId() + " Does Not Belong To This Collection!"));

                double itemPrice = collectionsItem.getItem().getPrice();
                Integer discountPercent = collectionsItem.getItem().getDiscountPercent();

                if (!collectionsItem.getItem().getSizesItemsList().isEmpty() && ordersItemsRequest.getSizeId() == null) {
                    throw new IllegalArgumentException("Specify Item's Size!");
                }

                Sizes size;

                if (ordersItemsRequest.getSizeId() != null) {

                    size = sizesRepository.findById(ordersItemsRequest.getSizeId())
                            .orElseThrow(() -> new EntityNotFoundException("Size " + ordersItemsRequest.getSizeId() + " Not Found"));

                    Sizes_Items sizesItem = sizesItemsRepository.findByItemAndSize(collectionsItem.getItem(), size);

                    if (sizesItem == null) {
                        throw new EntityNotFoundException("Size " + ordersItemsRequest.getSizeId() +
                                " For Item " + collectionsItem.getItem().getId() + " Not Found");
                    }

                    itemPrice = sizesItem.getPrice();
                    discountPercent = sizesItem.getDiscountPercent();
                }
                else {

                    size = sizesRepository.findByIsDefaultNoSize(true);
                }

                if (discountPercent != null && discountPercent != 0) {

                    itemPrice = itemPrice * (1 - discountPercent / 100.0);
                }

                collectionItemCountMap.putIfAbsent(collectionsItem.getCollection().getId(), 0);
                int currentCount = collectionItemCountMap.get(collectionsItem.getCollection().getId());

                double totalItemPrice = 0.0;
                int remainingQuantity = ordersItemsRequest.getQuantity();

                while (remainingQuantity > 0) {

                    if (currentCount % 2 == 0) {

                        totalItemPrice += itemPrice;
                    }
                    else {

                        totalItemPrice += itemPrice * 0.5;
                    }

                    currentCount++;
                    remainingQuantity--;
                }

                Orders_Items ordersItem = new Orders_Items(order, collectionsItem.getItem(), size, collectionsItem.getCollection(), totalItemPrice, ordersItemsRequest.getQuantity());

                totalSum += totalItemPrice;
                saveAllOrderItemsList.add(ordersItem);
                collectionItemCountMap.put(collectionsItem.getCollection().getId(), currentCount);
            }

            ordersItemsRepository.saveAll(saveAllOrderItemsList);
            order.setItemsList(saveAllOrderItemsList);

            if (totalSum >= 500000) {

                order.setTotalSum(totalSum);
            }
            else {

                order.setTotalSum(totalSum + ordersRequest.getDeliverySum());
                order.setSumForDelivery(0.0);
            }
        }

        return new OrdersDTO(ordersRepository.save(order));
    }

    public Page<OrdersDTO> myOrders(Integer page) {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdTime").descending());

        return ordersRepository.findAllByUser(user, pageable).map(OrdersDTO::new);
    }

    public OrdersDTO myOrdersById(Long id) {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        return ordersRepository.findByIdAndUser(id, user).map(OrdersDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Order Not Found"));
    }

    @CacheEvict(value = "ordersCache", allEntries = true)
    public String delete(Long id) {

        Orders order = ordersRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Order Not Found"));

        Uzum_Nasiya_Clients uzumNasiyaClient = uzumNasiyaClientsRepository.findByOrder(order)
                .orElse(null);

        if (uzumNasiyaClient != null) {

            uzumNasiyaClient.setOrder(null);
            uzumNasiyaClientsRepository.save(uzumNasiyaClient);
        }

        ordersItemsRepository.deleteAllByOrder(order);

        ordersRepository.delete(order);

        return "Order Successfully Deleted";
    }
}