package loris.parfume.Models;

import jakarta.persistence.*;
import lombok.*;
import loris.parfume.Models.Items.Items;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@IdClass(Wishlist_Ids.class)
@Table(name = "wishlist")
public class Wishlist {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @Id
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Items item;
}