package spring.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error response")
public record ApiError(
        @Schema(description = "Error message", example = "Resource not found")
        String message) {

}
