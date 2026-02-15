package spring.util.valid;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spring.exeption.ValidationException;

@Slf4j
@UtilityClass
public class UsernameAndPasswordValidation {
    public static final int MIN_PASSWORD_LENGTH = 5;
    public static final String AT_LEAST_ONE_DIGIT = ".*\\d.*";
    public static final String PASSWORD_MUST_CONTAIN_UPPERCASE_REGEX = ".*[A-Z].*";
    public static final String REGEX_LOWERCASE_LETTER = ".*[a-z].*";
    public static final String REGEX_SPECIAL_CHARACTER = ".*[!@#$%^&*()_+\\-{};':\"|,.<>/?].*";
    public static final int MIN_USERNAME_LENGTH = 5;


    public void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            log.warn("Password is empty");
            throw new ValidationException("Password is empty");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            log.warn("Password length less than 5");
            throw new ValidationException("Password length less than 5");
        }
        if (!password.matches(AT_LEAST_ONE_DIGIT)) {
            log.warn("Password must contain at least one digit");
            throw new ValidationException("Password must contain at least one digit");
        }
        if (!password.matches(PASSWORD_MUST_CONTAIN_UPPERCASE_REGEX)) {
            log.warn("Password must contain at least one uppercase letter");
            throw new ValidationException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(REGEX_LOWERCASE_LETTER)) {
            log.warn("Password must contain at least one lowercase letter");
            throw new ValidationException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(REGEX_SPECIAL_CHARACTER)) {
            throw new ValidationException("Password must contain at least one special character");
        }
    }

    public void validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            log.error("Login is empty");
            throw new ValidationException("Login is empty");
        }
        if (username.length() < MIN_USERNAME_LENGTH) {
            log.error("Username length less than 5");
            throw new ValidationException("Username length less than 5");
        }
    }
}
