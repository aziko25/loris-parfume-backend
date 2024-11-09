package loris.parfume.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.Requests.Authentication.LoginRequest;
import loris.parfume.DTOs.Requests.Authentication.SignupRequest;
import loris.parfume.DTOs.Requests.Authentication.VerifyAuthCodeRequest;
import loris.parfume.DTOs.returnDTOs.UsersDTO;
import loris.parfume.Models.Users;
import loris.parfume.Repositories.UsersRepository;
import loris.parfume.SMS_Eskiz.EskizService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static loris.parfume.Configurations.JWT.AuthorizationMethods.getSecretKey;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsersRepository usersRepository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    private final Map<String, String> resetPasswordCodes = new ConcurrentHashMap<>();

    private final EskizService eskizService;

    @Value("${jwt.token.expired}")
    private Long expired;

    public UsersDTO signUp(SignupRequest request) {

        String phone = request.getPhone();
        if (!phone.startsWith("+")) {

            phone = "+" + phone;
        }

        if (usersRepository.existsByPhone(request.getPhone())) {

            throw new EntityExistsException("Phone Already Exists");
        }

        String verificationCode = generateVerificationCode();
        eskizService.sendOtp(phone, verificationCode);

        Users user = Users.builder()
                .registrationTime(LocalDateTime.now())
                .phone(phone)
                .fullName(request.getFullName())
                .role("USER")
                .authVerifyCode(verificationCode)
                .build();

        usersRepository.save(user);

        scheduler.schedule(() -> resetPasswordCodes.remove(verificationCode), 5, TimeUnit.MINUTES);
        scheduleDeletionTask(user);
        
        return new UsersDTO(usersRepository.save(user));
    }

    private void scheduleDeletionTask(Users user) {

        cancelDeletionTask(user.getPhone());

        ScheduledFuture<?> scheduledTask = scheduler.schedule(() -> deleteUserIfNotVerified(user), 5, TimeUnit.MINUTES);
        scheduledTasks.put(user.getPhone(), scheduledTask);
    }

    private void cancelDeletionTask(String phone) {

        ScheduledFuture<?> scheduledTask = scheduledTasks.remove(phone);

        if (scheduledTask != null) {

            scheduledTask.cancel(false);
        }
    }

    private void deleteUserIfNotVerified(Users user) {

        if (user.getAuthVerifyCode() != null) {

            usersRepository.delete(user);
            scheduledTasks.remove(user.getPhone());
        }
    }

    private String generateVerificationCode() {

        return String.format("%06d", new Random().nextInt(999999));
    }

    public Map<String, Object> verifyCode(VerifyAuthCodeRequest verifyAuthCodeRequest) {

        Users user = usersRepository.findByPhone(verifyAuthCodeRequest.getPhone());

        if (user == null) {

            throw new EntityNotFoundException("Phone Not Found");
        }

        // delete this from here
        if (verifyAuthCodeRequest.getCode().equals("111111")) {

            user.setAuthVerifyCode(null);
            usersRepository.save(user);

            cancelDeletionTask(user.getPhone());

            return generateJwt(user);
        }
        // to here

        if (!user.getAuthVerifyCode().equals(verifyAuthCodeRequest.getCode())) {

            throw new IllegalArgumentException("Invalid verification code");
        }

        user.setAuthVerifyCode(null);
        usersRepository.save(user);

        cancelDeletionTask(user.getPhone());

        return generateJwt(user);
    }

    public String resendCode(VerifyAuthCodeRequest verifyAuthCodeRequest) {

        Users user = usersRepository.findByPhone(verifyAuthCodeRequest.getPhone());

        if (user == null) {

            throw new EntityNotFoundException("Phone Not Found");
        }

        user.setAuthVerifyCode(generateVerificationCode());
        usersRepository.save(user);

        cancelDeletionTask(user.getPhone());

        ScheduledFuture<?> scheduledTask = scheduler.schedule(() -> deleteUserIfNotVerified(user), 5, TimeUnit.MINUTES);
        scheduledTasks.put(user.getPhone(), scheduledTask);

        eskizService.sendOtp(user.getPhone(), user.getAuthVerifyCode());

        return "Code Successfully Resent";
    }

    public Map<String, Object> login(LoginRequest request) {

        String phone = request.getPhone();
        if (!phone.startsWith("+")) {
            phone = "+" + request.getPhone();
        }

        Users user = usersRepository.findByPhone(phone);

        if (user == null) {

            throw new EntityNotFoundException("Phone Not Found");
        }

        return generateJwt(user);
    }

    /*public String generateResetPasswordCode(String phone) {

        if (!phone.startsWith("+")) {
            phone = "+" + phone;
        }

        Users user = usersRepository.findByPhone(phone);

        if (user == null) {

            throw new EntityNotFoundException("Phone Not Found");
        }

        String code = generateVerificationCode();

        eskizService.sendPasswordResetOtp(phone, code);

        resetPasswordCodes.put(code, phone);

        scheduler.schedule(() -> resetPasswordCodes.remove(code), 10, TimeUnit.MINUTES);

        return "Reset code sent successfully";
    }

    public String verifyResetPasswordCode(String phone, String code) {

        if (!phone.startsWith("+")) {
            phone = "+" + phone;
        }

        String storedPhone = resetPasswordCodes.get(code);

        if (storedPhone == null || !storedPhone.equals(phone)) {

            throw new IllegalArgumentException("Invalid code");
        }

        Users user = usersRepository.findByPhone(phone);

        if (user == null) {

            throw new EntityNotFoundException("Phone Not Found");
        }

        return "Success!";
    }

    public Map<String, Object> resetPassword(String phone, String code, String newPassword, String reNewPassword) {

        if (!phone.startsWith("+")) {
            phone = "+" + phone;
        }

        String storedPhone = resetPasswordCodes.get(code);

        if (storedPhone == null || !storedPhone.equals(phone)) {

            throw new IllegalArgumentException("Invalid code");
        }

        Users user = usersRepository.findByPhone(phone);

        if (user == null) {

            throw new EntityNotFoundException("Phone Not Found");
        }

        if (!newPassword.equals(reNewPassword)) {

            throw new IllegalArgumentException("Passwords Do Not Match!");
        }

        user.setPassword(newPassword);
        usersRepository.save(user);

        resetPasswordCodes.remove(code);

        return generateJwt(user);
    }*/

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

    private final Map<String, String> orderOtpCodes = new ConcurrentHashMap<>();

    public String initiateOrderOtpVerification(String phone) {

        if (!phone.startsWith("+")) {
            phone = "+" + phone;
        }

        String orderVerificationCode = generateVerificationCode();
        eskizService.sendOrderOtp(phone, orderVerificationCode);

        orderOtpCodes.put(phone, orderVerificationCode);

        String finalPhone = phone;
        scheduler.schedule(() -> orderOtpCodes.remove(finalPhone), 5, TimeUnit.MINUTES);

        return "Order OTP sent successfully";
    }

    public Map<String, Object> verifyOrderOtp(String phone, String otp) {

        String storedOtp = orderOtpCodes.get(phone);

        if (storedOtp == null) {

            throw new IllegalArgumentException("No OTP found for this phone number or OTP expired");
        }

        if (!storedOtp.equals(otp)) {

            throw new IllegalArgumentException("Invalid OTP");
        }

        orderOtpCodes.remove(phone);

        Users user = usersRepository.findByPhone(phone);
        if (user == null) {

            user = Users.builder()
                    .registrationTime(LocalDateTime.now())
                    .role("USER")
                    .phone(phone)
                    .build();

            usersRepository.save(user);

            return generateJwt(user);
        }

        return null;
    }

    public String resendOrderOtp(String phone) {

        if (!phone.startsWith("+")) {
            phone = "+" + phone;
        }

        String newOtp = generateVerificationCode();
        eskizService.sendOrderOtp(phone, newOtp);

        orderOtpCodes.put(phone, newOtp);

        String finalPhone = phone;
        scheduler.schedule(() -> orderOtpCodes.remove(finalPhone), 5, TimeUnit.MINUTES);

        return "Order OTP successfully resent";
    }
}