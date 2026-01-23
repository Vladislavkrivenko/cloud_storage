package spring.storage.contex;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ListContext {
    private final String bucket;
    private final String prefix;
    private final String basePrefix;
}
