package loris.parfume.Controllers;

import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.Requests.Authentication.AuthRequest;
import loris.parfume.DTOs.Requests.Authentication.VerifyAuthCodeRequest;
import loris.parfume.Services.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<?> authenticate(@RequestBody AuthRequest authRequest) {

        return ResponseEntity.ok(authenticationService.authenticate(authRequest));
    }

    /*@PostMapping("/generateResetPasswordCode")
    public ResponseEntity<?> generateResetPasswordCode(@RequestParam String phone) {

        return ResponseEntity.ok(authenticationService.generateResetPasswordCode(phone));
    }

    @PostMapping("/verifyResetPasswordCode")
    public ResponseEntity<?> checkResetPasswordCode(@RequestParam String phone, @RequestParam String code){

        return ResponseEntity.ok(authenticationService.verifyResetPasswordCode(phone, code));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestParam String phone, @RequestParam String code,
                                           @RequestParam String newPassword, @RequestParam String reNewPassword) {

        return ResponseEntity.ok(authenticationService.resetPassword(phone, code, newPassword, reNewPassword));
    }*/

    @PostMapping("/verifyCode")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyAuthCodeRequest verifyAuthCodeRequest) {

        return ResponseEntity.ok(authenticationService.verifyCode(verifyAuthCodeRequest));
    }

    @PostMapping("/resendCode")
    public ResponseEntity<?> resendCode(@RequestBody VerifyAuthCodeRequest verifyAuthCodeRequest) {

        return ResponseEntity.ok(authenticationService.resendCode(verifyAuthCodeRequest));
    }

    @PostMapping("/create/order-otp")
    public ResponseEntity<?> createOrderOtp(@RequestParam String phone) {

        return ResponseEntity.ok(authenticationService.initiateOrderOtpVerification(phone));
    }

    @PostMapping("/verify/order-otp")
    public ResponseEntity<?> verifyOrderOtp(@RequestParam String phone, @RequestParam String code) {

        return ResponseEntity.ok(authenticationService.verifyOrderOtp(phone, code));
    }

    @PostMapping("/resend/order-otp")
    public ResponseEntity<?> resendOrderOtp(@RequestParam String phone) {

        return ResponseEntity.ok(authenticationService.resendOrderOtp(phone));
    }
}