package loris.parfume.Controllers.Orders;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Requests.Orders.OrdersRequest;
import loris.parfume.Services.Orders.OrdersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class OrdersController {

    private final OrdersService ordersService;

    //@Authorization(requiredRoles = {"ADMIN", "USER"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody OrdersRequest ordersRequest) {

        return new ResponseEntity<>(ordersService.create(ordersRequest), HttpStatus.CREATED);
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/all")
    public ResponseEntity<?> all(@RequestParam Integer page) {

        return ResponseEntity.ok(ordersService.all(page));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {

        return ResponseEntity.ok(ordersService.getById(id));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody OrdersRequest ordersRequest) {

        return ResponseEntity.ok(ordersService.update(id, ordersRequest));
    }

    @Authorization(requiredRoles = {"ADMIN", "USER"})
    @PostMapping("/allMy")
    public ResponseEntity<?> allMy(@RequestParam Integer page) {

        return ResponseEntity.ok(ordersService.myOrders(page));
    }

    @Authorization(requiredRoles = {"ADMIN", "USER"})
    @GetMapping("/my/{id}")
    public ResponseEntity<?> my(@PathVariable Long id) {

        return ResponseEntity.ok(ordersService.myOrdersById(id));
    }
}