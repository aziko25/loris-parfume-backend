package loris.parfume.Configurations.Exceptions;

import org.hibernate.PropertyValueException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ProjectGlobalExceptionHandler {

    @ExceptionHandler(PropertyValueException.class)
    public ResponseEntity<String> handlePropertyValueException(PropertyValueException ex) {

        String message = "A required field is missing or null: " + ex.getPropertyName();

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }
}