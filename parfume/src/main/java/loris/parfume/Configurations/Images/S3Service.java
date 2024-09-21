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

    @Bean
    public void updateMetadataForAllObjects() {
        // Получаем список всех объектов в bucket
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName);
        ListObjectsV2Result result;

        do {
            result = s3client.listObjectsV2(req);
            System.out.println(result.getObjectSummaries());
            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                String key = objectSummary.getKey();

                // Получаем существующий объект
                S3Object s3Object = s3client.getObject(bucketName, key);

                // Установка новых метаданных
                ObjectMetadata newMetadata = new ObjectMetadata();
                newMetadata.setContentType(s3Object.getObjectMetadata().getContentType());
                newMetadata.setCacheControl("public, max-age=2592000");

                // Копируем объект с новыми метаданными
                CopyObjectRequest copyRequest = new CopyObjectRequest(bucketName, key, bucketName, key)
                        .withNewObjectMetadata(newMetadata);

                // Копируем объект заново с новыми метаданными
                s3client.copyObject(copyRequest);
            }
            // Устанавливаем продолжение для списка объектов (если их много)
            String token = result.getNextContinuationToken();
            req.setContinuationToken(token);
        } while (result.isTruncated());
    }


    public void deleteFile(String imageUrl) {

        //String keyName = imageUrl.substring(imageUrl.indexOf(bucketName) + bucketName.length() + 1);

        //s3client.deleteObject(bucketName, keyName);
    }
}