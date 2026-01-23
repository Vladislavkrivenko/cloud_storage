package spring.storage.validate;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import spring.dto.ResourceType;
import spring.exeption.storageExeption.BadRequestException;
import spring.exeption.storageExeption.NotFoundException;
import spring.storage.contex.RemoveContext;
import spring.storage.interf.RemoveValidator;
import spring.storage.minio.MinioProperties;
import spring.util.StorageExistenceUtils;
import spring.util.StoragePathUtils;

@Component
@RequiredArgsConstructor
public class RemoveValidatorImpl implements RemoveValidator {
    private final MinioProperties minioProperties;
    private final MinioClient minioClient;

    @Override
    public RemoveContext validate(Integer userId, String path) {

        if (path == null || path.isBlank()) {
            throw new BadRequestException("Path must not be empty");
        }
        String bucket = minioProperties.getBucket();
        String base = StoragePathUtils.basePrefix(userId);

        String file = base + StoragePathUtils.normalizeFile(path);
        String dir = base + StoragePathUtils.normalizeDirectory(path);

        if (StorageExistenceUtils.fileExists(minioClient, bucket, file)) {
            return RemoveContext.builder()
                    .userId(userId)
                    .bucket(bucket)
                    .resourceType(ResourceType.FILE)
                    .object(file)
                    .path(path)
                    .build();
        }
        if (StorageExistenceUtils.directoryExists(minioClient, bucket, dir)) {
            return RemoveContext.builder()
                    .userId(userId)
                    .bucket(bucket)
                    .resourceType(ResourceType.DIRECTORY)
                    .object(dir)
                    .path(path)
                    .build();
        }

        throw new NotFoundException("Resource not found: " + path);
    }
}
