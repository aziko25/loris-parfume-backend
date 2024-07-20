package loris.parfume.Controllers;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.Services.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class WishlistController {

    private final WishlistService wishlistService;

    @Authorization(requiredRoles = {"ADMIN", "USER"})
    @PostMapping("/add/{id}")
    public ResponseEntity<?> add(@PathVariable Long id) {

        return ResponseEntity.ok(wishlistService.add(id));
    }

    @Authorization(requiredRoles = {"ADMIN", "USER"})
    @PostMapping("/all")
    public ResponseEntity<?> all(@RequestParam Integer page) {

        return ResponseEntity.ok(wishlistService.all(page));
    }

    @Authorization(requiredRoles = {"ADMIN", "USER"})
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<?> remove(@PathVariable Long id) {

        return ResponseEntity.ok(wishlistService.remove(id));
    }

    @Authorization(requiredRoles = {"ADMIN", "USER"})
    @DeleteMapping("/clear")
    public ResponseEntity<?> clear() {

        return ResponseEntity.ok(wishlistService.clear());
    }
}