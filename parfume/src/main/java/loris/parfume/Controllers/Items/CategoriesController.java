package loris.parfume.Controllers.Items;

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

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class CategoriesController {

    private final CategoriesService categoriesService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestParam(value = "media", required = false) MultipartFile image,
                                    @RequestParam("category") String categoryJson) throws IOException {

        CategoriesRequest categoriesRequest = new ObjectMapper().readValue(categoryJson, CategoriesRequest.class);

        return new ResponseEntity<>(categoriesService.create(categoriesRequest, image), HttpStatus.CREATED);
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
                                    @RequestParam(value = "media", required = false) MultipartFile image,
                                    @RequestParam("category") String categoryJson) throws IOException {

        CategoriesRequest categoriesRequest = new ObjectMapper().readValue(categoryJson, CategoriesRequest.class);

        return ResponseEntity.ok(categoriesService.update(slug, categoriesRequest, image));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{slug}")
    public ResponseEntity<?> delete(@PathVariable String slug) {

        return ResponseEntity.ok(categoriesService.delete(slug));
    }
}