package spring.storage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import spring.dto.ResourceDto;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileStorageFacade {

    private final StorageReadService readService;
    private final StorageWriteService writeService;

    public ResourceDto getResource(Integer userId, String path) {
        return readService.getResource(userId, path);
    }

    public InputStream download(Integer userId, String path) {
        return readService.download(userId, path);
    }

    public List<ResourceDto> listDirectory(Integer userId, String path) {
        return readService.listDirectory(userId, path);
    }

    public List<ResourceDto> search(Integer userId, String query) {
        return readService.search(userId, query);
    }


    public ResourceDto upload(Integer userId, String targetPath, MultipartFile file) {
        return writeService.upload(userId, targetPath, file);
    }

    public ResourceDto move(Integer userId, String from, String to) {
        return writeService.move(userId, from, to);
    }

    public void remove(Integer userId, String path) {
        writeService.remove(userId, path);
    }

    public ResourceDto createDirectory(Integer userId, String path) {
        return writeService.createDirectory(userId, path);
    }
}
