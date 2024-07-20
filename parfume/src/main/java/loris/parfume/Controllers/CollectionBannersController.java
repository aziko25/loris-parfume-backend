package loris.parfume.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Filters.BannerFilters;
import loris.parfume.DTOs.Requests.BannersRequest;
import loris.parfume.Services.CollectionBannersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/collection-banners")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class CollectionBannersController {

    private final CollectionBannersService collectionBannersService;

    //@Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestParam(value = "media") List<MultipartFile> media,
                                    @RequestParam("banner") String bannerJson) throws JsonProcessingException {

        BannersRequest bannersRequest = new ObjectMapper().readValue(bannerJson, BannersRequest.class);

        return new ResponseEntity<>(collectionBannersService.create(media, bannersRequest), HttpStatus.CREATED);
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

    //@Authorization(requiredRoles = {"ADMIN"})
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestParam(value = "media", required = false) List<MultipartFile> media,
                                    @RequestParam("banner") String bannerJson) throws JsonProcessingException {

        BannersRequest bannersRequest = new ObjectMapper().readValue(bannerJson, BannersRequest.class);

        return ResponseEntity.ok(collectionBannersService.update(id, media, bannersRequest));
    }

    //@Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return ResponseEntity.ok(collectionBannersService.delete(id));
    }
}