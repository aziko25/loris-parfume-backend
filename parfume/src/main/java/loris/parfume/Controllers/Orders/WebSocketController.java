package loris.parfume.Controllers.Orders;

import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.returnDTOs.OrdersDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@Controller
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendOrderUpdate(OrdersDTO order) {

        System.out.println("Income new Order");
        messagingTemplate.convertAndSend("/topic/orders", order);
    }
}