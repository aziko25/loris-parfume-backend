package loris.parfume.Controllers.Items;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Filters.ItemFilters;
import loris.parfume.DTOs.Requests.Items.ItemsRequest;
import loris.parfume.Services.Items.ItemsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class ItemsController {

    private final ItemsService itemsService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestParam(value = "media") MultipartFile media,
                                    @RequestParam("item") String itemJson) throws JsonProcessingException {

        ItemsRequest itemsRequest = new ObjectMapper().readValue(itemJson, ItemsRequest.class);

        return new ResponseEntity<>(itemsService.create(media, itemsRequest), HttpStatus.CREATED);
    }

    @PostMapping("/all")
    public ResponseEntity<?> all(@RequestParam Integer page, @RequestBody(required = false) ItemFilters itemFilters) {

        return ResponseEntity.ok(itemsService.all(page, itemFilters));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        return ResponseEntity.ok(itemsService.getById(id));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestParam(value = "media") MultipartFile media,
                                    @RequestParam("item") String itemJson) throws JsonProcessingException {

        ItemsRequest itemsRequest = new ObjectMapper().readValue(itemJson, ItemsRequest.class);

        return ResponseEntity.ok(itemsService.update(id, media, itemsRequest));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return ResponseEntity.ok(itemsService.delete(id));
    }
}