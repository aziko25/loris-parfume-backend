package loris.parfume.Controllers;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Requests.UsersRequest;
import loris.parfume.Services.UsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(maxAge = 3600)
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/all")
    public ResponseEntity<?> all(@RequestParam Integer page) {

        return ResponseEntity.ok(usersService.all(page));
    }

    @Authorization(requiredRoles = {"ADMIN", "USER"})
    @PostMapping("/me")
    public ResponseEntity<?> me() {

        return ResponseEntity.ok(usersService.me());
    }

    @Authorization(requiredRoles = {"ADMIN", "USER"})
    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody(required = false) UsersRequest usersRequest) {

        return ResponseEntity.ok(usersService.update(usersRequest));
    }
}