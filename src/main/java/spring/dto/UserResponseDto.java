package spring.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User response")
public record UserResponseDto(
        @Schema(description = "Username", example = "user_1")
        String username) {
}
