package loris.parfume.Controllers;

import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.returnDTOs.OrdersDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendOrderUpdate(OrdersDTO order) {

        messagingTemplate.convertAndSend("/topic/orders", order);
    }
}