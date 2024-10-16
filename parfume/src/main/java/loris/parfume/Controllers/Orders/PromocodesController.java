package loris.parfume.Controllers.Orders;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Requests.Orders.PromocodeRequest;
import loris.parfume.Services.Orders.PromocodesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/promocodes")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class PromocodesController {

    private final PromocodesService promocodesService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody PromocodeRequest promocodeRequest) {

        return new ResponseEntity<>(promocodesService.create(promocodeRequest), HttpStatus.CREATED);
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @GetMapping("/all")
    public ResponseEntity<?> getAll() {

        return ResponseEntity.ok(promocodesService.all());
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> getByCode(@PathVariable String code) {

        return ResponseEntity.ok(promocodesService.getByCode(code));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PromocodeRequest promocodeRequest) {

        return ResponseEntity.ok(promocodesService.update(id, promocodeRequest));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return ResponseEntity.ok(promocodesService.delete(id));
    }
}