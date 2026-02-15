package spring.storage.service;

import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import spring.dto.ResourceDto;
import spring.dto.ResourceType;
import spring.exeption.storageExeption.ApiException;
import spring.storage.contex.CreateDirectoryContext;
import spring.storage.contex.MoveContext;
import spring.storage.contex.RemoveContext;
import spring.storage.contex.UploadContext;
import spring.storage.interf.MoveValidator;
import spring.storage.interf.RemoveValidator;
import spring.storage.interf.UploadValidator;
import spring.storage.validate.CreateDirectoryValidator;
import spring.util.StoragePathUtils;

import java.io.ByteArrayInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageWriteService {
    private final MinioClient minioClient;
    private final MoveValidator moveValidator;
    private final RemoveValidator removeValidator;
    private final UploadValidator uploadValidator;
    private final CreateDirectoryValidator createDirectoryValidator;

    public ResourceDto upload(Integer userId, String targetPath, MultipartFile file) {
        UploadContext ctx = uploadValidator.validate(userId, targetPath, file);

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(ctx.getBucket())
                            .object(ctx.getObjectName())
                            .stream(ctx.getInputStream(), ctx.getFileSize(), -1)
                            .contentType(ctx.getContentType())
                            .build()
            );

            return new ResourceDto(
                    ctx.getName(),
                    ctx.getPath(),
                    ResourceType.FILE,
                    ctx.getFileSize()
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to upload file: " + ctx.getObjectName(), e
            );
        }
    }

    public ResourceDto createDirectory(Integer userId, String path) {

        CreateDirectoryContext ctx =
                createDirectoryValidator.validate(userId, path);

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(ctx.getBucket())
                            .object(ctx.getDirectoryPrefix())
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build()
            );

            return new ResourceDto(
                    StoragePathUtils.extractName(ctx.getDirectoryPrefix()) + "/",//resolver
                    ctx.getPath(),
                    ResourceType.DIRECTORY,
                    null
            );

        } catch (Exception e) {
            throw new IllegalStateException("Failed to create directory", e);
        }
    }

    public void remove(Integer userId, String path) {
        RemoveContext ctx = removeValidator.validate(userId, path);

        try {
            if (ctx.getResourceType() == ResourceType.FILE) {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(ctx.getBucket())
                                .object(ctx.getObject())
                                .build()
                );
                return;
            }

            Iterable<Result<Item>> iterable = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(ctx.getBucket())
                            .prefix(ctx.getPrefix())
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : iterable) {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(ctx.getBucket())
                                .object(result.get().objectName())
                                .build()
                );
            }

            log.debug("REMOVE DIRECTORY: '{}'", ctx.getPrefix());

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(ctx.getBucket())
                            .object(ctx.getPrefix())
                            .build()
            );
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to remove: " + path, e);
        }
    }

    public ResourceDto move(Integer userId, String from, String to) {
        MoveContext ctx = moveValidator.validate(userId, from, to);

        try {
            if (ctx.getResourceType() == ResourceType.FILE) {
                moveFile(ctx);

                return new ResourceDto(
                        StoragePathUtils.extractName(ctx.getTargetObject()),
                        StoragePathUtils.extractParentPath(
                                ctx.getTargetObject(),
                                StoragePathUtils.basePrefix(userId)
                        ),
                        ResourceType.FILE,
                        null
                );
            }

            if (ctx.getResourceType() == ResourceType.DIRECTORY) {
                moveDirectory(ctx);

                return new ResourceDto(
                        StoragePathUtils.extractName(ctx.getTargetPrefix()) + "/",
                        StoragePathUtils.extractParentPath(
                                ctx.getTargetPrefix(),
                                StoragePathUtils.basePrefix(userId)
                        ),
                        ResourceType.DIRECTORY,
                        null
                );
            }
            throw new IllegalStateException("Invalid resource type: " + ctx.getResourceType());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to move file: " + from + " to " + to, e);
        }
    }


    @SneakyThrows
    private void moveFile(MoveContext ctx) {

        log.debug(
                "MOVE FILE: '{}' -> '{}'",
                ctx.getSourceObject(),
                ctx.getTargetObject()
        );

        minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket(ctx.getBucket())
                        .object(ctx.getTargetObject())
                        .source(
                                CopySource.builder()
                                        .bucket(ctx.getBucket())
                                        .object(ctx.getSourceObject())
                                        .build()
                        )
                        .build()
        );

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(ctx.getBucket())
                        .object(ctx.getSourceObject())
                        .build()
        );

        log.debug("MOVE FILE DONE");
    }

    @SneakyThrows
    private void moveDirectory(MoveContext ctx) {

        log.debug(
                "MOVE DIRECTORY: '{}' -> '{}'",
                ctx.getSourcePrefix(),
                ctx.getTargetPrefix()
        );

        Iterable<Result<Item>> iterable = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(ctx.getBucket())
                        .prefix(ctx.getSourcePrefix())
                        .recursive(true)
                        .build()
        );

        for (Result<Item> result : iterable) {
            Item item = result.get();

            String relativePath =
                    item.objectName().substring(ctx.getSourcePrefix().length());

            String targetObject =
                    ctx.getTargetPrefix() + relativePath;

            log.debug(
                    "COPY: '{}' -> '{}'",
                    item.objectName(),
                    targetObject
            );

            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(ctx.getBucket())
                            .object(targetObject)
                            .source(
                                    CopySource.builder()
                                            .bucket(ctx.getBucket())
                                            .object(item.objectName())
                                            .build()
                            )
                            .build()
            );

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(ctx.getBucket())
                            .object(item.objectName())
                            .build()
            );
        }

        log.debug("MOVE DIRECTORY DONE");
    }
}
