package vn.com.fecredit.app.repository.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Test security configuration with default test user
 */
@Configuration
public class TestSecurityConfig {

    @Bean
    public String testSecurityUser() {
        return "test-user";
    }
    
    @Bean
    public String[] testSecurityRoles() {
        return new String[] {"USER", "ADMIN"};
    }
}
