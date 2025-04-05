package vn.com.fecredit.app.controller.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import vn.com.fecredit.app.controller.util.DatabaseCleanupUtil;

/**
 * Test configuration for controller tests.
 * This class serves as the SpringBootConfiguration for controller tests.
 */
@Configuration
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableTransactionManagement
@EntityScan(basePackages = "vn.com.fecredit.app.entity")
@EnableJpaRepositories(basePackages = "vn.com.fecredit.app.repository")
@ComponentScan(basePackages = {"vn.com.fecredit.app.controller", "vn.com.fecredit.app.service", "vn.com.fecredit.app.repository"})
public class TestConfig {
    
    /**
     * Register DatabaseCleanupUtil bean
     * @return DatabaseCleanupUtil instance
     */
    @Bean
    public DatabaseCleanupUtil databaseCleanupUtil() {
        return new DatabaseCleanupUtil();
    }
}
