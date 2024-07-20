package loris.parfume.Configurations.Telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MainTelegramBot extends TelegramLongPollingBot {

    public MainTelegramBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Value("${bot.username}")
    private String botUsername;

    public void sendMessage(SendMessage message) {

        try {

            execute(message);
        }
        catch (TelegramApiException e) {

            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {

        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {

    }
}