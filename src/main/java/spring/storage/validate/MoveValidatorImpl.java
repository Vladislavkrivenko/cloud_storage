package spring.storage.validate;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import spring.dto.ResourceType;
import spring.exeption.storageExeption.BadRequestException;
import spring.exeption.storageExeption.ConflictException;
import spring.exeption.storageExeption.NotFoundException;
import spring.storage.interf.MoveValidator;
import spring.storage.contex.MoveContext;
import spring.storage.minio.MinioProperties;
import spring.util.StorageExistenceUtils;
import spring.util.StoragePathUtils;

@Component
@RequiredArgsConstructor
class MoveValidatorImpl implements MoveValidator {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public MoveContext validate(Integer userId, String from, String to) {

        if (from == null || to == null) {
            throw new BadRequestException("from and to must not be null");
        }

        if (from.equals(to)) {
            throw new BadRequestException("from and to cannot be the same");
        }

        String bucket = minioProperties.getBucket();
        String basePrefix = StoragePathUtils.basePrefix(userId);

        String fromFile = basePrefix + StoragePathUtils.normalizeFile(from);
        String fromDir = basePrefix + StoragePathUtils.normalizeDirectory(from);

        String toFile = basePrefix + StoragePathUtils.normalizeFile(to);
        String toDir = basePrefix + StoragePathUtils.normalizeDirectory(to);

        if (StorageExistenceUtils.fileExists(minioClient, bucket, fromFile)) {

            if (StorageExistenceUtils.fileExists(minioClient, bucket, toFile)) {
                throw new ConflictException("Target file already exists");
            }

            return MoveContext.builder()
                    .userId(userId)
                    .bucket(bucket)
                    .resourceType(ResourceType.FILE)
                    .sourceObject(fromFile)
                    .targetObject(toFile)
                    .fromPath(from)
                    .toPath(to)
                    .build();
        }

        if (StorageExistenceUtils.directoryExists(minioClient, bucket, fromDir)) {

            if (StorageExistenceUtils.directoryExists(minioClient, bucket, toDir)) {
                throw new ConflictException("Target directory already exists");
            }

            if (toDir.startsWith(fromDir)) {
                throw new BadRequestException("Cannot move directory into itself");
            }

            return MoveContext.builder()
                    .userId(userId)
                    .bucket(bucket)
                    .resourceType(ResourceType.DIRECTORY)
                    .sourcePrefix(fromDir)
                    .targetPrefix(toDir)
                    .fromPath(from)
                    .toPath(to)
                    .build();
        }

        throw new NotFoundException("Source not found: " + from);
    }
}
