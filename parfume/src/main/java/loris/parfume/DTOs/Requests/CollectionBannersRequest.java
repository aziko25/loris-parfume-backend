package loris.parfume.DTOs.Requests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CollectionBannersRequest {

    private String titleUz;
    private String titleRu;
    private String titleEng;

    private List<String> imagesUrl;

    private String redirectTo;
    private Boolean isActive;
}