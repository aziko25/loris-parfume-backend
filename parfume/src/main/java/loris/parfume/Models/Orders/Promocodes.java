package loris.parfume.Models.Orders;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.*;
import loris.parfume.Configurations.Serializers.DoubleSerializer;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "promocodes")
public class Promocodes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdTime;

    private Boolean isEndlessQuantity;
    private Integer activationQuantity;

    private Boolean isForever;
    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime endTime;

    private Boolean isUserActivationOnce;
    private Integer userActivationQuantity;

    private Integer discountPercent;
    @JsonSerialize(using = DoubleSerializer.class)
    private Double discountSum;

    private Boolean isActive;

    private Integer activatedQuantity;

    private Boolean isDeleted;
}