package loris.parfume.DTOs.returnDTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import loris.parfume.Configurations.Serializers.DoubleSerializer;
import loris.parfume.Models.Orders.Orders;
import loris.parfume.Models.Orders.Orders_Items;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
public class OrdersDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdTime;

    private Long userId;
    private String userFullName;
    private String phone;
    private String address;
    private String addressLocationLink;
    private Double distance;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double sumForDelivery;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double totalSum;

    private String comments;
    private Boolean isDelivered;
    private Boolean isDelivery;

    private Boolean isSoonDeliveryTime;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime scheduledDeliveryTime;

    private Long branchId;
    private String branchName;
    private Double branchLongitude;
    private Double branchLatitude;
    private String branchRedirectTo;

    private String paymentLink;
    private Boolean isPaid;

    private String paymentResponseUz;
    private String paymentResponseRu;
    private String paymentResponseEng;

    private List<Orders_Items_DTO> itemsList;

    public OrdersDTO(Orders order) {

        id = order.getId();
        createdTime = order.getCreatedTime();

        userId = order.getUser().getId();
        userFullName = order.getUser().getFullName();
        phone = order.getUser().getPhone();
        address = order.getAddress();
        addressLocationLink = order.getAddressLocationLink();
        distance = order.getDistance();

        sumForDelivery = order.getSumForDelivery();
        totalSum = order.getTotalSum();
        comments = order.getComments();
        isDelivered = order.getIsDelivered();
        isDelivery = order.getIsDelivery();
        isSoonDeliveryTime = order.getIsSoonDeliveryTime();
        scheduledDeliveryTime = order.getScheduledDeliveryTime();

        paymentLink = order.getPaymentLink();
        isPaid = order.getIsPaid();

        paymentResponseUz = order.getPaymentResponseUz();
        paymentResponseRu = order.getPaymentResponseRu();
        paymentResponseEng = order.getPaymentResponseEng();

        if (order.getBranch() != null) {

            branchId = order.getBranch().getId();
            branchName = order.getBranch().getName();
            branchLongitude = order.getBranch().getLongitude();
            branchLatitude = order.getBranch().getLatitude();
            branchRedirectTo = order.getBranch().getRedirectTo();
        }

        itemsList = new ArrayList<>();
        if (order.getItemsList() != null && !order.getItemsList().isEmpty()) {
            for (Orders_Items ordersItems : order.getItemsList()) {
                itemsList.add(new Orders_Items_DTO(ordersItems));
            }
        }
    }
}