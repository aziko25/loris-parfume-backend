package loris.parfume.DTOs.Requests.Orders;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryRatesRequest {

    private String name;
    private Boolean isFixed;
    private Double sumPerKm;
    private Integer firstFreeKmQuantity;
    private Double afterFreeKmSumPerKm;
    private Boolean isActive;
}