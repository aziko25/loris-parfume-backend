package loris.parfume.Controllers.Items;

import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.Requests.Items.CollectionsRequest;
import loris.parfume.Services.Items.CollectionsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class CollectionsController {

    private final CollectionsService collectionsService;

    //@Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CollectionsRequest collectionsRequest) {

        return new ResponseEntity<>(collectionsService.create(collectionsRequest), HttpStatus.CREATED);
    }

    @PostMapping("/all")
    public ResponseEntity<?> all(@RequestParam Integer page, @RequestParam(required = false) String name) {

        return new ResponseEntity<>(collectionsService.all(page, name), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        return ResponseEntity.ok(collectionsService.getById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody(required = false) CollectionsRequest collectionsRequest) {

        return ResponseEntity.ok(collectionsService.update(id, collectionsRequest));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return ResponseEntity.ok(collectionsService.delete(id));
    }
}