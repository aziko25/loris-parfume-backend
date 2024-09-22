package loris.parfume.DTOs.Requests.Orders;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PromocodeRequest {

    private String code;

    private Boolean isEndlessQuantity;
    private Integer activationQuantity;

    private Boolean isForever;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Boolean isUserActivationOnce;
    private Integer userActivationQuantity;

    private Integer discountPercent;
    private Double discountSum;

    private Boolean isActive;
}