package loris.parfume.DTOs.returnDTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import loris.parfume.Models.Users;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsersDTO {

    private Long id;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime registrationTime;

    private String fullName;
    private String phone;

    public UsersDTO(Users user) {

        id = user.getId();
        registrationTime = user.getRegistrationTime();
        fullName = user.getFullName();
        phone = user.getPhone();
    }
}