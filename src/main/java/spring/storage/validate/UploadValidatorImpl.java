package spring.storage.validate;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import spring.exeption.storageExeption.BadRequestException;
import spring.exeption.storageExeption.ConflictException;
import spring.storage.contex.UploadContext;
import spring.storage.interf.UploadValidator;
import spring.storage.minio.MinioProperties;
import spring.util.StorageExistenceUtils;
import spring.util.StoragePathUtils;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class UploadValidatorImpl implements UploadValidator {

    private final MinioProperties minioProperties;
    private final MinioClient minioClient;

    public UploadContext validate(Integer userId, String targetPath, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new BadRequestException("File name must not be empty");
        }

        String bucket = minioProperties.getBucket();
        String basePrefix = StoragePathUtils.basePrefix(userId);
        String relativePath = StoragePathUtils.normalizeDirectory(targetPath) + fileName;

        String objectName = basePrefix + relativePath;

        if (StorageExistenceUtils.fileExists(minioClient, bucket, objectName)) {
            throw new ConflictException("File already exists: " + fileName);
        }

        return UploadContext.builder()
                .userId(userId)
                .bucket(bucket)
                .objectName(objectName)
                .relativePath(relativePath)
                .originalFileName(fileName)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .inputStream(getStream(file))
                .build();
    }

    private InputStream getStream(MultipartFile file) {
        try {
            return file.getInputStream();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read file stream", e);
        }
    }
}
