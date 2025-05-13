package vn.com.fecredit.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import vn.com.fecredit.app.config.TestConfig;
import vn.com.fecredit.app.service.config.TestServiceConfig;

/**
 * Test application configuration for service tests.
 * This allows running tests without loading the entire application context.
 */
@SpringBootApplication
@EntityScan(basePackages = "vn.com.fecredit.app.entity")
@EnableJpaRepositories(basePackages = "vn.com.fecredit.app.repository")
@ComponentScan(basePackages = {
    "vn.com.fecredit.app.service", 
    "vn.com.fecredit.app.service.impl", 
    "vn.com.fecredit.app.service.factory",
    "vn.com.fecredit.app.service.validator" // Only include validator package, not validation
})
@Import({TestServiceConfig.class, TestConfig.class})
public class ServiceTestApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ServiceTestApplication.class, args);
    }
    
    // Removed conflicting bean definitions for userDetailsService and authenticationManager
    // These are now provided by TestServiceConfig with @Primary annotation
}
