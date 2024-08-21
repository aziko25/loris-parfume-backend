package loris.parfume.DTOs.returnDTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import loris.parfume.Configurations.Serializers.DoubleSerializer;
import loris.parfume.Models.Branches;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BranchesDTO {

    private Long id;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdTime;

    private String name;
    private String phone;

    private Double longitude;
    private Double latitude;

    private String redirectTo;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double deliverySum;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double distance;

    public BranchesDTO(Branches branch, Double deliverySum, Double distance) {

        this.id = branch.getId();
        this.createdTime = branch.getCreatedTime();
        this.name = branch.getName();
        this.phone = branch.getPhone();
        this.longitude = branch.getLongitude();
        this.latitude = branch.getLatitude();
        this.redirectTo = branch.getRedirectTo();
        this.deliverySum = deliverySum;
        this.distance = distance;
    }
}