package vn.com.fecredit.app.repository.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

/**
 * Configuration class for JDBC-only tests
 * Does not load any JPA-related components
 */
@Configuration
@EnableAutoConfiguration(exclude = {})
@ActiveProfiles("test")
public class JdbcTestConfig {
    // This config is intentionally empty as we only need the JDBC components
    // that are auto-configured by Spring Boot
}