package loris.parfume.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.*;
import loris.parfume.Configurations.Serializers.DoubleSerializer;
import loris.parfume.Models.Items.Collections;
import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Items.Sizes;

import java.time.LocalDateTime;

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

    @Id
    @ManyToOne
    @JoinColumn(name = "size_id")
    private Sizes size;

    @ManyToOne
    @JoinColumn(name = "collection_id")
    private Collections collection;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime addedTime;
}