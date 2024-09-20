package loris.parfume.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class MediaController {

    @Value("${imagesDir}")
    private String imagesDir;

    @GetMapping(value = "/{mediaName}")
    public @ResponseBody ResponseEntity<Resource> getImageName(@PathVariable String mediaName) {

        try {

            File file = new File(imagesDir + "/" + mediaName);

            if (!file.exists()) {

                return ResponseEntity.notFound().build();
            }

            Path filePath = file.toPath();
            String contentType = Files.probeContentType(filePath);

            if (contentType == null) {

                contentType = "application/octet-stream";
            }

            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    //.header("Cache-Control", "public, max-age=3153600")
                    .body(resource);

        } catch (Exception e) {

            return ResponseEntity.status(500).body(null);
        }
    }
}