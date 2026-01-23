package spring.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spring.exeption.storageExeption.BadRequestException;

@Slf4j
@UtilityClass
public class StoragePathUtils {

    public String basePrefix(Integer userId) {
        return "user-" + userId + "-files/";
    }

    public String normalizeDirectory(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }

        path = path.trim();

        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (!path.endsWith("/")) {
            path += "/";
        }
        return path;
    }

    public String normalizeFile(String path) {
        if (path == null || path.isBlank()) {
            throw new BadRequestException("File path must not be empty");
        }

        path = path.trim();

        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            throw new BadRequestException("File path must not end with '/'");
        }
        return path;
    }

    public String extractName(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return "";
        }

        if (objectName.endsWith("/")) {
            objectName = objectName.substring(0, objectName.length() - 1);
        }

        int idx = objectName.lastIndexOf('/');

        return idx >= 0
                ? objectName.substring(idx + 1)
                : objectName;
    }

}
