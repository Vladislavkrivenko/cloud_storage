package spring.storage.validate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import spring.dto.ResourceType;
import spring.exeption.storageExeption.BadRequestException;
import spring.storage.contex.RemoveContext;
import spring.storage.interf.RemoveValidator;
import spring.storage.minio.MinioProperties;
import spring.storage.resolver.ResolvedPath;
import spring.storage.resolver.ResolvedType;
import spring.storage.resolver.StoragePathResolver;
import spring.util.StoragePathUtils;

@Component
@RequiredArgsConstructor
public class RemoveValidatorImpl implements RemoveValidator {

    private final MinioProperties minioProperties;
    private final StoragePathResolver storagePathResolver;

    @Override
    public RemoveContext validate(Integer userId, String path) {

        ResolvedPath resolved = storagePathResolver.resolve(userId, path);
        String base = StoragePathUtils.basePrefix(userId);

        if (resolved.getType() == ResolvedType.DIRECTORY
                && base.equals(resolved.getPrefix())) {
            throw new BadRequestException("Root directory cannot be removed");
        }

        if (resolved.getType() == ResolvedType.FILE) {
            return RemoveContext.builder()
                    .userId(userId)
                    .bucket(minioProperties.getBucket())
                    .resourceType(ResourceType.FILE)
                    .object(resolved.getObject())
                    .prefix(null)
                    .path(path)
                    .build();
        }

        return RemoveContext.builder()
                .userId(userId)
                .bucket(minioProperties.getBucket())
                .resourceType(ResourceType.DIRECTORY)
                .object(null)
                .prefix(resolved.getPrefix())
                .path(path)
                .build();
    }
}