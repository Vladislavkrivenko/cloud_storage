package spring.storage.validate;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import spring.dto.ResourceType;
import spring.exeption.storageExeption.BadRequestException;
import spring.exeption.storageExeption.NotFoundException;
import spring.storage.contex.DownloadContext;
import spring.storage.interf.DownloadValidator;
import spring.storage.minio.MinioProperties;
import spring.util.StorageExistenceUtils;
import spring.util.StoragePathUtils;

@Component
@RequiredArgsConstructor
public class DownloadValidatorImpl implements DownloadValidator {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public DownloadContext validate(Integer userId, String path) {

        if (path == null || path.isBlank()) {
            throw new BadRequestException("Path must not be empty");
        }

        String bucket = minioProperties.getBucket();
        String object = StoragePathUtils.basePrefix(userId)
                + StoragePathUtils.normalizeFile(path);

        boolean fileExists = StorageExistenceUtils.fileExists(minioClient, bucket, object);
        boolean dirExists = StorageExistenceUtils.directoryExists(minioClient, bucket, object + "/");

        if (!fileExists && !dirExists) {
            throw new NotFoundException("Resource not found: " + path);
        }

        ResourceType type = fileExists
                ? ResourceType.FILE
                : ResourceType.DIRECTORY;

        return DownloadContext.builder()
                .bucket(bucket)
                .object(object)
                .path(path)
                .resourceType(type)
                .build();
    }
}
