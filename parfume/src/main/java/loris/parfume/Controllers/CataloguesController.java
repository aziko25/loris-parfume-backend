package loris.parfume.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Requests.CataloguesRequest;
import loris.parfume.Services.CataloguesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogues")
@CrossOrigin(maxAge = 3600)
@RequiredArgsConstructor
public class CataloguesController {

    private final CataloguesService cataloguesService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestParam(value = "media") List<MultipartFile> media,
                                    @RequestParam("catalogue") String catalogueJson) throws IOException {

        CataloguesRequest cataloguesRequest = new ObjectMapper().readValue(catalogueJson, CataloguesRequest.class);

        return new ResponseEntity<>(cataloguesService.create(cataloguesRequest, media), HttpStatus.CREATED);
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
                                    @RequestParam(value = "media", required = false) List<MultipartFile> media,
                                    @RequestParam(value = "catalogue") String catalogueJson) throws IOException {

        CataloguesRequest cataloguesRequest = new ObjectMapper().readValue(catalogueJson, CataloguesRequest.class);

        return ResponseEntity.ok(cataloguesService.update(id, cataloguesRequest, media));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return ResponseEntity.ok(cataloguesService.delete(id));
    }
}