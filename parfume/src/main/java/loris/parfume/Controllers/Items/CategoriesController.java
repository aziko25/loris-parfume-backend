package loris.parfume.Controllers.Items;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Filters.CategoryFilters;
import loris.parfume.DTOs.Requests.Items.CategoriesRequest;
import loris.parfume.Services.Items.CategoriesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class CategoriesController {

    private final CategoriesService categoriesService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CategoriesRequest categoriesRequest) throws IOException {

        return new ResponseEntity<>(categoriesService.create(categoriesRequest), HttpStatus.CREATED);
    }

    @PostMapping("/all")
    public ResponseEntity<?> all(@RequestParam Integer page,
                                 @RequestBody(required = false) CategoryFilters categoryFilters) {

        return ResponseEntity.ok(categoriesService.all(page, categoryFilters));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> getBySlug(@PathVariable String slug) {

        return ResponseEntity.ok(categoriesService.getBySlug(slug));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @PutMapping("/update/{slug}")
    public ResponseEntity<?> update(@PathVariable String slug,
                                    @RequestBody CategoriesRequest categoriesRequest) throws IOException {

        return ResponseEntity.ok(categoriesService.update(slug, categoriesRequest));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{slug}")
    public ResponseEntity<?> delete(@PathVariable String slug) {

        return ResponseEntity.ok(categoriesService.delete(slug));
    }
}