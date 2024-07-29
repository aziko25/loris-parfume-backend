package loris.parfume.Controllers.Orders;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.Telegram.MainTelegramBot;
import loris.parfume.Models.Items.Sizes_Items;
import loris.parfume.Models.Orders.Orders;
import loris.parfume.Models.Orders.Orders_Items;
import loris.parfume.Repositories.Items.ItemsRepository;
import loris.parfume.Repositories.Items.Sizes_Items_Repository;
import loris.parfume.Repositories.Orders.OrdersRepository;
import loris.parfume.Repositories.Orders.Orders_Items_Repository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@CrossOrigin(maxAge = 3600)
@RequestMapping("/api/v1/click")
public class ClickOrdersController {

    private final OrdersRepository ordersRepository;
    private final Orders_Items_Repository ordersItemsRepository;
    private final Sizes_Items_Repository sizesItemsRepository;
    private final MainTelegramBot mainTelegramBot;
    private final ItemsRepository itemsRepository;

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

        List<Orders_Items> ordersItemsList = ordersItemsRepository.findAllByOrder(order);

        for (Orders_Items ordersItems : ordersItemsList) {

            if (!isStockAvailable(ordersItems)) {

                return createErrorResponse(response, -406, "Товар закончился");
            }
        }

        sendOrderDetailsToTelegram(order);

        response.put("error", "0");
        response.put("error_note", "Success");
        response.put("merchant_confirm_id", 1);

        order.setIsPaid(true);
        ordersRepository.save(order);

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> isTransactionValid(Orders order, Map<String, Object> response, Double amount) {

        if (order.getIsPaid()) {

            return createErrorResponse(response, -4, "Already paid");
        }

        if (Math.abs(order.getTotalSum() - amount) > 0.00001) {

            return createErrorResponse(response, -2, "Incorrect parameter amount");
        }

        return null;
    }

    private boolean isStockAvailable(Orders_Items ordersItems) {

        if (ordersItems.getSize().getIsDefaultNoSize()) {

            int currentQuantity = ordersItems.getItem().getQuantity();

            if (currentQuantity >= ordersItems.getQuantity()) {

                ordersItems.getItem().setQuantity(currentQuantity - ordersItems.getQuantity());
                itemsRepository.save(ordersItems.getItem());

                return true;
            }
        }
        else {

            Sizes_Items sizesItem = sizesItemsRepository.findByItemAndSize(ordersItems.getItem(), ordersItems.getSize());

            if (sizesItem != null && sizesItem.getQuantity() >= ordersItems.getQuantity()) {

                sizesItem.setQuantity(sizesItem.getQuantity() - ordersItems.getQuantity());
                sizesItemsRepository.save(sizesItem);

                return true;
            }
        }

        return false;
    }

    private void sendOrderDetailsToTelegram(Orders order) {

        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(chatId);
        String message = "Оплата\n-----------\nИмя: " + order.getUser().getFullName() +
                "\nТелефон: " + order.getPhone() +
                "\nАдрес: " + order.getAddress() +
                "\nСсылка на адрес: " + order.getAddressLocationLink() +
                "\nКомментарий: " + order.getComments() +
                "\nФилиал: " + order.getBranch().getName() +
                "\nОбщая Сумма за заказ: " + order.getTotalSum() +
                "\nСумма за доставку: " + order.getSumForDelivery() +
                "\nТовары: " + order.getItemsList().stream()
                .map(ordersItems -> ordersItems.getItem().getNameRu() + " (" + ordersItems.getQuantity() + " шт., размер: " + ordersItems.getSize().getNameRu() + ")")
                .collect(Collectors.joining(", ")) +
                "\nТип Заказа: " + (order.getIsDelivery() ? "Доставка" : "Самовывоз") +
                (order.getIsSoonDeliveryTime() ? "\nДоставка в ближайшее время" : "\nЗапланированное время доставки: " +
                        (order.getScheduledDeliveryTime() != null ? order.getScheduledDeliveryTime().toString() : "Не запланировано"));

        sendMessage.setText(message);

        mainTelegramBot.sendMessage(sendMessage);
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(Map<String, Object> response,
                                                                    Integer errorCode, String errorNote) {

        response.put("error", errorCode);
        response.put("error_note", errorNote);

        return ResponseEntity.ok(response);
    }
}