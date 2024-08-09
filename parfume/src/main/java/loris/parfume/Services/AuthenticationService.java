package loris.parfume.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.Requests.Authentication.LoginRequest;
import loris.parfume.DTOs.Requests.Authentication.SignupRequest;
import loris.parfume.DTOs.Requests.Authentication.VerifyAuthCodeRequest;
import loris.parfume.Models.Users;
import loris.parfume.Repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

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

        String verificationCode = generateVerificationCode();

        Users user = Users.builder()
                .registrationTime(LocalDateTime.now())
                .phone(request.getPhone())
                .fullName(request.getFullName())
                .password(request.getPassword())
                .role("USER")
                .authVerifyCode(verificationCode)
                .build();

        return usersRepository.save(user);
    }

    private String generateVerificationCode() {

        return "111111";
        //return String.format("%06d", new Random().nextInt(999999));
    }

    public Map<String, Object> verifyCode(VerifyAuthCodeRequest verifyAuthCodeRequest) {

        Users user = usersRepository.findByPhone(verifyAuthCodeRequest.getPhone());

        if (user == null) {

            throw new EntityNotFoundException("Phone Not Found");
        }

        if (!user.getAuthVerifyCode().equals(verifyAuthCodeRequest.getCode())) {

            throw new IllegalArgumentException("Invalid verification code");
        }

        user.setAuthVerifyCode(null);
        usersRepository.save(user);

        return generateJwt(user);
    }

    public Map<String, Object> login(LoginRequest request) {

        Users user = usersRepository.findByPhone(request.getPhone());

        if (user == null) {

            throw new EntityNotFoundException("Phone Not Found");
        }

        if (!Objects.equals(user.getPassword(), request.getPassword())) {

            throw new IllegalArgumentException("Password Didn't Match!");
        }

        return generateJwt(user);
    }

    private Map<String, Object> generateJwt(Users user) {

        Claims claims = Jwts.claims().setSubject(user.getPhone());
        claims.put("id", user.getId());
        claims.put("role", user.getRole());

        Date expiration = new Date(System.currentTimeMillis() + expired);

        String token = Jwts.builder()
                .setClaims(claims)
                .setExpiration(expiration)
                .signWith(getSecretKey())
                .compact();

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("id", user.getId());
        response.put("phone", user.getPhone());
        response.put("fullName", user.getFullName());
        response.put("role", user.getRole());
        response.put("token", token);

        return response;
    }
}