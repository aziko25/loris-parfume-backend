package loris.parfume.Models.Items;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@IdClass(Collections_Items_Ids.class)
@Table(name = "collections_items")
public class Collections_Items {

    @Id
    @ManyToOne
    @JoinColumn(name = "collection_id")
    private Collections collection;

    @Id
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Items item;
}