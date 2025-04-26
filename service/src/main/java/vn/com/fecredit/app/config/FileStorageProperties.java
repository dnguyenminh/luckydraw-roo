package vn.com.fecredit.app.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for file storage
 * Reads from application.properties or application.yml
 */
@Component
@ConfigurationProperties(prefix = "app.file-storage")
@Getter
@Setter
public class FileStorageProperties {
    
    /**
     * Base directory for temporary file storage
     * Defaults to system temp directory + 'luckydraw-exports' if not specified
     */
    private String tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "luckydraw-exports").toString();
    
    /**
     * Get the exports directory path
     * @return Path to exports directory
     */
    public Path getExportsPath() {
        return Paths.get(tempDir);
    }
}
