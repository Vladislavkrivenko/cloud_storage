package spring.storage.validate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import spring.exeption.storageExeption.BadRequestException;
import spring.storage.contex.ListContext;
import spring.storage.interf.ListValidator;
import spring.storage.minio.MinioProperties;
import spring.storage.resolver.ResolvedPath;
import spring.storage.resolver.ResolvedType;
import spring.storage.resolver.StoragePathResolver;
import spring.util.StoragePathUtils;

@Component
@RequiredArgsConstructor
public class ListValidatorImpl implements ListValidator {
    private final MinioProperties minioProperties;
    private final StoragePathResolver storagePathResolver;

    @Override
    public ListContext validate(Integer userId, String path) {

        String bucket = minioProperties.getBucket();
        String base = StoragePathUtils.basePrefix(userId);

        if (path == null || path.isBlank()) {
            return ListContext.builder()
                    .bucket(bucket)
                    .prefix(base)
                    .basePrefix(base)
                    .build();
        }

        ResolvedPath resolved = storagePathResolver.resolve(userId, path);

        if (resolved.getType() != ResolvedType.DIRECTORY) {
            throw new BadRequestException("Path is not a directory: " + path);
        }

        return ListContext.builder()
                .bucket(bucket)
                .prefix(resolved.getPrefix())
                .basePrefix(base)
                .build();
    }
}