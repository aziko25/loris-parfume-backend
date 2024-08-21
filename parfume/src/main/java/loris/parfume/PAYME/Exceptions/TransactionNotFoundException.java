package loris.parfume.PAYME.Exceptions;

public class TransactionNotFoundException extends Exception {

    private int code;
    private String data;

    public TransactionNotFoundException(String message, int code, String data) {
        super(message);
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public String getData() {
        return data;
    }
}
