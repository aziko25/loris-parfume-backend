package loris.parfume.Controllers.Orders;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.Telegram.MainTelegramBot;
import loris.parfume.Models.Items.Sizes_Items;
import loris.parfume.Models.Orders.Orders;
import loris.parfume.Models.Orders.Orders_Items;
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

    @Value("${payment.chat.id}")
    private String chatId;

    @PostMapping("/prepare-order")
    public ResponseEntity<?> prepareOrder(@RequestParam Map<String, String> body) {

        String clickTransId = body.get("click_trans_id");
        String merchantTransId = body.get("merchant_trans_id");

        Map<String, String> response = new HashMap<>();

        response.put("click_trans_id", clickTransId);
        response.put("merchant_trans_id", merchantTransId);
        response.put("merchant_prepare_id", merchantTransId);
        response.put("error", "0");
        response.put("error_note", "Success");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete-order")
    public ResponseEntity<Map<String, Object>> completeOrder(@RequestParam Map<String, String> body) {

        String clickTransId = body.get("click_trans_id");
        String merchantTransId = body.get("merchant_trans_id");
        String error = body.get("error");

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

            return createErrorResponse(response, "Заказ Не Найден!");
        }

        List<Orders_Items> ordersItemsList = ordersItemsRepository.findAllByOrder(order);

        for (Orders_Items ordersItems : ordersItemsList) {

            if (!isStockAvailable(ordersItems)) {

                return createErrorResponse(response, "Товар Закончился!");
            }
        }

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

        response.put("error", "0");
        response.put("error_note", "Success");
        response.put("merchant_confirm_id", 1);

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(Map<String, Object> response, String errorNote) {

        response.put("error", "-1905");
        response.put("error_note", errorNote);

        return ResponseEntity.ok(response);
    }

    private boolean isStockAvailable(Orders_Items ordersItems) {

        if (ordersItems.getSize().getIsDefaultNoSize()) {

            return ordersItems.getItem().getQuantity() >= ordersItems.getQuantity();
        }
        else {

            Sizes_Items sizesItem = sizesItemsRepository.findByItemAndSize(ordersItems.getItem(), ordersItems.getSize());

            return sizesItem != null && sizesItem.getQuantity() >= ordersItems.getQuantity();
        }
    }
}