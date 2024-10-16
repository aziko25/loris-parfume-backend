package loris.parfume.DTOs.Requests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BannersRequest {

    private String titleUz;
    private String titleRu;
    private String titleEng;

    private List<String> desktopImagesUrl;
    private List<String> mobileImagesUrl;

    private String redirectTo;
    private Boolean isActive;
}