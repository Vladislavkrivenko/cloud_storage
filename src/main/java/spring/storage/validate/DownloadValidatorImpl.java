package spring.storage.validate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import spring.dto.ResourceType;
import spring.exeption.storageExeption.BadRequestException;
import spring.storage.contex.DownloadContext;
import spring.storage.interf.DownloadValidator;
import spring.storage.minio.MinioProperties;
import spring.storage.resolver.ResolvedPath;
import spring.storage.resolver.ResolvedType;
import spring.storage.resolver.StoragePathResolver;
import spring.util.StoragePathUtils;

@Component
@RequiredArgsConstructor
public class DownloadValidatorImpl implements DownloadValidator {
    private final StoragePathResolver storagePathResolver;
    private final MinioProperties minioProperties;

    @Override
    public DownloadContext validate(Integer userId, String path) {

        if (path == null || path.isBlank()) {
            throw new BadRequestException("Path must not be empty");
        }

        ResolvedPath resolved = storagePathResolver.resolve(userId, path);
        String base = StoragePathUtils.basePrefix(userId);

        if (resolved.getType() == ResolvedType.DIRECTORY
                && base.equals(resolved.getPrefix())) {
            throw new BadRequestException("Root directory cannot be downloaded");
        }

        if (resolved.getType() == ResolvedType.FILE) {
            return DownloadContext.builder()
                    .bucket(minioProperties.getBucket())
                    .resourceType(ResourceType.FILE)
                    .object(resolved.getObject())
                    .prefix(null)
                    .path(path)
                    .build();
        }

        return DownloadContext.builder()
                .bucket(minioProperties.getBucket())
                .resourceType(ResourceType.DIRECTORY)
                .object(null)
                .prefix(resolved.getPrefix())
                .path(path)
                .build();
    }
}