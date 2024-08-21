package loris.parfume.Controllers.Orders;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import loris.parfume.PAYME.Account;
import loris.parfume.PAYME.Exceptions.*;
import loris.parfume.PAYME.PaymeMerchantService;
import loris.parfume.PAYME.Result.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Date;

@RestController
@RequestMapping("/api/v1/payme")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class PaymeOrdersController {

    private final PaymeMerchantService merchantService;

    @PostMapping("/perform-transactions")
    public ResponseEntity<?> handleTransaction(@RequestHeader(required = false) HttpHeaders headers,
                                               @RequestBody JsonNode jsonRequest) throws OrderNotExistsException, WrongAmountException, UnableCompleteException, TransactionNotFoundException, UnableCancelTransactionException {

        String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith("Basic ")) {

            String base64Credentials = authorization.substring("Basic ".length()).trim();
            String credentials;

            try {

                credentials = new String(Base64.getDecoder().decode(base64Credentials));
            }
            catch (IllegalArgumentException e) {

                throw new UnableCompleteException("Corrupted headers", -32504, "authorization");
            }

            String[] values = credentials.split(":");

            if (values.length == 2) {

                String username = values[0];
                String password = values[1];
            }
            else {

                throw new UnableCompleteException("Corrupted headers", -32504, "authorization");
            }
        }
        else {

            throw new UnableCompleteException("Corrupted headers", -32504, "authorization");
        }

        String method = jsonRequest.get("method").asText();
        JsonNode params = jsonRequest.get("params");
        JsonNode accountJson = params.get("account");
        Account account;

        String id;

        switch (method) {

            case "CheckPerformTransaction":

                double amount = params.get("amount").doubleValue();

                if (!accountJson.isEmpty()) {
                    account = new Account(accountJson.get("orderId").asText());
                }
                else {
                    account = new Account("1");
                }

                return ResponseEntity.ok(merchantService.checkPerformTransaction(amount, Long.valueOf(account.getOrderId())));

            case "CreateTransaction":

                id = params.get("id").asText();
                long time = params.get("time").longValue();
                amount = params.get("amount").intValue();
                Date transactionDate = new Date(time);

                if (!accountJson.isEmpty()) {
                    account = new Account(accountJson.get("orderId").asText());
                }
                else {
                    account = new Account("1");
                }

                return ResponseEntity.ok(merchantService.createTransaction(Long.valueOf(id), transactionDate, amount, Long.valueOf(account.getOrderId())));

            case "CheckTransaction":

                id = params.get("id").asText();

                return ResponseEntity.ok(merchantService.checkTransaction(Long.valueOf(id)));

            case "PerformTransaction":

                id = params.get("id").asText();

                return ResponseEntity.ok(merchantService.performTransaction(Long.valueOf(id)));

            case "CancelTransaction":

                id = params.get("id").asText();
                int reasonCode = params.get("reason").intValue();

                OrderCancelReason reason = OrderCancelReason.fromCode(reasonCode);

                return ResponseEntity.ok(merchantService.cancelTransaction(Long.valueOf(id), reason));

            case "GetStatement":

                long from = params.get("from").longValue();
                long to = params.get("to").longValue();

                Date fromDate = new Date(from);
                Date toDate = new Date(to);

                return ResponseEntity.ok(merchantService.getStatement(fromDate, toDate));

            default:
                return ResponseEntity.badRequest().body("Unsupported method");
        }
    }
}