package spring.dto;

public record ResourcesDto(
        String name,
        String path,
        ResourceType type,
        Long size) {
}
