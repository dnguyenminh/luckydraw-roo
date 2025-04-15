package vn.com.fecredit.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import vn.com.fecredit.app.controller.config.TestControllerConfig;

@SpringBootApplication
@ComponentScan(basePackages = {
    "vn.com.fecredit.app.controller.config", 
    "vn.com.fecredit.app.controller.api",
    "vn.com.fecredit.app.service",
    "vn.com.fecredit.app.service.impl"
})
@EntityScan(basePackages = "vn.com.fecredit.app.entity") 
@Import(TestControllerConfig.class)  // Import the TestControllerConfig
public class ControllerTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ControllerTestApplication.class, args);
    }
}
