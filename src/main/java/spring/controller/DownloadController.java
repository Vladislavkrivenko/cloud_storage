package spring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring.storage.service.FileStorageFacade;

import java.io.InputStream;

@RestController
@RequestMapping("/api/download")
@RequiredArgsConstructor
public class DownloadController extends BaseController {
    private final FileStorageFacade fileStorageFacade;

    @GetMapping
    public ResponseEntity<InputStreamResource> download(@RequestParam String path,
                                                        Authentication auth) {
        InputStream stream = fileStorageFacade.download(userId(auth), path);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"download\""
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(stream));
    }
}
