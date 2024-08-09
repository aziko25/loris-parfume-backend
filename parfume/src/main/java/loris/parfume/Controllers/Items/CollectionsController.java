package loris.parfume.Controllers.Items;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Requests.Items.CollectionsRequest;
import loris.parfume.Services.Items.CollectionsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class CollectionsController {

    private final CollectionsService collectionsService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestParam(value = "media") MultipartFile image,
                                    @RequestParam("collection") String collectionJson) throws JsonProcessingException {

        CollectionsRequest collectionsRequest = new ObjectMapper().readValue(collectionJson, CollectionsRequest.class);

        return new ResponseEntity<>(collectionsService.create(collectionsRequest, image), HttpStatus.CREATED);
    }

    @PostMapping("/all")
    public ResponseEntity<?> all(@RequestParam Integer page, @RequestParam(required = false) String name) {

        return new ResponseEntity<>(collectionsService.all(page, name), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        return ResponseEntity.ok(collectionsService.getById(id));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestParam(value = "media") MultipartFile image,
                                    @RequestParam("collection") String collectionJson) throws JsonProcessingException {

        CollectionsRequest collectionsRequest = new ObjectMapper().readValue(collectionJson, CollectionsRequest.class);

        return ResponseEntity.ok(collectionsService.update(id, collectionsRequest, image));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return ResponseEntity.ok(collectionsService.delete(id));
    }
}