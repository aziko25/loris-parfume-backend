package loris.parfume.DTOs.Requests.Items;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriesRequest {

    private String slug;

    private String nameUz;
    private String nameRu;
    private String nameEng;

    private String descriptionUz;
    private String descriptionRu;

    private Boolean isRecommendedInMainPage;

    private Long collectionId;
}