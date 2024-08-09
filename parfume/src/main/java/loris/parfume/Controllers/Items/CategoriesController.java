package loris.parfume.Controllers.Items;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Filters.CategoryFilters;
import loris.parfume.DTOs.Requests.Items.CategoriesRequest;
import loris.parfume.Services.Items.CategoriesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class CategoriesController {

    private final CategoriesService categoriesService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestParam(value = "media", required = false) MultipartFile image,
                                    @RequestParam("category") String categoryJson) throws JsonProcessingException {

        CategoriesRequest categoriesRequest = new ObjectMapper().readValue(categoryJson, CategoriesRequest.class);

        return new ResponseEntity<>(categoriesService.create(categoriesRequest, image), HttpStatus.CREATED);
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

    @Authorization(requiredRoles = {"ADMIN"})
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestParam(value = "media", required = false) MultipartFile image,
                                    @RequestParam("category") String categoryJson) throws JsonProcessingException {

        CategoriesRequest categoriesRequest = new ObjectMapper().readValue(categoryJson, CategoriesRequest.class);

        return ResponseEntity.ok(categoriesService.update(id, categoriesRequest, image));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return ResponseEntity.ok(categoriesService.delete(id));
    }
}