package spring.storage.interf;

import org.springframework.web.multipart.MultipartFile;
import spring.storage.contex.UploadContext;

public interface UploadValidator {
    UploadContext validate(Integer userId, String targetPath, MultipartFile file);
}
