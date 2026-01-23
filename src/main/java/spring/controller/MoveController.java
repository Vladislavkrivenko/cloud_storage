package spring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring.dto.ResourceDto;
import spring.storage.service.FileStorageFacade;

@RestController
@RequestMapping("/api/move")
@RequiredArgsConstructor
public class MoveController extends BaseController {
    private final FileStorageFacade fileStorageFacade;

    @PostMapping
    public ResourceDto move(
            @RequestParam String from,
            @RequestParam String to,
            Authentication auth
    ) throws Exception {
        return fileStorageFacade.move(userId(auth), from, to);
    }
}
