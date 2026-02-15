package spring.storage.validate;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import spring.dto.ResourceType;
import spring.exeption.storageExeption.BadRequestException;
import spring.storage.contex.MoveContext;
import spring.storage.interf.MoveValidator;
import spring.storage.minio.MinioProperties;
import spring.storage.resolver.ResolvedPath;
import spring.storage.resolver.ResolvedType;
import spring.storage.resolver.StoragePathResolver;
import spring.util.StorageExistenceUtils;
import spring.util.StoragePathUtils;

@Slf4j
@Component
@RequiredArgsConstructor
class MoveValidatorImpl implements MoveValidator {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final StoragePathResolver storagePathResolver;

    @Override
    public MoveContext validate(Integer userId, String from, String to) {

        log.debug("MOVE request: userId={}, from='{}', to='{}'", userId, from, to);

        if (from == null || to == null) {
            throw new BadRequestException("from and to must not be null");
        }

        if (from.equals(to)) {
            throw new BadRequestException("from and to cannot be the same");
        }

        String bucket = minioProperties.getBucket();
        String base = StoragePathUtils.basePrefix(userId);

        ResolvedPath source = storagePathResolver.resolve(userId, from);

        if (source.getType() == ResolvedType.DIRECTORY) {

            String fromPrefix = source.getPrefix();
            String toDirPrefix = base + StoragePathUtils.normalizeDirectory(to);

            log.debug("DIRECTORY move detected. fromPrefix='{}'", fromPrefix);

            if (StorageExistenceUtils.hasAnyObjectWithPrefix(
                    minioClient,
                    bucket,
                    toDirPrefix
            )) {

                String dirName = StoragePathUtils.extractName(fromPrefix);
                String targetPrefix = toDirPrefix + dirName + "/";

                if (targetPrefix.startsWith(fromPrefix)) {
                    throw new BadRequestException("Cannot move directory into itself");
                }

                log.debug("Move DIRECTORY INTO directory. targetPrefix='{}'", targetPrefix);

                return MoveContext.builder()
                        .userId(userId)
                        .bucket(bucket)
                        .resourceType(ResourceType.DIRECTORY)
                        .sourcePrefix(fromPrefix)
                        .targetPrefix(targetPrefix)
                        .build();
            }

            log.debug("Rename DIRECTORY. targetPrefix='{}'", toDirPrefix);

            return MoveContext.builder()
                    .userId(userId)
                    .bucket(bucket)
                    .resourceType(ResourceType.DIRECTORY)
                    .sourcePrefix(fromPrefix)
                    .targetPrefix(toDirPrefix)
                    .build();
        }

        if (source.getType() == ResolvedType.FILE) {

            String fromObject = source.getObject();
            String fileName = StoragePathUtils.extractName(fromObject);

            log.debug("FILE move detected. fromObject='{}'", fromObject);

            String toDirPrefix = base + StoragePathUtils.normalizeDirectory(to);

            if (StorageExistenceUtils.hasAnyObjectWithPrefix(
                    minioClient,
                    bucket,
                    toDirPrefix
            )) {

                String targetObject = toDirPrefix + fileName;

                log.debug("Move FILE INTO directory. targetObject='{}'", targetObject);

                return MoveContext.builder()
                        .userId(userId)
                        .bucket(bucket)
                        .resourceType(ResourceType.FILE)
                        .sourceObject(fromObject)
                        .targetObject(targetObject)
                        .build();
            }

            String toFile = base + StoragePathUtils.normalizeFile(to);

            log.debug("Rename FILE. targetObject='{}'", toFile);

            return MoveContext.builder()
                    .userId(userId)
                    .bucket(bucket)
                    .resourceType(ResourceType.FILE)
                    .sourceObject(fromObject)
                    .targetObject(toFile)
                    .build();
        }

        throw new IllegalStateException("Unsupported resource type");
    }
}