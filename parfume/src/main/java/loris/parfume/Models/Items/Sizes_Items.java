package loris.parfume.Models.Items;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.*;
import loris.parfume.Configurations.Serializers.DoubleSerializer;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@IdClass(Sizes_Items_Ids.class)
@Table(name = "sizes_items")
public class Sizes_Items {

    @Id
    @ManyToOne
    @JoinColumn(name = "size_id")
    private Sizes size;

    @Id
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Items item;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double price;

    private Integer quantity;
    private Integer discountPercent;
}