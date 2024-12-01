package loris.parfume.DTOs.Requests.Authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    private String fullName;
    private String phone;
    private String password;
}