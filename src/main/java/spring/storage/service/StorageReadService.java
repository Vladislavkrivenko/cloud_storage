package spring.storage.service;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring.dto.ResourceDto;
import spring.dto.ResourceType;
import spring.exeption.storageExeption.ApiException;
import spring.exeption.storageExeption.BadRequestException;
import spring.exeption.storageExeption.NotFoundException;
import spring.storage.contex.DownloadContext;
import spring.storage.contex.ListContext;
import spring.storage.contex.SearchContext;
import spring.storage.interf.DownloadValidator;
import spring.storage.interf.ListValidator;
import spring.storage.interf.SearchValidator;
import spring.storage.minio.MinioProperties;
import spring.util.StoragePathUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageReadService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final DownloadValidator downloadValidator;
    private final ListValidator listValidator;
    private final SearchValidator searchValidator;
    private final Executor zipExecutor;

    public ResourceDto getResource(Integer userId, String path) {

        if (path == null || path.isBlank()) {
            throw new BadRequestException("Path must not be empty");
        }

        String bucket = minioProperties.getBucket();
        String basePrefix = StoragePathUtils.basePrefix(userId);

        try {
            String fileObjectName = basePrefix + StoragePathUtils.normalizeFile(path);

            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(fileObjectName)
                            .build()
            );

            return new ResourceDto(
                    StoragePathUtils.extractName(fileObjectName),
                    path,
                    ResourceType.FILE,
                    stat.size()

            );

        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
            } else {
                throw new IllegalStateException("Failed to stat file: " + path, e);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to stat file: " + path, e);
        }

        try {
            String dirPrefix = basePrefix + StoragePathUtils.normalizeDirectory(path);

            Iterable<Result<Item>> iterable = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(dirPrefix)
                            .recursive(false)
                            .maxKeys(1)
                            .build()
            );

            if (iterable.iterator().hasNext()) {
                return new ResourceDto(
                        StoragePathUtils.extractName(dirPrefix),
                        path,
                        ResourceType.DIRECTORY,
                        null
                );
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to check directory: " + path, e);
        }

        throw new NotFoundException("Resource not found: " + path);
    }

    public InputStream download(Integer userId, String path) {
        DownloadContext ctx = downloadValidator.validate(userId, path);

        return ctx.getResourceType() == ResourceType.FILE
                ? downloadFile(ctx)
                : downloadDirectoryAsZip(ctx);
    }

    private InputStream downloadFile(DownloadContext ctx) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(ctx.getBucket())
                            .object(ctx.getObject())
                            .build()
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to download file: " + ctx.getPath(), e
            );
        }
    }

    private InputStream downloadDirectoryAsZip(DownloadContext ctx) {
        try {
            PipedOutputStream pos = new PipedOutputStream();
            PipedInputStream pis = new PipedInputStream(pos);

            zipExecutor.execute(() -> {
                try (ZipOutputStream zos = new ZipOutputStream(pos)) {

                    Iterable<Result<Item>> items = minioClient.listObjects(
                            ListObjectsArgs.builder()
                                    .bucket(ctx.getBucket())
                                    .prefix(ctx.getObject() + "/")
                                    .recursive(true)
                                    .build()
                    );

                    for (Result<Item> result : items) {
                        Item item = result.get();

                        if (item.objectName().endsWith("/")) {
                            continue;
                        }

                        String relativePath =
                                item.objectName()
                                        .substring(ctx.getObject().length() + 1);

                        zos.putNextEntry(new ZipEntry(relativePath));

                        try (InputStream is = minioClient.getObject(
                                GetObjectArgs.builder()
                                        .bucket(ctx.getBucket())
                                        .object(item.objectName())
                                        .build()
                        )) {
                            is.transferTo(zos);
                        }

                        zos.closeEntry();
                    }

                } catch (Exception e) {
                    log.error("Failed to stream zip for {}", ctx.getPath(), e);
                }
            });

            return pis;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to create zip stream for " + ctx.getPath(), e
            );
        }
    }

    public List<ResourceDto> search(Integer userId, String query) {
        SearchContext ctx = searchValidator.validate(userId, query);
        List<ResourceDto> result = new ArrayList<>();
        try {
            Iterable<Result<Item>> iterable = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(ctx.getBucket())
                            .prefix(ctx.getBasePrefix())
                            .recursive(true)
                            .build()
            );

            for (Result<Item> r : iterable) {
                Item item = r.get();

                String objectName = item.objectName();
                if (!objectName.contains(ctx.getQuery())) {
                    continue;
                }

                boolean isDirectory = objectName.endsWith("/");

                result.add(new ResourceDto(
                        StoragePathUtils.extractName(objectName),
                        objectName.replace(ctx.getBasePrefix(), ""),
                        isDirectory ? ResourceType.DIRECTORY : ResourceType.FILE,
                        isDirectory ? null : item.size()
                ));

            }
            return result;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to search files: " + query, e);
        }
    }

    public List<ResourceDto> listDirectory(Integer userId, String path) {
        ListContext ctx = listValidator.validate(userId, path);

        List<ResourceDto> result = new ArrayList<>();

        try {
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(ctx.getBucket())
                            .prefix(ctx.getPrefix())
                            .recursive(false)
                            .build()
            );
            for (Result<Item> r : objects) {
                Item item = r.get();

                if (item.objectName().equals(ctx.getPrefix())) {
                    continue;
                }
                boolean isDirectory = item.objectName().endsWith("/");
                result.add(new ResourceDto(
                        StoragePathUtils.extractName(item.objectName()),
                        item.objectName().replace(ctx.getBasePrefix(), ""),
                        isDirectory ? ResourceType.DIRECTORY : ResourceType.FILE,
                        isDirectory ? null : item.size()
                ));
            }
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to list directory: " + path, e);
        }
    }

}
