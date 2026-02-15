package spring.storage.resolver;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResolvedPath {
    private final ResolvedType type;
    private final String object;
    private final String prefix;
}
