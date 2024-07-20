package loris.parfume.Controllers.Items;

import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.Filters.CategoryFilters;
import loris.parfume.DTOs.Requests.Items.CategoriesRequest;
import loris.parfume.Services.Items.CategoriesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class CategoriesController {

    private final CategoriesService categoriesService;

    //@Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CategoriesRequest categoriesRequest) {

        return new ResponseEntity<>(categoriesService.create(categoriesRequest), HttpStatus.CREATED);
    }

    @PostMapping("/all")
    public ResponseEntity<?> all(@RequestParam Integer page,
                                 @RequestBody(required = false) CategoryFilters categoryFilters) {

        return ResponseEntity.ok(categoriesService.all(page, categoryFilters));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        return ResponseEntity.ok(categoriesService.getById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CategoriesRequest categoriesRequest) {

        return ResponseEntity.ok(categoriesService.update(id, categoriesRequest));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return ResponseEntity.ok(categoriesService.delete(id));
    }
}