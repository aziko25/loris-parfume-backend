package loris.parfume.DTOs.Requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BannersRequest {

    private String titleUz;
    private String titleRu;
    private String titleEng;

    private String redirectTo;
    private Boolean isActive;
}