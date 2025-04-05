package vn.com.fecredit.app.controller.util;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Test application class that provides a complete Spring Boot configuration for tests.
 * This is the entry point for the test context and scans all necessary packages.
 */
@SpringBootApplication
@EntityScan("vn.com.fecredit.app.entity")
@EnableJpaRepositories("vn.com.fecredit.app.repository") 
@ComponentScan(basePackages = {
    "vn.com.fecredit.app.controller",
    "vn.com.fecredit.app.service", 
    "vn.com.fecredit.app.repository",
    "vn.com.fecredit.app.controller.util",
    "vn.com.fecredit.app.controller.config"
})
public class TestApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
