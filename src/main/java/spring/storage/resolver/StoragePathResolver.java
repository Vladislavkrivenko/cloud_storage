package spring.storage.resolver;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import spring.exeption.storageExeption.BadRequestException;
import spring.exeption.storageExeption.NotFoundException;
import spring.storage.minio.MinioProperties;
import spring.util.StorageExistenceUtils;
import spring.util.StoragePathUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoragePathResolver {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public ResolvedPath resolve(Integer userId, String path) {

        log.debug("RESOLVE path='{}'", path);

        if (path == null) {
            throw new BadRequestException("Path must not be null");
        }

        String bucket = minioProperties.getBucket();
        String base = StoragePathUtils.basePrefix(userId);

        if (path.isBlank()) {
            log.debug("RESOLVED AS ROOT DIRECTORY: '{}'", base);
            return ResolvedPath.builder()
                    .type(ResolvedType.DIRECTORY)
                    .prefix(base)
                    .build();
        }

        if (!path.endsWith("/")) {
            String file = base + StoragePathUtils.normalizeFile(path);

            if (StorageExistenceUtils.fileExists(minioClient, bucket, file)) {
                log.debug("RESOLVED AS FILE: '{}'", file);
                return ResolvedPath.builder()
                        .type(ResolvedType.FILE)
                        .object(file)
                        .build();
            }
        }

        String dir = base + StoragePathUtils.normalizeDirectory(path);

        if (StorageExistenceUtils.hasAnyObjectWithPrefix(minioClient, bucket, dir)) {
            log.debug("RESOLVED AS DIRECTORY: '{}'", dir);
            return ResolvedPath.builder()
                    .type(ResolvedType.DIRECTORY)
                    .prefix(dir)
                    .build();
        }

        throw new NotFoundException("Resource not found: " + path);
    }
}