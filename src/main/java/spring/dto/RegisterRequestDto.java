package spring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration request")
public record RegisterRequestDto(

        @NotBlank
        @Size(min = 5)
        @Schema(example = "user_1")
        String username,

        @NotBlank
        @Size(min = 5)
        @Pattern(
                regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-{};':\"|,.<>/?]).+$",
                message = "Password must contain digit, upper, lower and special character"
        )
        @Schema(example = "Password1!")
        String password
) {
}
