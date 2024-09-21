package loris.parfume.DTOs.Requests.Items;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollectionsRequest {

    private String slug;

    private String nameUz;
    private String nameRu;
    private String nameEng;

    private String descriptionUz;
    private String descriptionRu;

    private Integer sortOrder;
    private Boolean isFiftyPercentSaleApplied;
    private Boolean isRecommendedInMainPage;
}