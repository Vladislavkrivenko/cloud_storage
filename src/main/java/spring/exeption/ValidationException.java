package spring.exeption;

import spring.exeption.storageExeption.BadRequestException;

public class ValidationException extends BadRequestException {

    public ValidationException(String message) {
        super(message);
    }
}
