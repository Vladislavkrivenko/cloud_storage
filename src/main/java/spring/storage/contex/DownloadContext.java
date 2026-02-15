package spring.storage.contex;

import lombok.Builder;
import lombok.Getter;
import spring.dto.ResourceType;

@Getter
@Builder
public class DownloadContext {
    private final String bucket;
    private final ResourceType resourceType;
    private final String object;
    private final String prefix;
    private final String path;
}