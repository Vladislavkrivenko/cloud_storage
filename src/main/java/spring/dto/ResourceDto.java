package spring.dto;

public record ResourceDto(
        String name,
        String path,
        ResourceType type,
        Long size) {
}
