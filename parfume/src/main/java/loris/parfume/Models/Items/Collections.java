package loris.parfume.Models.Items;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

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
        name = "collections",
        indexes = @Index(name = "idx_slug_collections", columnList = "slug")
)
public class Collections {

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

    private String bannerImage;

    private Integer sortOrder;

    @OneToMany(mappedBy = "collection")
    private List<Collections_Items> collectionsItemsList;

    @OneToMany(mappedBy = "collection")
    private List<Categories> categoriesList;
}