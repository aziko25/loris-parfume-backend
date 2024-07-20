package loris.parfume.Configurations.Telegram;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(MainTelegramBot mainTelegramBot) throws TelegramApiException {

        var api = new TelegramBotsApi(DefaultBotSession.class);

        api.registerBot(mainTelegramBot);

        return api;
    }
}