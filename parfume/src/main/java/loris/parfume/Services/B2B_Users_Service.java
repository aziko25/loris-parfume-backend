package loris.parfume.Services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.Telegram.MainTelegramBot;
import loris.parfume.Models.B2B_Users;
import loris.parfume.Repositories.B2B_Users_Repository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class B2B_Users_Service {

    private final B2B_Users_Repository b2BUsersRepository;
    private final MainTelegramBot mainTelegramBot;

    @Value("${payment.chat.id}")
    private Long chatId;

    public B2B_Users create(B2B_Users b2BUser) {

        B2B_Users bUSer = B2B_Users.builder()
                .createdTime(LocalDateTime.now())
                .fullName(b2BUser.getFullName())
                .email(b2BUser.getEmail())
                .phone(b2BUser.getPhone())
                .contactSource(b2BUser.getContactSource())
                .message(b2BUser.getMessage())
                .build();

        SendMessage message = new SendMessage();

        message.setChatId(chatId);
        message.setText("Yangi B2B Shartnoma:" +
                        "\nTo'liq Ism: " + b2BUser.getFullName() +
                        "\nEmail: " + b2BUser.getEmail() +
                        "\nTelefon Raqam: " + b2BUser.getPhone() +
                        "\nAloqa Kanali: " + b2BUser.getContactSource() +
                        "\nHabarnoma: " + b2BUser.getMessage());

        mainTelegramBot.sendMessage(message);

        return b2BUsersRepository.save(bUSer);
    }

    public List<B2B_Users> all() {

        return b2BUsersRepository.findAll(Sort.by("createdTime").descending());
    }

    public B2B_Users getById(Long id) {

        return b2BUsersRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("B2B User Not Found"));
    }

    public String delete(Long id) {

        B2B_Users bUSer = getById(id);

        b2BUsersRepository.delete(bUSer);

        return "Successfully Deleted";
    }
}