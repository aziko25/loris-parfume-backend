package loris.parfume.Configurations.Images;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 s3client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file) throws IOException {

        ObjectMetadata metadata = new ObjectMetadata();

        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        metadata.setCacheControl("public, max-age=2592000");

        String fileExtension = Objects.requireNonNull
                (file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".")).toLowerCase();

        String keyName = System.currentTimeMillis() + fileExtension;

        s3client.putObject(bucketName, keyName, file.getInputStream(), metadata);

        return s3client.getUrl(bucketName, keyName).toString();
    }

    public void deleteFile(String imageUrl) {

        //String keyName = imageUrl.substring(imageUrl.indexOf(bucketName) + bucketName.length() + 1);

        //s3client.deleteObject(bucketName, keyName);
    }
}