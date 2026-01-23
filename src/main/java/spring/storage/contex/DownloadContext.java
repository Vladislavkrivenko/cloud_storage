package spring.storage.contex;

import lombok.Builder;
import lombok.Getter;
import spring.dto.ResourceType;

@Getter
@Builder
public class DownloadContext {
    private final String bucket;
    private final String object;
    private final String path;
    private final ResourceType resourceType;
}
