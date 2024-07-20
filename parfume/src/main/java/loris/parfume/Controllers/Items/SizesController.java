package loris.parfume.Controllers.Items;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Requests.Items.SizesRequest;
import loris.parfume.DTOs.Requests.Items.Sizes_Items_Request;
import loris.parfume.Services.Items.SizesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items-sizes")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class SizesController {

    private final SizesService sizesService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody SizesRequest sizesRequest) {

        return new ResponseEntity<>(sizesService.create(sizesRequest), HttpStatus.CREATED);
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/addItems/{id}")
    public ResponseEntity<?> addItems(@PathVariable Long id, @RequestBody List<Sizes_Items_Request> sizesItemsRequest) {

        return ResponseEntity.ok(sizesService.addItems(id, sizesItemsRequest));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @PutMapping("/updateItems/{id}")
    public ResponseEntity<?> updateItems(@PathVariable Long id,
                                         @RequestBody(required = false) List<Sizes_Items_Request> sizesItemsRequest) {

        return ResponseEntity.ok(sizesService.updateItems(id, sizesItemsRequest));
    }

    @PostMapping("/all")
    public ResponseEntity<?> all(@RequestParam Integer page, @RequestParam(required = false) String search) {

        return ResponseEntity.ok(sizesService.all(page, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        return ResponseEntity.ok(sizesService.getById(id));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody(required = false) SizesRequest sizesRequest) {

        return ResponseEntity.ok(sizesService.update(id, sizesRequest));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return ResponseEntity.ok(sizesService.delete(id));
    }
}