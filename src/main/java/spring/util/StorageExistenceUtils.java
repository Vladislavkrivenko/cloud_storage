package spring.util;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.messages.Item;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class StorageExistenceUtils {

    public boolean fileExists(
            MinioClient minioClient,
            String bucket,
            String object
    ) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(object)
                            .build()
            );
            return true;
        } catch (Exception e) {
            log.debug("Failed to check existence for {}/{}: {}", bucket, object, e.toString());
            return false;
        }
    }

    public boolean hasAnyObjectWithPrefix(
            MinioClient minioClient,
            String bucket,
            String prefix
    ) {
        try {
            Iterable<Result<Item>> items = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(prefix)
                            .recursive(true)
                            .maxKeys(1)
                            .build()
            );
            return items.iterator().hasNext();
        } catch (Exception e) {
            log.error("Failed to list objects for prefix {}/{}", bucket, prefix, e);
            throw new IllegalStateException("Failed to access storage", e);
        }
    }
}
