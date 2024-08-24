package loris.parfume.Controllers.Orders;

import lombok.RequiredArgsConstructor;
import loris.parfume.UZUM.UzumService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/uzum")
@CrossOrigin(maxAge = 3600)
@RequiredArgsConstructor
public class UzumOrdersController {

    private final UzumService uzumService;

    private static final String BASIC_PREFIX = "Basic ";
    //private static final String EXPECTED_CREDENTIALS = "dXp1bTpiYW5r";
    private static final String EXPECTED_CREDENTIALS = "bG9yaXNwYXJmdW1lOlN0cm9uZ1Bhc3N3b3Jk";

    private boolean isAuthorized(String authorizationHeader) {

        if (authorizationHeader != null && authorizationHeader.startsWith(BASIC_PREFIX)) {

            String base64Credentials = authorizationHeader.substring(BASIC_PREFIX.length());

            return !EXPECTED_CREDENTIALS.equals(base64Credentials);
        }

        return true;
    }

    @PostMapping("/check")
    public ResponseEntity<?> check(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                   @RequestBody Map<String, Object> request) {

        if (isAuthorized(authorizationHeader)) {

            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(uzumService.check(request));
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                    @RequestBody Map<String, Object> request) {

        if (isAuthorized(authorizationHeader)) {

            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(uzumService.create(request));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                     @RequestBody Map<String, Object> request) {

        if (isAuthorized(authorizationHeader)) {

            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(uzumService.confirm(request));
    }

    @PostMapping("/reverse")
    public ResponseEntity<?> reverse(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                     @RequestBody Map<String, Object> request) {

        if (isAuthorized(authorizationHeader)) {

            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(uzumService.reverse(request));
    }

    @PostMapping("/status")
    public ResponseEntity<?> status(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                    @RequestBody Map<String, Object> request) {

        if (isAuthorized(authorizationHeader)) {

            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(uzumService.status(request));
    }
}