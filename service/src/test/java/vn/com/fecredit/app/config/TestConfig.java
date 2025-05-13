package vn.com.fecredit.app.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    /**
     * Creates a FileStorageProperties bean for tests
     * This resolves the "No qualifying bean of type 'vn.com.fecredit.app.config.FileStorageProperties'" error
     * Using @Primary to resolve bean conflict when multiple FileStorageProperties beans are found
     */
    @Bean
    @Primary
    public FileStorageProperties fileStorageProperties() {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setUploadDir("uploads-test");
        return properties;
    }
}
