package loris.parfume.Models.Items;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "items",
        indexes = @Index(name = "idx_slug_items", columnList = "slug")
)
public class Items implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(unique = true, nullable = false)
    private String barcode;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdTime;

    private String nameUz;
    private String nameRu;
    private String nameEng;

    private String descriptionUz;
    private String descriptionRu;
    private String descriptionEng;

    private Double price;
    private Integer discountPercent;
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Categories category;

    @OneToMany(mappedBy = "item")
    private List<Items_Images> itemsImagesList;

    @OneToMany(mappedBy = "item")
    private List<Collections_Items> collectionsItemsList;

    @OneToMany(mappedBy = "item")
    private List<Sizes_Items> sizesItemsList;
}