package loris.parfume.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.Requests.Authentication.LoginRequest;
import loris.parfume.DTOs.Requests.Authentication.SignupRequest;
import loris.parfume.Models.Users;
import loris.parfume.Repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import static loris.parfume.Configurations.JWT.AuthorizationMethods.getSecretKey;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsersRepository usersRepository;

    @Value("${jwt.token.expired}")
    private Long expired;

    public Users signUp(SignupRequest request) {

        if (!request.getPassword().equals(request.getRePassword())) {

            throw new IllegalArgumentException("Passwords do not match");
        }

        if (usersRepository.existsByPhone(request.getPhone())) {

            throw new EntityExistsException("Phone Already Exists");
        }

        Users user = Users.builder()
                .registrationTime(LocalDateTime.now())
                .phone(request.getPhone())
                .fullName(request.getFullName())
                .password(request.getPassword())
                .role("USER")
                .build();

        return usersRepository.save(user);
    }

    public Map<String, Object> login(LoginRequest request) {

        Users user = usersRepository.findByPhone(request.getPhone());

        if (user == null) {

            throw new EntityNotFoundException("User not found");
        }

        if (Objects.equals(user.getPassword(), request.getPassword())) {

            Claims claims = Jwts.claims();

            claims.put("id", user.getId());
            claims.put("role", user.getRole());

            // Expires in a week
            Date expiration = new Date(System.currentTimeMillis() + expired);

            Map<String, Object> map = new LinkedHashMap<>();

            map.put("id", user.getId());
            map.put("phone", user.getPhone());
            map.put("fullName", user.getFullName());
            map.put("role", user.getRole());
            map.put("token", Jwts.builder().setClaims(claims).setExpiration(expiration).signWith(getSecretKey()).compact());

            return map;
        }
        else {

            throw new IllegalArgumentException("Password Didn't Match!");
        }
    }
}