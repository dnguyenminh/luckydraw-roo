package vn.com.fecredit.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.mockito.Mockito;
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
@Import(TestServiceConfig.class)
public class ServiceTestApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ServiceTestApplication.class, args);
    }
    
    /**
     * Provides a PasswordEncoder bean for the test context.
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Provides a mock UserDetailsService for testing
     * 
     * @return Mock UserDetailsService
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return Mockito.mock(UserDetailsService.class);
    }
    
    /**
     * Provides a mock AuthenticationManager for testing
     * 
     * @return Mock AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        return Mockito.mock(AuthenticationManager.class);
    }
}
