package loris.parfume.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Requests.BlogsRequest;
import loris.parfume.Services.BlogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/blogs")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class BlogsController {

    private final BlogService blogService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestParam(value = "media") MultipartFile image,
                                    @RequestParam("blog") String blogJson) throws IOException {

        BlogsRequest blogsRequest = new ObjectMapper().readValue(blogJson, BlogsRequest.class);

        return new ResponseEntity<>(blogService.create(image, blogsRequest), HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<?> all(@RequestParam(required = false) Boolean isActive) {

        return ResponseEntity.ok(blogService.all(isActive));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        return ResponseEntity.ok(blogService.getById(id));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return ResponseEntity.ok(blogService.delete(id));
    }
}