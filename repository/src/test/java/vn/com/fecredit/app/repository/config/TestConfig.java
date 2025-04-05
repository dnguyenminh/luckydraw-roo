package vn.com.fecredit.app.repository.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EntityScan(basePackages = "vn.com.fecredit.app.entity")
@EnableJpaRepositories(basePackages = "vn.com.fecredit.app.repository")
@ComponentScan(basePackages = "vn.com.fecredit.app.repository")
@EnableTransactionManagement
public class TestConfig {
    // No custom beans needed for repository tests as they use the actual database
}
