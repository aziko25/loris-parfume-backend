package loris.parfume.Controllers;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Filters.BannerFilters;
import loris.parfume.DTOs.Requests.CollectionBannersRequest;
import loris.parfume.Services.CollectionBannersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/collection-banners")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class CollectionBannersController {

    private final CollectionBannersService collectionBannersService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CollectionBannersRequest collectionBannersRequest) throws IOException {

        return new ResponseEntity<>(collectionBannersService.create(collectionBannersRequest), HttpStatus.CREATED);
    }

    @PostMapping("/all")
    public ResponseEntity<?> all(@RequestBody(required = false) BannerFilters filters,
                                 @RequestParam Integer page) {

        return ResponseEntity.ok(collectionBannersService.all(filters, page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        return ResponseEntity.ok(collectionBannersService.getById(id));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody CollectionBannersRequest collectionBannersRequest) throws IOException {

        return ResponseEntity.ok(collectionBannersService.update(id, collectionBannersRequest));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return ResponseEntity.ok(collectionBannersService.delete(id));
    }
}