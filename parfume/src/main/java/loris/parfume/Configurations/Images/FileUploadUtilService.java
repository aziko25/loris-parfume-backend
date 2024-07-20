package loris.parfume.Configurations.Images;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class FileUploadUtilService {

    @Value("${imagesDir}")
    private String uploadDir;

    public void saveFile(String fileName, MultipartFile multipartFile) throws IOException {

        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {

            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException ioe) {

            throw new IOException("Could not save image file: " + fileName, ioe);
        }
    }

    public String handleMediaUpload(String name, MultipartFile media) {

        if (media.isEmpty()) {

            throw new IllegalArgumentException("Upload A File!");
        }

        String originalFileName = media.getOriginalFilename();

        assert originalFileName != null;
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();

        /*if (!fileExtension.matches(".png|.heic|.jpg|.jpeg|.mp4|.avi|.mov|.mkv")) {

            throw new IllegalArgumentException("Invalid file type! Please upload a .png, .heic, .jpg, .jpeg, " +
                    ".mp4, .avi, .mov or .mkv file.");
        }*/

        String fileName = name + fileExtension;

        try {

            saveFile(fileName, media);
        }
        catch (IOException e) {

            throw new IllegalArgumentException(e.getMessage());
        }

        return fileName;
    }

    public void handleMediaDeletion(String name) {

        File file = new File(uploadDir + "/" + name);

        boolean isDeleted = file.delete();

        System.out.println(isDeleted);
    }

    public void handleMultipleMediaDeletion(List<String> namesList) {

        for (String name : namesList) {

            File file = new File(uploadDir + "/" + name);

            boolean isDeleted = file.delete();

            System.out.println(isDeleted);
        }
    }
}