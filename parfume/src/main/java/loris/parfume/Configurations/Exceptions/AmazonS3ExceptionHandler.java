package loris.parfume.Configurations.Exceptions;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AmazonS3ExceptionHandler {

    @ExceptionHandler(value = AmazonS3Exception.class)
    public ResponseEntity<Object> exception(AmazonS3Exception exception) {

        if ("NoSuchKey".equals(exception.getErrorCode())) {

            return new ResponseEntity<>("File not found in Amazon S3", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}