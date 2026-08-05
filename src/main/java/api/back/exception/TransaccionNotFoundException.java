package api.back.exception;

public class TransaccionNotFoundException extends RuntimeException {
    public TransaccionNotFoundException(String message) {
        super(message);
    }
}
