package loris.parfume.Configurations.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class NullPointerExceptionHandler {

    @ExceptionHandler(value = NullPointerException.class)
    public ResponseEntity<Object> exception(NullPointerException exception) {

        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
}