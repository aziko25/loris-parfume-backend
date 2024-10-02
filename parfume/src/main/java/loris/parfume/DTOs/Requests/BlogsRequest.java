package loris.parfume.DTOs.Requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlogsRequest {

    private String titleUz;
    private String titleRu;

    private String descriptionUz;
    private String descriptionRu;

    private Boolean isActive;
}