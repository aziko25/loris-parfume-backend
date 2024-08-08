package loris.parfume.Services.Orders;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import loris.parfume.Controllers.Orders.WebSocketController;
import loris.parfume.DTOs.Requests.NearestBranchRequest;
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
import loris.parfume.Services.BranchesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final BranchesService branchesService;

    @Value("${pageSize}")
    private Integer pageSize;

    @Value("${clickMerchantId}")
    private Integer clickMerchantId;

    @Value("${clickServiceId}")
    private Integer clickServiceId;

    private static final String[] paymentTypesList = {"CLICK", "CASH"};

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

            collectionItemCountMap.putIfAbsent(collection.getId(), 0);
            int currentCount = collectionItemCountMap.get(collection.getId());

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

            Orders_Items ordersItem = new Orders_Items(order, item, size, collection, totalItemPrice, ordersItemsRequest.getQuantity());

            totalSum += totalItemPrice;
            saveAllOrderItemsList.add(ordersItem);
            collectionItemCountMap.put(collection.getId(), currentCount);
        }

        order.setTotalSum(totalSum + ordersRequest.getDeliverySum());

        ordersItemsRepository.saveAll(saveAllOrderItemsList);

        order.setItemsList(saveAllOrderItemsList);

        basketsRepository.deleteAllByUser(user);

        if (ordersRequest.getPaymentType().equalsIgnoreCase("CLICK")) {

            order.setPaymentLink("https://my.click.uz/services/pay?service_id=" + clickServiceId + "&merchant_id=" + clickMerchantId +
                    "&return_url=" + ordersRequest.getReturnUrl() +
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

        ordersItemsRepository.deleteAllByOrder(order);

        ordersRepository.delete(order);

        return "Order Successfully Deleted";
    }
}