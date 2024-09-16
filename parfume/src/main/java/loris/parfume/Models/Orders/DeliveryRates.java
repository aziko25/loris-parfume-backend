package loris.parfume.Models.Orders;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "delivery_rates")
public class DeliveryRates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdTime;

    private String name;

    private Boolean isFixed;

    private Double sumPerKm;
    private Integer firstFreeKmQuantity;
    private Double afterFreeKmSumPerKm;

    private Integer firstPaidKmQuantity;
    private Double firstPaidKmQuantityPrice;
    private Double afterPaidKmSumPerKm;

    private Boolean isActive;
    private Boolean isDefault;
}