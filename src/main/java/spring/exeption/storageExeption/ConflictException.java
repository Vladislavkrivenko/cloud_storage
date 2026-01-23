package spring.exeption.storageExeption;

public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(message);
    }
}
