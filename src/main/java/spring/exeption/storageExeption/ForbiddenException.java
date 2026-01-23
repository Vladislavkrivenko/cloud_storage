package spring.exeption.storageExeption;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(message);
    }
}
