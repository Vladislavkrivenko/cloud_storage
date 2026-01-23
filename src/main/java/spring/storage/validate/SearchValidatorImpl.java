package spring.storage.validate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import spring.exeption.storageExeption.BadRequestException;
import spring.storage.interf.SearchValidator;
import spring.storage.contex.SearchContext;
import spring.storage.minio.MinioProperties;
import spring.util.StoragePathUtils;

@Component
@RequiredArgsConstructor
public class SearchValidatorImpl implements SearchValidator {

    private final MinioProperties minioProperties;

    @Override
    public SearchContext validate(Integer userId, String query) {
        if (query == null || query.isBlank()) {
            throw new BadRequestException("Search query must not be empty");
        }
        return SearchContext.builder()
                .userId(userId)
                .bucket(minioProperties.getBucket())
                .basePrefix(StoragePathUtils.basePrefix(userId))
                .query(query)
                .build();
    }
}
