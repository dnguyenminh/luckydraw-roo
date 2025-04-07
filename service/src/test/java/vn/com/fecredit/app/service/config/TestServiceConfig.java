package vn.com.fecredit.app.service.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import vn.com.fecredit.app.service.validator.TableFetchRequestValidator;

/**
 * Configuration class for service module tests.
 * Provides necessary beans to ensure the service tests can run properly.
 */
@TestConfiguration
public class TestServiceConfig {

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return Mockito.mock(UserDetailsService.class);
    }
    
    @Bean
    @Primary
    public AuthenticationManager authenticationManager() {
        return Mockito.mock(AuthenticationManager.class);
    }
    
    @Bean
    @Primary
    public TableFetchRequestValidator tableFetchRequestValidator() {
        // Create a real instance instead of a mock for more reliable behavior
        return new TableFetchRequestValidator();
    }
}
