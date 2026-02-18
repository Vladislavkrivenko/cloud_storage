package spring;

import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {

    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("pass");

    static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7.2")
                    .withExposedPorts(6379);

    static final GenericContainer<?> minio =
            new GenericContainer<>("minio/minio:latest")
                    .withExposedPorts(9000)
                    .withEnv("MINIO_ROOT_USER", "admin")
                    .withEnv("MINIO_ROOT_PASSWORD", "adminadmin")
                    .withCommand("server /data");

    // 🔥 СТАРТУЄМО 1 РАЗ
    static {
        postgres.start();
        redis.start();
        minio.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url",
                () -> postgres.getJdbcUrl() + "&options=-c%20TimeZone=UTC");

        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port",
                () -> redis.getMappedPort(6379));

        registry.add("spring.session.store-type", () -> "redis");

        registry.add("minio.url",
                () -> "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
        registry.add("minio.access-key", () -> "admin");
        registry.add("minio.secret-key", () -> "adminadmin");
        registry.add("minio.bucket", () -> "user-files");
    }
}
