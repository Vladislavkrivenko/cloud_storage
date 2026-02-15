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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring.dto.ApiError;
import spring.dto.ResourceDto;
import spring.storage.service.FileStorageFacade;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
@Tag(name = "Resources", description = "Operations with files and directories")
@SecurityRequirement(name = "cookieAuth")
public class MoveController extends BaseController {

    private final FileStorageFacade fileStorageFacade;

    @Operation(summary = "Move or rename resource",
            description = "Moves or renames file or directory")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource moved"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Resource not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Target already exists",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/move")
    public ResourceDto move(
            @Parameter(description = "Current full path", example = "docs/file.txt")
            @RequestParam String from,
            @Parameter(description = "Target full path", example = "archive/file.txt")
            @RequestParam String to,
            Authentication auth
    ) {
        return fileStorageFacade.move(userId(auth), from, to);
    }
}
