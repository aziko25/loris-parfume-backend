package loris.parfume.Services.Orders;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.Telegram.MainTelegramBot;
import loris.parfume.Controllers.Orders.WebSocketController;
import loris.parfume.DTOs.Requests.Orders.OrdersRequest;
import loris.parfume.DTOs.Requests.Orders.Orders_Items_Request;
import loris.parfume.DTOs.returnDTOs.OrdersDTO;
import loris.parfume.Models.Items.*;
import loris.parfume.Models.Orders.*;
import loris.parfume.Models.Users;
import loris.parfume.Repositories.Items.*;
import loris.parfume.Repositories.Orders.*;
import loris.parfume.Repositories.UsersRepository;
import loris.parfume.SMS_Eskiz.EskizService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

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
    //private final BranchesRepository branchesRepository;
    private final WebSocketController webSocketController;
    //private final BranchesService branchesService;
    private final Uzum_Nasiya_Clients_Repository uzumNasiyaClientsRepository;
    private final PromocodesService promocodesService;
    private final MainTelegramBot mainTelegramBot;
    private final EskizService eskizService;

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

    private static final String[] paymentTypesList = {"CLICK", "PAYME", "CASH"};

    @Transactional
    @CacheEvict(value = "ordersCache", allEntries = true)
    public OrdersDTO create(OrdersRequest ordersRequest) {

        Users user = usersRepository.findById(USER_ID)
                    .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        if (ordersRequest.getFullName() != null && !ordersRequest.getFullName().isEmpty()) {

            user.setFullName(ordersRequest.getFullName());

            usersRepository.save(user);
        }

        /*Branches branch = branchesRepository.findById(ordersRequest.getBranchId())
                .orElseThrow(() -> new EntityNotFoundException("Branch Not Found"));

        NearestBranchRequest nearestBranchRequest =
                new NearestBranchRequest(ordersRequest.getLongitude(), ordersRequest.getLatitude(), ordersRequest.getCity());

        BigDecimal calculatedSum = BigDecimal.valueOf(branchesService.calculateDeliverySum(nearestBranchRequest, branch, null, ordersRequest.getCity())).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedSum = BigDecimal.valueOf(ordersRequest.getDeliverySum()).setScale(2, RoundingMode.HALF_UP);

        if (calculatedSum.compareTo(expectedSum) != 0 && ordersRequest.getTotalSum() < 500000.00) {

            throw new IllegalArgumentException("Delivery Sum Is Incorrect. It Should Be " +
                    calculatedSum + " Instead Of " + expectedSum);
        }*/

        double deliverySum;
        if (ordersRequest.getCity().equalsIgnoreCase("tashkent")
                || ordersRequest.getCity().equalsIgnoreCase("toshkent")
                || ordersRequest.getCity().equalsIgnoreCase("ташкент")) {

            deliverySum =  20000.0;
        }
        else {

            deliverySum = 30000.0;
        }

        if (!Arrays.asList(paymentTypesList).contains(ordersRequest.getPaymentType().toUpperCase())) {

            throw new IllegalArgumentException("Invalid payment type: " + ordersRequest.getPaymentType());
        }

        Orders order = Orders.builder()
                .createdTime(LocalDateTime.now())
                .address(ordersRequest.getAddress())
                .city(ordersRequest.getCity())
                .addressLocationLink(ordersRequest.getAddressLocationLink() + "&z=19")
                .distance(ordersRequest.getDistance())
                .fullName(ordersRequest.getFullName())
                .phone(ordersRequest.getPhone())
                .comments(ordersRequest.getComment())
                .sumForDelivery(deliverySum)
                .isDelivered(false)
                .isDelivery(ordersRequest.getIsDelivery())
                .isSoonDeliveryTime(ordersRequest.getIsSoonDeliveryTime())
                .scheduledDeliveryTime(ordersRequest.getScheduledDeliveryTime())
                .isOrderDelivered(false)
                .status(OrdersStatuses.NEW)
                .user(user)
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
            Boolean isFiftyPercentSaleApplied = collectionsItem.getCollection().getIsFiftyPercentSaleApplied();

            if (!collectionsItem.getItem().getSizesItemsList().isEmpty() && ordersItemsRequest.getSizeId() == null) {

                throw new EntityNotFoundException("Select Item's Size!");
            }

            Sizes size;

            if (ordersItemsRequest.getSizeId() != null && ordersItemsRequest.getSizeId() != 1) {

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

                if (Boolean.TRUE.equals(isFiftyPercentSaleApplied)) {

                    if (currentCount % 2 == 0) {
                        totalItemPrice += itemPrice;
                    }
                    else {
                        totalItemPrice += itemPrice * 0.5;
                    }
                }
                else {
                    totalItemPrice += itemPrice;  // No sale if isFiftyPercentSaleAvailable is false or null
                }

                currentCount++;
                remainingQuantity--;
            }

            Orders_Items ordersItem = new Orders_Items(order, collectionsItem.getItem(), size, collectionsItem.getCollection(), totalItemPrice, ordersItemsRequest.getQuantity());

            totalSum += totalItemPrice;
            saveAllOrderItemsList.add(ordersItem);
            collectionItemCountMap.put(collectionsItem.getCollection().getId(), currentCount);
        }

        if (ordersRequest.getPromocode() != null && !ordersRequest.getPromocode().isEmpty()) {

            Promocodes promocode = promocodesService.getByCode(ordersRequest.getPromocode());

            if (promocode.getDiscountSum() != null) {

                totalSum = totalSum - promocode.getDiscountSum();

                if (totalSum < 0) {
                    totalSum = 0;
                }
            }

            if (promocode.getDiscountPercent() != null) {

                totalSum = totalSum - (totalSum * promocode.getDiscountPercent() / 100);
            }
        }

        if (totalSum >= 500000) {

            order.setTotalSum(totalSum);
            order.setSumForDelivery(0.0);
        }
        else {

            order.setTotalSum(totalSum + deliverySum);
            order.setSumForDelivery(deliverySum);
            totalSum += deliverySum;
        }

        if (ordersRequest.getTotalSum() != totalSum) {

            throw new IllegalArgumentException("Total Sum Should Be " + totalSum + ", Not " + ordersRequest.getTotalSum());
        }

        ordersItemsRepository.saveAll(saveAllOrderItemsList);
        order.setItemsList(saveAllOrderItemsList);

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

            eskizService.sendOrderCreatedSms(ordersRequest.getPhone(), order.getId());
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
    @CacheEvict(value = "ordersCache", allEntries = true)
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
        Optional.ofNullable(ordersRequest.getIsOrderDelivered()).ifPresent(order::setIsOrderDelivered);

        if (ordersRequest.getStatus() != null) {

            if (ordersRequest.getStatus().equals(OrdersStatuses.ON_THE_WAY)) {

                if (order.getCity().equalsIgnoreCase("tashkent") ||
                    order.getCity().equalsIgnoreCase("toshkent") ||
                    order.getCity().equalsIgnoreCase("ташкент")) {

                    eskizService.sendOrderIsOnTheWaySms(order.getPhone(), "Tez Orada", order.getId());
                }
                else {

                    eskizService.sendOrderIsOnTheWaySms(order.getPhone(), "3 Kun Ichida", order.getId());
                }
            }

            order.setStatus(ordersRequest.getStatus());
        }

        if (ordersRequest.getOrdersItemsList() != null && !ordersRequest.getOrdersItemsList().isEmpty()) {

            List<Orders_Items> saveAllOrderItemsList = new ArrayList<>();
            double totalSum = 0.0;

            Map<Long, Integer> collectionItemCountMap = new HashMap<>();

            ordersItemsRepository.deleteAllByOrder(order);
            order.setItemsList(null);

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