package loris.parfume.Controllers;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Requests.BlogsRequest;
import loris.parfume.Services.BlogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/blogs")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class BlogsController {

    private final BlogService blogService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody BlogsRequest blogsRequest) throws IOException {

        return new ResponseEntity<>(blogService.create(blogsRequest), HttpStatus.CREATED);
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
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody BlogsRequest blogsRequest) throws IOException {

        return new ResponseEntity<>(blogService.update(id, blogsRequest), HttpStatus.OK);
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return ResponseEntity.ok(blogService.delete(id));
    }
}