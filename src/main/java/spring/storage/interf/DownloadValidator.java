package spring.storage.interf;

import spring.storage.contex.DownloadContext;

public interface DownloadValidator {
    DownloadContext validate(Integer userId, String path);
}
