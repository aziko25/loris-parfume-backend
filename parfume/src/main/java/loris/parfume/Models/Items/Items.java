package loris.parfume.Models.Items;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

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

    @Field(type = FieldType.Text)
    @Column(unique = true, nullable = false)
    private String slug;

    @Field(type = FieldType.Text)
    @Column(unique = true)
    private String barcode;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdTime;

    @Field(type = FieldType.Text)
    private String nameUz;

    @Field(type = FieldType.Text)
    private String nameRu;

    @Field(type = FieldType.Text)
    private String nameEng;

    @Column(length = 64000)
    @Field(type = FieldType.Text)
    private String descriptionUz;

    @Column(length = 64000)
    @Field(type = FieldType.Text)
    private String descriptionRu;

    private String descriptionEng;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Integer)
    private Integer discountPercent;

    private Boolean isRecommendedInMainPage;
    private Boolean isActive;

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