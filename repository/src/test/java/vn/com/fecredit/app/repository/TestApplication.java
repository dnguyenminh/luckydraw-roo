package vn.com.fecredit.app.repository;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;

import vn.com.fecredit.app.repository.config.TestApplicationContextInitializer;
import vn.com.fecredit.app.repository.config.TestConfig;

@SpringBootApplication
@Import(TestConfig.class)
@EntityScan("vn.com.fecredit.app.entity") // Explicit entity scanning
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TestApplication.class);
        app.addInitializers(new TestApplicationContextInitializer()); // Add our custom initializer
        app.run(args);
    }
}
