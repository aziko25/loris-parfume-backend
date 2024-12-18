package loris.parfume.SMS_Eskiz;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EskizService {

    public static String ESKIZ_TOKEN;

    private final Sms_Otp_Repository smsOtpRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${eskiz.mail}")
    private String mail;

    @Value("${eskiz.password}")
    private String password;

    @Value("${payment.chat.id}")
    private Long paymentChatId;

    @PostConstruct
    public void onStartup() {

        getEskizToken();
    }

    @Scheduled(cron = "0 0 0 * * MON")
    public void getEskizToken() {

        String url = "https://notify.eskiz.uz/api/auth/login";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("email", mail);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, TokenResponse.class);

        if (response.getBody() != null && response.getBody().getData() != null) {

            ESKIZ_TOKEN = response.getBody().getData().getToken();
        }
    }

    public void sendOtp(String phone, String code) {

        String text = "Код подтверждения для регистрации на сайте Loris Parfume: " + code;
        sendSms(phone, text);
    }

    public void sendOrderOtp(String phone, String code) {

        String text = "Код подтверждения для заказа на сайте Loris Parfume: " + code;
        sendSms(phone, text);
    }

    public void sendOrderCreatedSms(String phone, Long orderId) {

        String text = "Assalomu alaykum. Loris Parfum'dan xarid qilganingiz uchun rahmat! Buyurtma ID: " + orderId + ". Xaridingizni tez orada yetkazib beramiz!";

        sendSms(phone, text);
    }

    public void sendOrderIsOnTheWaySms(String phone, String deliveryDuration, Long orderId) {

        String text = "Assalomu alaykum. Loris Parfum'dan Buyurtma ID: " + orderId + " " + deliveryDuration + " Yetkazib Beriladi!";

        sendSms(phone, text);
    }

    private void sendSms(String phone, String text) {

        try {

            String url = "https://notify.eskiz.uz/api/message/sms/send";

            Sms_Otp smsOtp = Sms_Otp.builder()
                    .createdTime(LocalDateTime.now())
                    .text(text)
                    .phone(phone)
                    .build();

            smsOtpRepository.save(smsOtp);

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(ESKIZ_TOKEN);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

            body.add("mobile_phone", phone);
            body.add("message", text);
            body.add("from", "4546");

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        }
        catch (Exception ignored) {}
    }
}

@Getter
@Setter
class TokenResponse {

    private Data data;

    @Getter
    @Setter
    static class Data {

        private String token;
    }
}