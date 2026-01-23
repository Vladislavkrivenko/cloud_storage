package spring.storage.minio;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ZipExecutorConfig {
    @Bean
    public Executor zipExecutor() {
        return Executors.newCachedThreadPool();
    }
}
