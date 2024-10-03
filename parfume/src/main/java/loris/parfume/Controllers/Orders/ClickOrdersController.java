package loris.parfume.Controllers.Orders;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.Telegram.MainTelegramBot;
import loris.parfume.Models.Orders.Orders;
import loris.parfume.Repositories.BasketsRepository;
import loris.parfume.Repositories.Orders.OrdersRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@CrossOrigin(maxAge = 3600)
@RequestMapping("/api/v1/click")
public class ClickOrdersController {

    private final OrdersRepository ordersRepository;
    private final MainTelegramBot mainTelegramBot;
    private final BasketsRepository basketsRepository;

    @Value("${payment.chat.id}")
    private String chatId;

    @PostMapping("/prepare-order")
    public ResponseEntity<?> prepareOrder(@RequestParam Map<String, String> body) {

        String clickTransId = body.get("click_trans_id");
        String merchantTransId = body.get("merchant_trans_id");
        String error = body.get("error");

        float amountFloat = Float.parseFloat(body.get("amount"));
        Double amount = (double) amountFloat;

        Map<String, Object> response = new HashMap<>();

        response.put("click_trans_id", clickTransId);
        response.put("merchant_trans_id", merchantTransId);

        if (!"0".equals(error)) {

            response.put("merchant_confirm_id", null);

            return ResponseEntity.ok(response);
        }

        Long orderId = Long.valueOf(merchantTransId);
        Orders order = ordersRepository.findById(orderId).orElse(null);

        if (order == null) {

            return createErrorResponse(response, -6, "Transaction does not exist");
        }

        ResponseEntity<?> validationResponse = isTransactionValid(order, response, amount);
        if (validationResponse != null) {

            return validationResponse;
        }

        response.put("merchant_prepare_id", merchantTransId);
        response.put("error", "0");
        response.put("error_note", "Success");

        return ResponseEntity.ok(response);
    }

    @Transactional
    @PostMapping("/complete-order")
    public ResponseEntity<?> completeOrder(@RequestParam Map<String, String> body) {

        String clickTransId = body.get("click_trans_id");
        String merchantTransId = body.get("merchant_trans_id");
        String error = body.get("error");

        float amountFloat = Float.parseFloat(body.get("amount"));
        Double amount = (double) amountFloat;

        Map<String, Object> response = new HashMap<>();

        response.put("click_trans_id", clickTransId);
        response.put("merchant_trans_id", merchantTransId);

        if (!"0".equals(error)) {

            response.put("merchant_confirm_id", null);

            return ResponseEntity.ok(response);
        }

        Long orderId = Long.valueOf(merchantTransId);
        Orders order = ordersRepository.findById(orderId).orElse(null);

        if (order == null) {

            return createErrorResponse(response, -6, "Transaction does not exist");
        }

        ResponseEntity<?> validationResponse = isTransactionValid(order, response, amount);
        if (validationResponse != null) {

            return validationResponse;
        }

        isTransactionValid(order, response, amount);

        sendOrderDetailsToTelegram(order);

        response.put("error", "0");
        response.put("error_note", "Success");
        response.put("merchant_confirm_id", 1);

        order.setIsPaid(true);
        ordersRepository.save(order);

        if (order.getUser() != null) {

            basketsRepository.deleteAllByUser(order.getUser());
        }

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> isTransactionValid(Orders order, Map<String, Object> response, Double amount) {

        if (order.getIsPaid()) {

            order.setPaymentResponseUz("Buyurtma To'lanbogan");
            order.setPaymentResponseRu("Заказ Уже Оплачен");
            order.setPaymentResponseEng("Order Was Already Paid");

            ordersRepository.save(order);

            return createErrorResponse(response, -4, "Already paid");
        }

        if (Math.abs(order.getTotalSum() - amount) > 0.00001) {

            return createErrorResponse(response, -2, "Incorrect parameter amount");
        }

        return null;
    }

    private void sendOrderDetailsToTelegram(Orders order) {

        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(chatId);
        sendMessage.setText(orderDetailsMessage(order, "CLICK"));

        mainTelegramBot.sendMessage(sendMessage);

        if (order.getBranch().getTgChatId() != null) {

            sendMessage = new SendMessage();

            sendMessage.setChatId(order.getBranch().getTgChatId());
            sendMessage.setText(orderDetailsMessage(order, "CLICK"));

            mainTelegramBot.sendMessage(sendMessage);
        }
    }

    public static String orderDetailsMessage(Orders order, String paymentType) {

        return  "To'lov: " + paymentType + "\n-----------\nZakaz ID: " + order.getId() + "\nIsm Sharif: " + order.getUser().getFullName() +
                "\nTelephon Raqami: " + order.getPhone() +
                "\nAddress: " + order.getAddress() +
                "\nKartadagi Addressi: " + order.getAddressLocationLink() +
                "\nIzoh: " + order.getComments() +
                "\nFilial: " + order.getBranch().getName() +
                "\nZakazning Summasi: " + order.getTotalSum() +
                "\nYetkazib Berish Summasi: " + order.getSumForDelivery() +
                "\nXaridlar: " + order.getItemsList().stream()
                .map(ordersItems -> ordersItems.getItem().getNameRu() + " (" + ordersItems.getQuantity() + " dona, razmer: " + ordersItems.getSize().getNameRu() + ")")
                .collect(Collectors.joining(", ")) +
                "\nZakaz Turi: " + (order.getIsDelivery() ? "Yetkazib Berish" : "O'zi Olib Ketish") +
                (order.getIsSoonDeliveryTime() ? "\nTez Orada Yetkazib Berilsin" : "\nShu Vaqtda Yetkazib Berilsin: " +
                        (order.getScheduledDeliveryTime() != null ? order.getScheduledDeliveryTime().toString() : "Rejalashtirilmagan"));
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(Map<String, Object> response,
                                                                    Integer errorCode, String errorNote) {

        response.put("error", errorCode);
        response.put("error_note", errorNote);

        return ResponseEntity.ok(response);
    }
}