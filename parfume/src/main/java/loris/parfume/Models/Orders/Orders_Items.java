package loris.parfume.Models.Orders;

import jakarta.persistence.*;
import lombok.*;
import loris.parfume.Models.Items.Collections;
import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Items.Sizes;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@IdClass(Orders_Items_Ids.class)
@Table(name = "orders_items")
public class Orders_Items {

    @Id
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Orders order;

    @Id
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Items item;

    @Id
    @ManyToOne
    @JoinColumn(name = "size_id")
    private Sizes size;

    @ManyToOne
    @JoinColumn(name = "collection_id")
    private Collections collection;

    private Double totalPrice;
    private Integer quantity;
}