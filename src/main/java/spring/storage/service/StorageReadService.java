package spring.storage.service;

import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring.dto.ResourceDto;
import spring.dto.ResourceType;
import spring.exeption.storageExeption.ApiException;
import spring.exeption.storageExeption.BadRequestException;
import spring.storage.contex.DownloadContext;
import spring.storage.contex.ListContext;
import spring.storage.contex.SearchContext;
import spring.storage.interf.DownloadValidator;
import spring.storage.interf.ListValidator;
import spring.storage.interf.SearchValidator;
import spring.storage.minio.MinioProperties;
import spring.storage.resolver.ResolvedPath;
import spring.storage.resolver.ResolvedType;
import spring.storage.resolver.StoragePathResolver;
import spring.util.StoragePathUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.*;
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
    private final StoragePathResolver storagePathResolver;

    public ResourceDto getResource(Integer userId, String path) {
        ResolvedPath resolved = storagePathResolver.resolve(userId, path);
        String basePrefix = StoragePathUtils.basePrefix(userId);

        if (resolved.getType() == ResolvedType.FILE) {
            try {
                StatObjectResponse stat = minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(minioProperties.getBucket())
                                .object(resolved.getObject())
                                .build()
                );

                return new ResourceDto(
                        StoragePathUtils.extractName(resolved.getObject()),
                        StoragePathUtils.extractParentPath(
                                resolved.getObject(),
                                basePrefix
                        ),
                        ResourceType.FILE,
                        stat.size()
                );

            } catch (Exception e) {
                throw new IllegalStateException("Failed to stat file: " + path, e);
            }
        }
        String dirPrefix = resolved.getPrefix();

        if (dirPrefix.equals(basePrefix)) {
            throw new BadRequestException("Root directory has no resource representation");
        }
        String parentPath = StoragePathUtils.extractParentPath(
                dirPrefix,
                basePrefix
        );

        String name = StoragePathUtils.extractName(dirPrefix);

        return new ResourceDto(
                name.endsWith("/") ? name : name + "/",
                parentPath,
                ResourceType.DIRECTORY,
                null
        );
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

            String dirPrefix = ctx.getPrefix();

            zipExecutor.execute(() -> {
                try (ZipOutputStream zos = new ZipOutputStream(pos)) {

                    Iterable<Result<Item>> items = minioClient.listObjects(
                            ListObjectsArgs.builder()
                                    .bucket(ctx.getBucket())
                                    .prefix(dirPrefix)
                                    .recursive(true)
                                    .build()
                    );

                    for (Result<Item> result : items) {
                        Item item = result.get();

                        if (item.objectName().endsWith("/")) {
                            continue;
                        }

                        String relativePath =
                                item.objectName().substring(dirPrefix.length());

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
        Set<String> seenDirectories = new HashSet<>();

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

                if (objectName.equals(ctx.getBasePrefix())) {
                    continue;
                }

                String name = StoragePathUtils.extractName(objectName);
                if (!name.contains(ctx.getQuery())) {
                    continue;
                }

                boolean isDirectory = objectName.endsWith("/");

                if (isDirectory && !seenDirectories.add(objectName)) {
                    continue;
                }

                String fullPath = objectName.replace(ctx.getBasePrefix(), "");
                result.add(new ResourceDto(
                        name,
                        fullPath,
                        isDirectory ? ResourceType.DIRECTORY : ResourceType.FILE,
                        isDirectory ? null : item.size()
                ));

            }
            result.sort(
                    Comparator
                            .comparing((ResourceDto r) -> r.type() != ResourceType.DIRECTORY)
                            .thenComparing(ResourceDto::name)
            );

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
                String objectName = item.objectName();

                if (objectName.equals(ctx.getPrefix())) {
                    continue;
                }

                boolean isDirectory = objectName.endsWith("/");

                String name = StoragePathUtils.extractName(objectName);

                if (isDirectory && !name.endsWith("/")) {
                    name = name + "/";
                }

                String parentPath = StoragePathUtils.extractParentPath(
                        objectName,
                        ctx.getBasePrefix()
                );
                log.debug(
                        "LIST DTO -> name='{}', fullPath='{}', isDirectory={}",
                        name,
                        parentPath,
                        isDirectory
                );
                result.add(new ResourceDto(
                        name,
                        parentPath,
                        isDirectory ? ResourceType.DIRECTORY : ResourceType.FILE,
                        isDirectory ? null : item.size()
                ));
            }

            result.sort(
                    Comparator
                            .comparing((ResourceDto r) -> r.type() != ResourceType.DIRECTORY)
                            .thenComparing(ResourceDto::name)
            );
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to list directory: " + path, e);
        }
    }

}
