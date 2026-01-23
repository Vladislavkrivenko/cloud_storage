package spring.storage.validate;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import spring.exeption.storageExeption.BadRequestException;
import spring.exeption.storageExeption.NotFoundException;
import spring.storage.contex.ListContext;
import spring.storage.interf.ListValidator;
import spring.storage.minio.MinioProperties;
import spring.util.StorageExistenceUtils;
import spring.util.StoragePathUtils;

@Component
@RequiredArgsConstructor
public class ListValidatorImpl implements ListValidator {
    private final MinioProperties minioProperties;
    private final MinioClient minioClient;

    @Override
    public ListContext validate(Integer userId, String path) {

        if (path == null || path.isBlank()) {
            throw new BadRequestException("Directory path must not be empty");
        }
        String bucket = minioProperties.getBucket();
        String base = StoragePathUtils.basePrefix(userId);
        String dirPrefix = base + StoragePathUtils.normalizeDirectory(path);

        if (!StorageExistenceUtils.directoryExists(minioClient, bucket, dirPrefix)) {
            throw new NotFoundException("Directory not found: " + path);
        }
        return ListContext.builder()
                .bucket(bucket)
                .prefix(dirPrefix)
                .basePrefix(base)
                .build();
    }
}
