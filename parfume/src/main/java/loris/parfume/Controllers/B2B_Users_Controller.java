package loris.parfume.Controllers;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.Models.B2B_Users;
import loris.parfume.Services.B2B_Users_Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/b2b")
@CrossOrigin(maxAge = 3600)
@RequiredArgsConstructor
public class B2B_Users_Controller {

    private final B2B_Users_Service b2BUsersService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody B2B_Users b2bUser) {

        return new ResponseEntity<>(b2BUsersService.create(b2bUser), HttpStatus.CREATED);
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @GetMapping("/all")
    public ResponseEntity<?> getAll() {

        return ResponseEntity.ok(b2BUsersService.all());
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        return ResponseEntity.ok(b2BUsersService.getById(id));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return ResponseEntity.ok(b2BUsersService.delete(id));
    }
}