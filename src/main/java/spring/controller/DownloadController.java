package spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import spring.dto.ApiError;
import spring.storage.resolver.ResolvedPath;
import spring.storage.resolver.ResolvedType;
import spring.storage.resolver.StoragePathResolver;
import spring.storage.service.FileStorageFacade;

import java.io.InputStream;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
@Tag(name = "Resources", description = "File and directory resource operations")
@SecurityRequirement(name = "cookieAuth")
public class DownloadController extends BaseController {

    private final FileStorageFacade fileStorageFacade;
    private final StoragePathResolver storagePathResolver;

    @Operation(summary = "Download file or directory",
            description = "Downloads file as binary stream. Directories are returned as zip archive.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Binary file content",
                    content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "400", description = "Invalid path",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Resource not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping(value = "/download")
    public ResponseEntity<StreamingResponseBody> download(
            @Parameter(description = "Full path to resource", example = "folder/file.txt")
            @RequestParam String path,
            Authentication auth
    ) {
        Integer userId = userId(auth);
        ResolvedPath resolved = storagePathResolver.resolve(userId, path);

        InputStream is = fileStorageFacade.download(userId, path);

        boolean isDirectory = resolved.getType() == ResolvedType.DIRECTORY;

        StreamingResponseBody body = outputStream -> {
            try (is) {
                is.transferTo(outputStream);
            }
        };

        return ResponseEntity.ok()
                .contentType(isDirectory
                        ? MediaType.valueOf("application/zip")
                        : MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }
}
