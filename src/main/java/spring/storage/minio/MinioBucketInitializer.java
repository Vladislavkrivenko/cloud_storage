package spring.storage.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioBucketInitializer {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @PostConstruct
    public void initBucket() {
        String bucket = minioProperties.getBucket();

        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("MinIO bucket name is not configured");
        }

        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucket)
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucket)
                                .build()
                );
                log.debug("MinIO bucket '{}' created successfully", bucket);
            } else {
                log.debug("MinIO bucket '{}' already exists", bucket);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize MinIO bucket: " + bucket, e);
        }
    }
}
