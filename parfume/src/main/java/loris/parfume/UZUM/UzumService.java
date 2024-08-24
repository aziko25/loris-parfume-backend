package loris.parfume.UZUM;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.Telegram.MainTelegramBot;
import loris.parfume.Models.Orders.Orders;
import loris.parfume.Repositories.Orders.OrdersRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.HashMap;
import java.util.Map;

import static loris.parfume.Controllers.Orders.ClickOrdersController.orderDetailsMessage;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class UzumService {

    private final OrdersRepository ordersRepository;
    private final MainTelegramBot mainTelegramBot;
    private final UzumOrderTransactionsRepository uzumOrderTransactionsRepository;

    @Value("${payment.chat.id}")
    private String paymentChatId;

    public Map<String, Object> check(Map<String, Object> request) {

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        Integer serviceId = (Integer) request.get("serviceId");

        Map<String, Object> params = (Map<String, Object>) request.get("params");
        Long orderId = (Long) params.get("orderId");

        response.put("serviceId", serviceId);
        response.put("timestamp", System.currentTimeMillis());

        Orders order = ordersRepository.findById(orderId).orElse(null);

        if (order != null && !order.getIsPaid() && order.getPaymentType().equalsIgnoreCase("UZUM")) {

            response.put("status", "OK");
        }
        else {

            response.put("status", "FAILED");
            response.put("errorCode", "10007");

            return response;
        }

        data.put("orderId", orderId);
        response.put("data", data);

        return response;
    }

    public Map<String, Object> create(Map<String, Object> request) {

        UzumOrderTransactions uzumOrderTransactions = new UzumOrderTransactions();

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        Integer serviceId = (Integer) request.get("serviceId");
        String transId = (String) request.get("transId");

        Map<String, Object> params = (Map<String, Object>) request.get("params");
        Long orderId = (Long) params.get("orderId");
        Integer amount = (Integer) params.get("amount");

        response.put("serviceId", serviceId);
        response.put("transId", transId);
        response.put("transTime", System.currentTimeMillis());

        uzumOrderTransactions.setTransId(transId);
        uzumOrderTransactions.setTransTime(System.currentTimeMillis());
        uzumOrderTransactions.setAmount(amount);
        uzumOrderTransactions.setTransTime((Long) response.get("transTime"));

        Orders order = ordersRepository.findById(orderId).orElse(null);

        if (order != null && !order.getIsPaid() && order.getPaymentType().equalsIgnoreCase("UZUM") &&
                Math.abs(order.getTotalSum() * 100 - amount) > 0.00001) {

            response.put("status", "CREATED");
            uzumOrderTransactions.setStatus("CREATED");
            uzumOrderTransactions.setOrder(order);
        }
        else {

            response.put("status", "FAILED");
            response.put("errorCode", "10013");
            uzumOrderTransactions.setStatus("FAILED");
            uzumOrderTransactions.setErrorCode("10013");

            uzumOrderTransactionsRepository.save(uzumOrderTransactions);

            return response;
        }

        data.put("orderId", orderId);
        response.put("data", data);
        response.put("amount", amount);

        uzumOrderTransactionsRepository.save(uzumOrderTransactions);

        return response;
    }

    public Map<String, Object> confirm(Map<String, Object> request) {

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        Integer serviceId = (Integer) request.get("serviceId");
        String transId = (String) request.get("transId");

        response.put("serviceId", serviceId);
        response.put("transId", transId);
        response.put("confirmTime", System.currentTimeMillis());

        Map<String, Object> params = (Map<String, Object>) request.get("params");
        Long orderId = (Long) params.get("orderId");
        Integer amount = (Integer) params.get("amount");

        UzumOrderTransactions uzumOrderTransaction = uzumOrderTransactionsRepository.findByTransId(transId).orElse(null);
        Orders order = ordersRepository.findById(orderId).orElse(null);

        if (isTransactionAndOrderValid(uzumOrderTransaction, order, amount) && !order.getIsPaid()) {

            String paymentSource = (String) request.get("paymentSource");
            uzumOrderTransaction.setPaymentSource(paymentSource);
            uzumOrderTransaction.setConfirmTime((Long) response.get("confirmTime"));

            response.put("status", "CONFIRMED");
            data.put("orderId", orderId);
            response.put("data", data);
            response.put("amount", amount);

            uzumOrderTransactionsRepository.save(uzumOrderTransaction);

            SendMessage message = new SendMessage();

            message.setChatId(paymentChatId);
            message.setText(orderDetailsMessage(order, "UZUM"));
        }
        else {

            response.put("status", "FAILED");
            response.put("errorCode", "10014");
        }

        return response;
    }

    public Map<String, Object> reverse(Map<String, Object> request) {

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        Integer serviceId = (Integer) request.get("serviceId");
        String transId = (String) request.get("transId");

        response.put("serviceId", serviceId);
        response.put("transId", transId);
        response.put("reverseTime", System.currentTimeMillis());

        Map<String, Object> params = (Map<String, Object>) request.get("params");
        Long orderId = (Long) params.get("orderId");
        Integer amount = (Integer) params.get("amount");

        UzumOrderTransactions uzumOrderTransaction = uzumOrderTransactionsRepository.findByTransId(transId).orElse(null);
        Orders order = ordersRepository.findById(orderId).orElse(null);

        if (isTransactionAndOrderValid(uzumOrderTransaction, order, amount)) {

            uzumOrderTransaction.setStatus("REVERSED");
            uzumOrderTransaction.setReverseTime((Long) response.get("reverseTime"));

            data.put("orderId", orderId);
            response.put("status", "REVERSED");
            response.put("data", data);
            response.put("amount", amount);

            uzumOrderTransactionsRepository.save(uzumOrderTransaction);

            SendMessage message = new SendMessage();

            message.setChatId(paymentChatId);
            message.setText("Заказ Был Отменен Клиентом! ID Заказа: " + uzumOrderTransaction.getOrder().getId());
            mainTelegramBot.sendMessage(message);
        }
        else {

            response.put("status", "FAILED");
            response.put("errorCode", "10017");
        }

        return response;
    }

    public Map<String, Object> status(Map<String, Object> request) {

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        Integer serviceId = (Integer) request.get("serviceId");
        String transId = (String) request.get("transId");

        response.put("serviceId", serviceId);
        response.put("transId", transId);

        Map<String, Object> params = (Map<String, Object>) request.get("params");
        Long orderId = (Long) params.get("orderId");
        Integer amount = (Integer) params.get("amount");

        UzumOrderTransactions uzumOrderTransaction = uzumOrderTransactionsRepository.findByTransId(transId).orElse(null);
        Orders order = ordersRepository.findById(orderId).orElse(null);

        if (isTransactionAndOrderValid(uzumOrderTransaction, order, amount)) {


            response.put("status", uzumOrderTransaction.getStatus());
            response.put("transTime", uzumOrderTransaction.getTransTime());
            response.put("confirmTime", uzumOrderTransaction.getConfirmTime());
            response.put("reverseTime", uzumOrderTransaction.getReverseTime());
            data.put("orderId", orderId);
            response.put("data", data);
            response.put("amount", amount);
        }
        else {

            response.put("transTime", null);
            response.put("confirmTime", null);
            response.put("reverseTime", null);
            response.put("status", "FAILED");
            response.put("errorCode", "10017");
        }

        return response;
    }

    private boolean isTransactionAndOrderValid(UzumOrderTransactions transaction, Orders order, Integer amount) {

        return transaction != null &&
                order != null &&
                "UZUM".equalsIgnoreCase(order.getPaymentType()) &&
                Math.abs(order.getTotalSum() * 100 - amount) <= 0.00001;
    }
}