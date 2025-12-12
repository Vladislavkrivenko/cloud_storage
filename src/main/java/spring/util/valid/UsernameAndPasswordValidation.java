package spring.util.valid;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class UsernameAndPasswordValidation {
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final String AT_LEAST_ONE_DIGIT = ".*\\d.*";
    public static final String PASSWORD_MUST_CONTAIN_UPPERCASE_REGEX = ".*[A-Z].*";
    public static final String REGEX_LOWERCASE_LETTER = ".*[a-z].*";
    public static final String REGEX_SPECIAL_CHARACTER = ".*[!@#$%^&*()_+\\-{};':\"|,.<>/?].*";
    public static final int MIN_USERNAME_LENGTH = 3;


    public void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            log.error("Password is empty");
            throw new IllegalArgumentException("Password is empty");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            log.error("Password length less than 8");
            throw new IllegalArgumentException("Password length less than 8");
        }
        if (!password.matches(AT_LEAST_ONE_DIGIT)) {
            log.error("Password must contain at least one digit");
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        if (!password.matches(PASSWORD_MUST_CONTAIN_UPPERCASE_REGEX)) {
            log.error("Password must contain at least one uppercase letter");
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(REGEX_LOWERCASE_LETTER)) {
            log.error("Password must contain at least one lowercase letter");
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(REGEX_SPECIAL_CHARACTER)) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
    }

    public void validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            log.error("Login is empty");
            throw new IllegalArgumentException("Login is empty");
        }
        if (username.length() < MIN_USERNAME_LENGTH) {
            log.error("Username length less than 3");
            throw new IllegalArgumentException("Username length less than 3");
        }
    }
}
