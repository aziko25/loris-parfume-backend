package loris.parfume.DTOs.Requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CataloguesRequest {

    private String nameUz;
    private String nameRu;
    private String nameEng;

    private String descriptionUz;
    private String descriptionRu;
    private String descriptionEng;
}