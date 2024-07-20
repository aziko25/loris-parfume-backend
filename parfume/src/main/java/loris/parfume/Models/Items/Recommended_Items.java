package loris.parfume.Models.Items;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@IdClass(Recommended_Items_Ids.class)
@Table(name = "recommended_items")
public class Recommended_Items {

    @Id
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Items item;

    @Id
    @ManyToOne
    @JoinColumn(name = "recommended_item_id")
    private Items recommendedItem;
}