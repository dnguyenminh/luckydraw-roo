package vn.com.fecredit.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@ConfigurationProperties(prefix = "file")
@Getter
@Setter
@Component
public class FileStorageProperties {
    private String uploadDir;
    private String exportsDir;

    // Default constructor used by Spring
    public FileStorageProperties() {
        // Set default values
        this.uploadDir = "uploads";
        this.exportsDir = "exports";
    }

    /**
     * Returns the path to the exports directory
     * @return Path object representing the exports directory
     */
    public Path getExportsPath() {
        return Paths.get(exportsDir).toAbsolutePath().normalize();
    }

    /**
     * Returns the path to the uploads directory
     * @return Path object representing the uploads directory
     */
    public Path getUploadPath() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }
}
