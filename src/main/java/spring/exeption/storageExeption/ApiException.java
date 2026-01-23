package spring.exeption.storageExeption;

public abstract class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
