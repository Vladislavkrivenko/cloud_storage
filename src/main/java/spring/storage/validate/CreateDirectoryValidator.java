package spring.storage.validate;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import spring.exeption.storageExeption.BadRequestException;
import spring.exeption.storageExeption.ConflictException;
import spring.exeption.storageExeption.ForbiddenException;
import spring.storage.contex.CreateDirectoryContext;
import spring.storage.minio.MinioProperties;
import spring.storage.resolver.ResolvedPath;
import spring.storage.resolver.ResolvedType;
import spring.storage.resolver.StoragePathResolver;
import spring.util.StorageExistenceUtils;
import spring.util.StoragePathUtils;

@Component
@RequiredArgsConstructor
public class CreateDirectoryValidator {
    private final MinioProperties minioProperties;
    private final MinioClient minioClient;
    private final StoragePathResolver storagePathResolver;

    public CreateDirectoryContext validate(Integer userId, String path) {

        if (path == null) {
            throw new BadRequestException("Directory path must not be empty");
        }

        String bucket = minioProperties.getBucket();
        String base = StoragePathUtils.basePrefix(userId);

        String dirPrefix = base + StoragePathUtils.normalizeDirectory(path);

        if (dirPrefix.equals(base)) {
            throw new ForbiddenException("Root directory already exists");
        }

        if (StorageExistenceUtils.hasAnyObjectWithPrefix(
                minioClient,
                bucket,
                dirPrefix
        )) {
            throw new ConflictException("Directory already exists");
        }

        String parentPath = StoragePathUtils.extractParentPath(dirPrefix, base);

        if (parentPath != null && !parentPath.isBlank()) {

            if (!parentPath.equals(base)) {

                ResolvedPath parent = storagePathResolver.resolve(userId, parentPath);

                if (parent.getType() != ResolvedType.DIRECTORY) {
                    throw new BadRequestException("Parent is not a directory");
                }
            }
        }
        return CreateDirectoryContext.builder()
                .userId(userId)
                .bucket(bucket)
                .directoryPrefix(dirPrefix)
                .path(path)
                .build();
    }
}