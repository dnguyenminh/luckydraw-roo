package vn.com.fecredit.app.controller.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import vn.com.fecredit.app.config.FileStorageProperties;

@Configuration
public class TestConfig {

    /**
     * Creates a FileStorageProperties bean for tests
     * This resolves the "No qualifying bean of type 'vn.com.fecredit.app.config.FileStorageProperties'" error
     */
    @Bean
    @Primary
    public FileStorageProperties fileStorageProperties() {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setUploadDir("uploads-test");
        properties.setExportsDir("exports-test");
        return properties;
    }
}
