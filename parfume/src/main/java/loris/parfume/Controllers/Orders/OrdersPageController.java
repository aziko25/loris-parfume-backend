package loris.parfume.Controllers.Orders;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrdersPageController {

    @GetMapping("/orders")
    public String ordersPage() {

        return "orders";
    }
}