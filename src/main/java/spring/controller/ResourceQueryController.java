package spring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring.dto.ResourceDto;
import spring.storage.service.FileStorageFacade;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceQueryController extends BaseController {
    private final FileStorageFacade fileStorageFacade;

    @GetMapping
    public ResourceDto getResource(@RequestParam String path,
                                   Authentication auth) {
        return fileStorageFacade.getResource(userId(auth), path);
    }

    @GetMapping("/list")
    public List<ResourceDto> listDirectory(@RequestParam String path,
                                           Authentication auth) {
        return fileStorageFacade.listDirectory(userId(auth), path);
    }

    @GetMapping("/search")
    public List<ResourceDto> search(@RequestParam String query, Authentication auth) {
        return fileStorageFacade.search(userId(auth), query);
    }
}
