package loris.parfume.Controllers.Items;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Filters.ItemFilters;
import loris.parfume.DTOs.Requests.Items.ItemsRequest;
import loris.parfume.Services.Items.ItemsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class ItemsController {

    private final ItemsService itemsService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody ItemsRequest itemsRequest) throws IOException {

        return new ResponseEntity<>(itemsService.create(itemsRequest), HttpStatus.CREATED);
    }

    @PostMapping("/all")
    public ResponseEntity<?> all(@RequestParam Integer page,
                                 @RequestParam(required = false) String collectionSlug,
                                 @RequestParam(required = false) String categorySlug,
                                 @RequestBody(required = false) ItemFilters itemFilters) {

        return ResponseEntity.ok(itemsService.all(page, collectionSlug, categorySlug, itemFilters));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> getBySlug(@PathVariable String slug) {

        return ResponseEntity.ok(itemsService.getBySlug(slug));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @PutMapping("/update/{slug}")
    public ResponseEntity<?> update(@PathVariable String slug,
                                    @RequestBody ItemsRequest itemsRequest) throws IOException {

        return ResponseEntity.ok(itemsService.update(slug, itemsRequest));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{slug}")
    public ResponseEntity<?> delete(@PathVariable String slug) {

        return ResponseEntity.ok(itemsService.delete(slug));
    }
}