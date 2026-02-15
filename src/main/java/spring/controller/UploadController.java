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
import org.springframework.web.multipart.MultipartFile;
import spring.dto.ApiError;
import spring.dto.ResourceDto;
import spring.exeption.storageExeption.BadRequestException;
import spring.storage.service.FileStorageFacade;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
@Tag(name = "Resources", description = "File operations")
@SecurityRequirement(name = "SessionAuth")
public class UploadController extends BaseController {

    private final FileStorageFacade fileStorageFacade;

    @Operation(
            summary = "Upload files",
            description = "Uploads one or multiple files into specified directory"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Files uploaded"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "File already exists",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResourceDto> upload(

            @Parameter(description = "Target directory path", example = "folder1/")
            @RequestParam(required = false, defaultValue = "") String path,

            @Parameter(
                    description = "Files to upload",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart("object") MultipartFile[] object,

            Authentication authentication
    ) {

        if (object == null || object.length == 0) {
            throw new BadRequestException("No files to upload");
        }

        List<ResourceDto> result = new ArrayList<>();

        for (MultipartFile file : object) {
            result.add(fileStorageFacade.upload(userId(authentication), path, file));
        }

        return result;
    }
}
