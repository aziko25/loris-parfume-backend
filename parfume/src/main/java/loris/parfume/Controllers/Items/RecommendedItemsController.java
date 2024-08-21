package loris.parfume.Controllers.Items;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Requests.Items.RecommendedItemsRequest;
import loris.parfume.Services.Items.Recommended_Items_Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommended-items")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class RecommendedItemsController {

    private final Recommended_Items_Service recommendedItemsService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody RecommendedItemsRequest recommendedItemsRequest) {

        return new ResponseEntity<>(recommendedItemsService.create(recommendedItemsRequest), HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<?> all() {

        return ResponseEntity.ok(recommendedItemsService.all());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        return ResponseEntity.ok(recommendedItemsService.getById(id));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody(required = false) RecommendedItemsRequest recommendedItemsRequest) {

        return ResponseEntity.ok(recommendedItemsService.update(recommendedItemsRequest));
    }
}