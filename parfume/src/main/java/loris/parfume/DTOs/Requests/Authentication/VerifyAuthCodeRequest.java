package loris.parfume.DTOs.Requests.Authentication;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyAuthCodeRequest {

    private String phone;
    private String code;
}