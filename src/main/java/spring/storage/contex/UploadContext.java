package spring.storage.contex;

import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;

@Getter
@Builder
public class UploadContext {
    private final Integer userId;
    private final String bucket;

    private final String objectName;

    private final String relativePath;

    private final String originalFileName;
    private final Long fileSize;
    private final String contentType;

    private final InputStream inputStream;
}
