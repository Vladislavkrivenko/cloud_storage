package spring.storage.contex;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchContext {
    private final Integer userId;
    private final String bucket;
    private final String basePrefix;
    private final String query;
}
