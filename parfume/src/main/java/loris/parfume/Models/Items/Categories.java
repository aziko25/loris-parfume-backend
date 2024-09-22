package loris.parfume.Models.Items;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "categories",
        indexes = @Index(name = "idx_slug_categories", columnList = "slug")
)
public class Categories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String slug;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdTime;

    private String nameUz;
    private String nameRu;
    private String nameEng;

    @Column(length = 64000)
    private String descriptionUz;
    @Column(length = 64000)
    private String descriptionRu;

    private String bannerImage;

    private Integer sortOrderWithinCollection;
    private Boolean isRecommendedInMainPage;

    @ManyToOne
    @JoinColumn(name = "collection_id")
    private Collections collection;
}