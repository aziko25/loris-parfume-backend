package loris.parfume.Models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.*;
import loris.parfume.Configurations.Serializers.DoubleSerializer;
import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Items.Sizes;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@IdClass(Basket_Ids.class)
@Table(name = "basket")
public class Basket {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @Id
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Items item;

    @Id
    @ManyToOne
    @JoinColumn(name = "size_id")
    private Sizes size;

    private Integer quantity;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double price;

    private Integer discountPercent;
}