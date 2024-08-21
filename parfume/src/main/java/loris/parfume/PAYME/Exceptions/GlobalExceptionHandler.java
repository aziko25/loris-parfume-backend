package loris.parfume.PAYME.Exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnableCompleteException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ErrorResponseWrapper handleUnableCompleteException(UnableCompleteException e) {

        ErrorResponse errorResponse = new ErrorResponse("2.0", e.getCode(), e.getMessage(), e.getData());

        return new ErrorResponseWrapper(errorResponse);
    }

    @ExceptionHandler(WrongAmountException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ErrorResponseWrapper handleWrongAmountException(WrongAmountException e) {

        ErrorResponse errorResponse = new ErrorResponse("2.0", e.getCode(), e.getMessage(), e.getData());

        return new ErrorResponseWrapper(errorResponse);
    }

    @ExceptionHandler(OrderNotExistsException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ErrorResponseWrapper handleOrderNotExistException(OrderNotExistsException e) {

        ErrorResponse errorResponse = new ErrorResponse("2.0", e.getCode(), e.getMessage(), e.getData());

        return new ErrorResponseWrapper(errorResponse);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ErrorResponseWrapper handleTransactionNotFoundException(TransactionNotFoundException e) {

        ErrorResponse errorResponse = new ErrorResponse("2.0", e.getCode(), e.getMessage(), e.getData());

        return new ErrorResponseWrapper(errorResponse);
    }

    @ExceptionHandler(UnableCancelTransactionException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ErrorResponseWrapper handleUnableCancelTransactionException(UnableCancelTransactionException e) {

        ErrorResponse errorResponse = new ErrorResponse("2.0", e.getCode(), e.getMessage(), e.getData());

        return new ErrorResponseWrapper(errorResponse);
    }

    @Getter
    static class ErrorResponseWrapper {

        private final ErrorResponse error;

        public ErrorResponseWrapper(ErrorResponse error) {
            this.error = error;
        }

    }

    @Getter
    static class ErrorResponse {

        private final String jsonrpc;
        private final int code;
        private final String message;
        private final String data;

        public ErrorResponse(String jsonrpc, int code, String message, String data) {
            this.jsonrpc = jsonrpc;
            this.code = code;
            this.message = message;
            this.data = data;
        }

    }
}