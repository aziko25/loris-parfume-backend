package loris.parfume.Models.Items;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "items")
public class Items_ElasticSearch {

    @Id
    private Long id;

    private String slug;

    private String barcode;

    private String nameUz;
    private String nameRu;
    private String nameEng;

    private String descriptionUz;
    private String descriptionRu;
    private String descriptionEng;

    private Double price;
    private Integer discountPercent;

    private Boolean isActive;
}