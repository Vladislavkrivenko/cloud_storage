package spring.exeption.storageExeption;

public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(message);
    }
}
