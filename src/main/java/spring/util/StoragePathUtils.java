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

    public static String extractName(String objectName) {

        log.debug("extractName INPUT: '{}'", objectName);

        if (objectName == null || objectName.isBlank()) {
            log.debug("extractName EMPTY");
            return "";
        }

        if (objectName.endsWith("/")) {
            objectName = objectName.substring(0, objectName.length() - 1);
            log.debug("extractName AFTER trim '/': '{}'", objectName);
        }

        int idx = objectName.lastIndexOf('/');

        String result = idx >= 0
                ? objectName.substring(idx + 1)
                : objectName;

        log.debug("extractName RESULT: '{}'", result);
        return result;
    }

    public String extractParentPath(String objectName, String basePrefix) {
        String relative = objectName.replace(basePrefix, "");
        if (relative.endsWith("/")) {
            relative = relative.substring(0, relative.length() - 1);
        }

        int idx = relative.lastIndexOf('/');
        if (idx < 0) {
            return "";
        }

        return relative.substring(0, idx + 1);
    }

}
