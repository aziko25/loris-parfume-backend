package loris.parfume.Controllers;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.JWT.Authorization;
import loris.parfume.DTOs.Requests.BranchesRequest;
import loris.parfume.DTOs.Requests.NearestBranchRequest;
import loris.parfume.Services.BranchesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class BranchesController {

    private final BranchesService branchesService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody BranchesRequest branchesRequest) {

        return new ResponseEntity<>(branchesService.create(branchesRequest), HttpStatus.CREATED);
    }

    @PostMapping("/all")
    public ResponseEntity<?> all(@RequestParam(required = false) String search) {

        return ResponseEntity.ok(branchesService.all(search));
    }

    /*@PostMapping("/nearest")
    public ResponseEntity<?> nearest(@RequestBody NearestBranchRequest nearestBranchRequest) {

        return ResponseEntity.ok(branchesService.getNearestBranch(nearestBranchRequest));
    }*/

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        return ResponseEntity.ok(branchesService.getById(id));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody(required = false) BranchesRequest branchesRequest) {

        return ResponseEntity.ok(branchesService.update(id, branchesRequest));
    }

    @Authorization(requiredRoles = {"ADMIN"})
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return ResponseEntity.ok(branchesService.delete(id));
    }
}