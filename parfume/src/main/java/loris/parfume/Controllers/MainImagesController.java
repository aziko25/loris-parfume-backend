package loris.parfume.Controllers;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.Services.MainImagesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/main-images")
@CrossOrigin(maxAge = 3600)
@RequiredArgsConstructor
public class MainImagesController {

    private final MainImagesService mainImagesService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestParam(value = "image") MultipartFile image, @RequestParam String name) {

        return new ResponseEntity<>(mainImagesService.create(image, name), HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll(@RequestParam Integer page) {

        return ResponseEntity.ok(mainImagesService.all(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        return ResponseEntity.ok(mainImagesService.getById(id));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestParam(required = false) MultipartFile image,
                                    @RequestParam(required = false) String name) {

        return ResponseEntity.ok(mainImagesService.update(id, image, name));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return ResponseEntity.ok(mainImagesService.delete(id));
    }
}