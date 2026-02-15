package spring.conf;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cloudStorageOpenAPI() {

        SecurityScheme sessionScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("SESSION")
                .description("Session cookie authentication");

        return new OpenAPI()
                .info(new Info()
                        .title("Cloud Storage API")
                        .description("""
                                Multi-user cloud file storage service.
                                Files are stored in MinIO (S3 compatible).
                                Sessions are stored in Redis.
                                Authentication is handled via SESSION cookie.
                                """)
                        .version("v1.0.0")
                )
                .components(new Components()
                        .addSecuritySchemes("sessionAuth", sessionScheme)
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList("sessionAuth"));
    }
}
