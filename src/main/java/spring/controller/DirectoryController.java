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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import spring.dto.ApiError;
import spring.dto.ResourceDto;
import spring.storage.service.FileStorageFacade;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
@Tag(name = "Directories", description = "Directory operations")
@SecurityRequirement(name = "cookieAuth")
public class DirectoryController extends BaseController {

    private final FileStorageFacade fileStorageFacade;

    @Operation(summary = "Create directory",
            description = "Creates new empty directory")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Directory created"),
            @ApiResponse(responseCode = "400", description = "Invalid path",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Parent directory not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Directory already exists",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceDto createDirectory(
            @Parameter(description = "Full directory path", example = "folder1/folder2/")
            @RequestParam String path,
            Authentication authentication
    ) {
        return fileStorageFacade.createDirectory(userId(authentication), path);
    }

    @Operation(summary = "List directory content",
            description = "Returns non-recursive list of resources inside directory")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Directory content returned"),
            @ApiResponse(responseCode = "400", description = "Invalid path",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Directory not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ResourceDto> listDirectory(
            @Parameter(description = "Directory path", example = "folder1/")
            @RequestParam String path,
            Authentication auth
    ) {
        return fileStorageFacade.listDirectory(userId(auth), path);
    }
}
