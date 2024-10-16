package loris.parfume.Controllers;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Requests.CataloguesRequest;
import loris.parfume.Services.CataloguesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/catalogues")
@CrossOrigin(maxAge = 3600)
@RequiredArgsConstructor
public class CataloguesController {

    private final CataloguesService cataloguesService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CataloguesRequest cataloguesRequest) throws IOException {

        return new ResponseEntity<>(cataloguesService.create(cataloguesRequest), HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<?> all() {

        return ResponseEntity.ok(cataloguesService.all());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        return ResponseEntity.ok(cataloguesService.getById(id));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody CataloguesRequest cataloguesRequest) throws IOException {

        return ResponseEntity.ok(cataloguesService.update(id, cataloguesRequest));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return ResponseEntity.ok(cataloguesService.delete(id));
    }
}