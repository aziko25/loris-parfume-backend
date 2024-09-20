package loris.parfume.Configurations.Images;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileUploadUtilService {

    private final S3Service s3Service;

    public String handleMediaUpload(MultipartFile media) throws IOException {

        if (media.isEmpty()) {

            throw new IllegalArgumentException("Upload A File!");
        }

        return s3Service.uploadFile(media);
    }

    public void handleMediaDeletion(String name) {

        s3Service.deleteFile(name);
    }

    public void handleMultipleMediaDeletion(List<String> namesList) {

        for (String name : namesList) {

            s3Service.deleteFile(name);
        }
    }
}