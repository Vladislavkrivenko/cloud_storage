package spring.storage.validate;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import spring.exeption.storageExeption.BadRequestException;
import spring.exeption.storageExeption.ConflictException;
import spring.exeption.storageExeption.ForbiddenException;
import spring.storage.contex.CreateDirectoryContext;
import spring.storage.minio.MinioProperties;
import spring.util.StorageExistenceUtils;
import spring.util.StoragePathUtils;

@Component
@RequiredArgsConstructor
public class CreateDirectoryValidator {
    private final MinioProperties minioProperties;
    private final MinioClient minioClient;

    public CreateDirectoryContext validate(Integer userId, String path) {

        if (path == null || path.isBlank()) {
            throw new BadRequestException("Directory path must not be empty");
        }
        String bucket = minioProperties.getBucket();
        String basePrefix = StoragePathUtils.basePrefix(userId);
        String dirPrefix = basePrefix + StoragePathUtils.normalizeDirectory(path);

        if (dirPrefix.equals(basePrefix)) {
            throw new ForbiddenException("Root directory already exists");
        }
        String fileObject = dirPrefix.substring(0, dirPrefix.length() - 1);
        if (StorageExistenceUtils.fileExists(minioClient, bucket, fileObject)) {
            throw new ConflictException("File with same name already exists");
        }

        if (StorageExistenceUtils.directoryExists(minioClient, bucket, dirPrefix)) {
            throw new ConflictException("Directory already exists");
        }

        return CreateDirectoryContext.builder()
                .userId(userId)
                .bucket(bucket)
                .directoryPrefix(dirPrefix)
                .path(path)
                .build();
    }
}
