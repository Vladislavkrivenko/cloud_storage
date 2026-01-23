package spring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spring.dto.ResourceDto;
import spring.exeption.storageExeption.BadRequestException;
import spring.storage.service.FileStorageFacade;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController extends BaseController {
    private final FileStorageFacade fileStorageFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResourceDto> upload(@RequestParam String path,
                                    @RequestPart MultipartFile[] files,
                                    Authentication authentication) {
        List<ResourceDto> result = new ArrayList<>();

        if (files.length == 0) {
            throw new BadRequestException("No files to upload");
        }
        for (MultipartFile file : files) {
            result.add(
                    fileStorageFacade.upload(userId(authentication), path, file)
            );
        }
        return result;
    }
}
