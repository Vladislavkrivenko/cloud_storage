package spring.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "File or directory resource")
public record ResourceDto(
        @Schema(description = "Resource name", example = "file.txt")
        String name,
        @Schema(description = "Parent directory path", example = "folder1/folder2")
        String path,
        @Schema(description = "Resource type: FILE or DIRECTORY", example = "FILE")
        ResourceType type,
        @Schema(description = "File size in bytes(null for directories)", example = "123")
        Long size) {
}
