package loris.parfume.Controllers;

import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.Requests.Authentication.LoginRequest;
import loris.parfume.DTOs.Requests.Authentication.SignupRequest;
import loris.parfume.Services.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@RequestBody SignupRequest signupRequest) {

        return new ResponseEntity<>(authenticationService.signUp(signupRequest), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        return ResponseEntity.ok(authenticationService.login(loginRequest));
    }
}