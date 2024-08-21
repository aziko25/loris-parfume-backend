package loris.parfume.DTOs.Requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BranchesRequest {

    private String name;
    private String phone;

    private Double longitude;
    private Double latitude;

    private String redirectTo;
}