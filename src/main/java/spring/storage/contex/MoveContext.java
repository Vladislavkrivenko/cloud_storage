package spring.storage.contex;

import lombok.Builder;
import lombok.Getter;
import spring.dto.ResourceType;

@Builder
@Getter
public class MoveContext {
    private final Integer userId;
    private final String bucket;

    private final ResourceType resourceType;

    private final String sourceObject;
    private final String sourcePrefix;

    private final String targetObject;
    private final String targetPrefix;

    private final String fromPath;
    private final String toPath;
}
