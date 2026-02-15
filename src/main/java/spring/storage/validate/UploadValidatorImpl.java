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
import spring.storage.resolver.ResolvedPath;
import spring.storage.resolver.ResolvedType;
import spring.storage.resolver.StoragePathResolver;
import spring.util.StorageExistenceUtils;
import spring.util.StoragePathUtils;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class UploadValidatorImpl implements UploadValidator {

    private final MinioProperties minioProperties;
    private final MinioClient minioClient;
    private final StoragePathResolver resolver;

    @Override
    public UploadContext validate(Integer userId, String targetPath, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new BadRequestException("File name must not be empty");
        }

        String bucket = minioProperties.getBucket();
        String base = StoragePathUtils.basePrefix(userId);

        ResolvedPath resolved = resolver.resolve(userId, targetPath);

        if (resolved.getType() != ResolvedType.DIRECTORY) {
            throw new BadRequestException("Upload target must be a directory");
        }

        String targetPrefix = resolved.getPrefix();
        String objectName = targetPrefix + fileName;

        if (StorageExistenceUtils.fileExists(minioClient, bucket, objectName)) {
            throw new ConflictException("File already exists: " + fileName);
        }

        String relativePath = objectName.replace(base, "");

        return UploadContext.builder()
                .userId(userId)
                .bucket(bucket)
                .targetPrefix(targetPrefix)
                .objectName(objectName)
                .name(fileName)
                .path(StoragePathUtils.extractParentPath(objectName, base))
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
