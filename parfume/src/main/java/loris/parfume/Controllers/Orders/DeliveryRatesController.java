package loris.parfume.Controllers.Orders;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Requests.Orders.DeliveryRatesRequest;
import loris.parfume.Services.Orders.DeliveryRatesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/delivery-rates")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class DeliveryRatesController {

    /*private final DeliveryRatesService deliveryRatesService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody DeliveryRatesRequest deliveryRatesRequest) {

        return new ResponseEntity<>(deliveryRatesService.create(deliveryRatesRequest), HttpStatus.CREATED);
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/all")
    public ResponseEntity<?> all(@RequestParam Integer page, @RequestParam(required = false) String search) {

        return ResponseEntity.ok(deliveryRatesService.all(page, search));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        return ResponseEntity.ok(deliveryRatesService.getById(id));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody(required = false) DeliveryRatesRequest deliveryRatesRequest) {

        return ResponseEntity.ok(deliveryRatesService.update(id, deliveryRatesRequest));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return ResponseEntity.ok(deliveryRatesService.delete(id));
    }*/
}