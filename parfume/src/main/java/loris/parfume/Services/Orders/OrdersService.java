package loris.parfume.Services.Orders;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import loris.parfume.Controllers.Orders.WebSocketController;
import loris.parfume.DTOs.Requests.Orders.OrdersRequest;
import loris.parfume.DTOs.Requests.Orders.Orders_Items_Request;
import loris.parfume.DTOs.returnDTOs.OrdersDTO;
import loris.parfume.Models.Branches;
import loris.parfume.Models.Items.*;
import loris.parfume.Models.Items.Collections;
import loris.parfume.Models.Orders.Orders;
import loris.parfume.Models.Orders.Orders_Items;
import loris.parfume.Models.Users;
import loris.parfume.Repositories.BasketsRepository;
import loris.parfume.Repositories.BranchesRepository;
import loris.parfume.Repositories.Items.CollectionsRepository;
import loris.parfume.Repositories.Items.ItemsRepository;
import loris.parfume.Repositories.Items.SizesRepository;
import loris.parfume.Repositories.Items.Sizes_Items_Repository;
import loris.parfume.Repositories.Orders.OrdersRepository;
import loris.parfume.Repositories.Orders.Orders_Items_Repository;
import loris.parfume.Repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static loris.parfume.Configurations.JWT.AuthorizationMethods.USER_ID;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;

    private final ItemsRepository itemsRepository;
    private final Orders_Items_Repository ordersItemsRepository;
    private final UsersRepository usersRepository;
    private final SizesRepository sizesRepository;
    private final Sizes_Items_Repository sizesItemsRepository;
    private final BranchesRepository branchesRepository;
    private final CollectionsRepository collectionsRepository;
    private final WebSocketController webSocketController;
    private final BasketsRepository basketsRepository;

    @Value("${pageSize}")
    private Integer pageSize;

    @Value("${clickMerchantId}")
    private Integer clickMerchantId;

    @Value("${clickServiceId}")
    private Integer clickServiceId;

    @Value("${paymentReturnUrl}")
    private String paymentReturnUrl;

    private static final String[] paymentTypesList = {"CLICK", "CASH"};

    @Transactional
    public OrdersDTO create(OrdersRequest ordersRequest) {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Branches branch = branchesRepository.findById(ordersRequest.getBranchId())
                .orElseThrow(() -> new EntityNotFoundException("Branch Not Found"));

        if (!Arrays.asList(paymentTypesList).contains(ordersRequest.getPaymentType().toUpperCase())) {

            throw new IllegalArgumentException("Invalid payment type: " + ordersRequest.getPaymentType());
        }

        Orders order = Orders.builder()
                .createdTime(LocalDateTime.now())
                .address(ordersRequest.getAddress())
                .addressLocationLink(ordersRequest.getAddressLocationLink())
                .distance(ordersRequest.getDistance())
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

        for (Orders_Items_Request ordersItemsRequest : ordersRequest.getOrdersItemsList()) {

            Items item = itemsRepository.findById(ordersItemsRequest.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("Item " + ordersItemsRequest.getItemId() + " Not Found"));

            Collections collection = collectionsRepository.findById(ordersItemsRequest.getCollectionId())
                    .orElseThrow(() -> new EntityNotFoundException("Collection Not Found"));

            if (ordersItemsRequest.getQuantity() <= 0 || ordersItemsRequest.getQuantity() > item.getQuantity()) {

                throw new IllegalArgumentException("Invalid quantity for item " + item.getId());
            }

            Sizes size = sizesRepository.findByIsDefaultNoSize(true);
            double itemPrice = item.getPrice();

            if (!item.getSizesItemsList().isEmpty() && ordersItemsRequest.getSizeId() == null) {

                throw new IllegalArgumentException("Specify Item's Size!");
            }

            if (ordersItemsRequest.getSizeId() != null) {

                size = sizesRepository.findById(ordersItemsRequest.getSizeId())
                        .orElseThrow(() -> new EntityNotFoundException("Size " + ordersItemsRequest.getSizeId() + " Not Found"));

                Sizes_Items sizesItem = sizesItemsRepository.findByItemAndSize(item, size);

                if (sizesItem == null) {

                    throw new EntityNotFoundException("Size " + ordersItemsRequest.getSizeId() +
                            " For Item " + item.getId() + " Not Found");
                }

                if (ordersItemsRequest.getQuantity() > sizesItem.getQuantity()) {

                    throw new IllegalArgumentException("Invalid quantity for item " + item.getId() + " and size " + sizesItem.getSize().getId());
                }

                itemPrice = sizesItem.getPrice();
            }

            Orders_Items ordersItem = new Orders_Items(order, item, size, collection,
                    itemPrice * ordersItemsRequest.getQuantity(), ordersItemsRequest.getQuantity());

            totalSum += ordersItem.getTotalPrice();
            saveAllOrderItemsList.add(ordersItem);
        }

        Map<Long, List<Orders_Items>> collectionItemsMap = new HashMap<>();

        for (Orders_Items_Request ordersItemsRequest : ordersRequest.getOrdersItemsList()) {

            Long collectionId = ordersItemsRequest.getCollectionId();

            collectionItemsMap.computeIfAbsent(collectionId, k -> new ArrayList<>()).add(
                    saveAllOrderItemsList.stream()
                            .filter(oi -> oi.getItem().getId().equals(ordersItemsRequest.getItemId()))
                            .findFirst()
                            .orElseThrow(() -> new EntityNotFoundException("Orders_Item not found"))
            );
        }

        for (List<Orders_Items> ordersItems : collectionItemsMap.values()) {

            if (ordersItems.size() > 1) {

                for (Orders_Items ordersItem : ordersItems) {

                    double discountedPrice = ordersItem.getTotalPrice() * 0.75;
                    totalSum -= (ordersItem.getTotalPrice() - discountedPrice);
                    ordersItem.setTotalPrice(discountedPrice);
                }
            }
        }

        order.setTotalSum(totalSum + ordersRequest.getDeliverySum());

        ordersItemsRepository.saveAll(saveAllOrderItemsList);

        order.setItemsList(saveAllOrderItemsList);

        basketsRepository.deleteAllByUser(user);

        if (ordersRequest.getPaymentType().equalsIgnoreCase("CLICK")) {

            order.setPaymentLink("https://my.click.uz/services/pay?service_id=" + clickServiceId + "&merchant_id=" + clickMerchantId +
                    "&return_url=" + paymentReturnUrl +
                    "&amount=" + order.getTotalSum() +
                    "&transaction_param=" + order.getId());
            order.setIsPaid(false);
        }
        else if (ordersRequest.getPaymentType().equalsIgnoreCase("CASH")) {

            order.setPaymentLink("CASH");
            order.setIsPaid(false);
        }

        OrdersDTO orderDTO = new OrdersDTO(order);

        webSocketController.sendOrderUpdate(orderDTO);

        return orderDTO;
    }

    public Page<OrdersDTO> all(Integer page) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdTime").descending());

        return ordersRepository.findAll(pageable).map(OrdersDTO::new);
    }

    public OrdersDTO getById(Long id) {

        return ordersRepository.findById(id).map(OrdersDTO::new).orElseThrow(() -> new EntityNotFoundException("Order not found"));
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

    public String delete(Long id) {

        Orders order = ordersRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Order Not Found"));

        ordersItemsRepository.deleteAllByOrder(order);

        ordersRepository.delete(order);

        return "Order Successfully Deleted";
    }
}