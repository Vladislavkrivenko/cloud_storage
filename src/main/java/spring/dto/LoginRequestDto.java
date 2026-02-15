package spring.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User login request")
public record LoginRequestDto(
        @Schema(description = "Username", example = "user_1")
        String username,
        @Schema(description = "Password", example = "Password1!")
        String password) {
}
