package loris.parfume.DTOs.Requests.Orders;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrdersRequest {

    private String address;
    private String addressLocationLink;
    private Double distance;
    private String phone;
    private String comment;
    private Boolean isDelivery;
    private Boolean isSoonDeliveryTime;
    private LocalDateTime scheduledDeliveryTime;

    private Double longitude;
    private Double latitude;

    private Double deliverySum;
    private Double totalSum;
    private String paymentType;

    private Long branchId;

    private String returnUrl;

    private List<Orders_Items_Request> ordersItemsList;
}