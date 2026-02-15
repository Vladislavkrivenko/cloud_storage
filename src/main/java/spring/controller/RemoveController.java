package spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import spring.dto.ApiError;
import spring.storage.service.FileStorageFacade;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
@Tag(name = "Resources", description = "Delete operations")
@SecurityRequirement(name = "SessionAuth")
public class RemoveController extends BaseController {

    private final FileStorageFacade fileStorageFacade;

    @Operation(
            summary = "Delete resource",
            description = "Deletes file or directory by path"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Resource deleted"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@RequestParam String path, Authentication auth) {
        fileStorageFacade.remove(userId(auth), path);
    }
}