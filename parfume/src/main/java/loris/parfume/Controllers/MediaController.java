package loris.parfume.Controllers;

import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.Images.FileUploadUtilService;
import loris.parfume.Configurations.JWT.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media")
@CrossOrigin(maxAge = 3600)
public class MediaController {

    private final FileUploadUtilService fileUploadUtilService;

    @Authorization(requiredRoles = {"ADMIN"})
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {

        return ResponseEntity.ok(fileUploadUtilService.handleMediaUpload(file));
    }
}