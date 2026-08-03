package api.back;

public class TransaccionNotFoundException extends RuntimeException {
    public TransaccionNotFoundException(String message) {
        super(message);
    }
}
