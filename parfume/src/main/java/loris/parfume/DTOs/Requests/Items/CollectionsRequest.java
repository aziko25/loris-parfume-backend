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

    private Integer sortOrder;
}