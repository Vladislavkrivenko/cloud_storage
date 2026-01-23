package spring.storage.contex;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateDirectoryContext {

    private final Integer userId;
    private final String bucket;
    private final String directoryPrefix;
    private final String path;
}
