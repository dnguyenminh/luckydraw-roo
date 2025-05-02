package vn.com.fecredit.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;

import vn.com.fecredit.app.config.FileStorageProperties;
import vn.com.fecredit.app.controller.config.TestConfig;
import vn.com.fecredit.app.controller.config.TestSecurityConfig;
import vn.com.fecredit.app.controller.config.TestDbSetup;
import vn.com.fecredit.app.security.SecurityConfig;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ComponentScan(
    basePackages = {
        "vn.com.fecredit.app.controller.config", 
        "vn.com.fecredit.app.controller.api",
        "vn.com.fecredit.app.service",
        "vn.com.fecredit.app.service.impl",
        "vn.com.fecredit.app.repository" 
    },
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
    }
)
@EntityScan(basePackages = "vn.com.fecredit.app.entity") 
@EnableConfigurationProperties({FileStorageProperties.class})
@Import({TestConfig.class, TestSecurityConfig.class, TestDbSetup.class})
@PropertySource("classpath:application-test.properties")
public class ControllerTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ControllerTestApplication.class, args);
    }
}
