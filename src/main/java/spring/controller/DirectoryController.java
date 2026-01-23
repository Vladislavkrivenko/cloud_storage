package spring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import spring.dto.ResourceDto;
import spring.storage.service.FileStorageFacade;

@RestController
@RequestMapping("/api/directories")
@RequiredArgsConstructor
public class DirectoryController extends BaseController {

    private final FileStorageFacade fileStorageFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceDto createDirectory(@RequestParam String path,
                                       Authentication authentication) {
        return fileStorageFacade.createDirectory(userId(authentication), path);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDirectory(@RequestParam String path, Authentication authentication) {
        fileStorageFacade.remove(userId(authentication), path);
    }
}
