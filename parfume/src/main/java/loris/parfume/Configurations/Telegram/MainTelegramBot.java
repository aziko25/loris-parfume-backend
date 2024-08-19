package loris.parfume.Configurations.Telegram;

import jakarta.persistence.EntityNotFoundException;
import loris.parfume.Models.Orders.Orders;
import loris.parfume.Models.Orders.Uzum_Nasiya_Clients;
import loris.parfume.Repositories.Orders.OrdersRepository;
import loris.parfume.Repositories.Orders.Uzum_Nasiya_Clients_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MainTelegramBot extends TelegramLongPollingBot {

    @Autowired
    private Uzum_Nasiya_Clients_Repository uzumNasiyaClientsRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    public MainTelegramBot(@Value("${bot.token}") String botToken, Uzum_Nasiya_Clients_Repository uzumNasiyaClientsRepository,
                           OrdersRepository ordersRepository) {

        super(botToken);
        this.uzumNasiyaClientsRepository = uzumNasiyaClientsRepository;
        this.ordersRepository = ordersRepository;
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
    public void onUpdateReceived(Update update) {

        if (update.hasCallbackQuery()) {

            try {
                handleCallbackQuery(update.getCallbackQuery());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) throws TelegramApiException {

        String callbackData = callbackQuery.getData();
        String chatId = callbackQuery.getMessage().getChatId().toString();
        Integer messageId = callbackQuery.getMessage().getMessageId();  // Get the message ID

        Long orderId = Long.valueOf(callbackData.split("_")[1]);
        Orders order = ordersRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("Order Not Found"));

        Uzum_Nasiya_Clients uzumNasiyaClient = uzumNasiyaClientsRepository.findByOrder(order)
                .orElseThrow(() -> new EntityNotFoundException("Nasiya Client Not Found"));

        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        if (callbackData.startsWith("confirm_")) {

            order.setIsPaid(true);
            ordersRepository.save(order);

            uzumNasiyaClient.setIsApproved(true);
            uzumNasiyaClientsRepository.save(uzumNasiyaClient);

            message.setText("Nasiya Approved For Order ID: " + order.getId() + "!");
            sendMessage(message);
        }
        else if (callbackData.startsWith("reject_")) {

            uzumNasiyaClient.setIsApproved(false);
            uzumNasiyaClientsRepository.save(uzumNasiyaClient);

            message.setText("Nasiya Is Not Approved For Order ID: " + order.getId() + "!");
            sendMessage(message);
        }

        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();

        editMessageReplyMarkup.setChatId(chatId);
        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setReplyMarkup(null);

        execute(editMessageReplyMarkup);
    }


    @Override
    public String getBotUsername() {

        return botUsername;
    }
}