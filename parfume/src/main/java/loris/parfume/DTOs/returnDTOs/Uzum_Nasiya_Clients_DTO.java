package loris.parfume.DTOs.returnDTOs;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import loris.parfume.Configurations.Serializers.DoubleSerializer;
import loris.parfume.Models.Orders.Orders_Items;
import loris.parfume.Models.Orders.Uzum_Nasiya_Clients;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Uzum_Nasiya_Clients_DTO {

    private Long id;
    private String phone;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double sum;

    private Boolean isApproved;

    private Long userId;
    private String fullName;

    private Long orderId;
    private List<Orders_Items> ordersItemsList;

    public Uzum_Nasiya_Clients_DTO(Uzum_Nasiya_Clients nasiya) {

        id = nasiya.getId();
        phone = nasiya.getPhone();
        sum = nasiya.getSum();
        isApproved = nasiya.getIsApproved();

        if (nasiya.getUser() != null) {

            userId = nasiya.getUser().getId();
            fullName = nasiya.getUser().getFullName();
        }

        ordersItemsList = new ArrayList<>();
        if (nasiya.getOrder() != null) {

            orderId = nasiya.getOrder().getId();
            ordersItemsList = nasiya.getOrder().getItemsList();
        }
    }
}