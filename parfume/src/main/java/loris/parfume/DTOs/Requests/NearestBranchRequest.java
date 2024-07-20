package loris.parfume.DTOs.Requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NearestBranchRequest {

    private Double longitude;
    private Double latitude;
}