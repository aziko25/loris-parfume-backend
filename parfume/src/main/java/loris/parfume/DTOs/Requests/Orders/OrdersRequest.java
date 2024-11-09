package loris.parfume.DTOs.Requests.Orders;

import lombok.Getter;
import lombok.Setter;
import loris.parfume.Models.Orders.OrdersStatuses;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrdersRequest {

    private String address;
    private String addressLocationLink;
    private Double distance;
    private String fullName;
    private String phone;
    private String comment;
    private Boolean isDelivery;
    private Boolean isSoonDeliveryTime;
    private LocalDateTime scheduledDeliveryTime;

    private Double longitude;
    private Double latitude;
    private String city;

    private Double deliverySum;
    private Double totalSum;
    private String paymentType;

    private String returnUrl;

    private String promocode;

    private Boolean isOrderDelivered;

    private List<Orders_Items_Request> ordersItemsList;

    private OrdersStatuses status;
}