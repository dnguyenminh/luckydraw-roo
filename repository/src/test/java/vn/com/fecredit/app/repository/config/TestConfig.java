package vn.com.fecredit.app.repository.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;

@Configuration
@EntityScan(basePackages = "vn.com.fecredit.app.entity")
@EnableJpaRepositories(basePackages = "vn.com.fecredit.app.repository")
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "testAuditorAware")
public class TestConfig {

    @Bean
    public AuditorAware<String> testAuditorAware() {
        return () -> Optional.of("test-user");
    }
}
