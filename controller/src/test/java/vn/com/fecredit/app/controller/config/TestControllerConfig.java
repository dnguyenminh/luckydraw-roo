package vn.com.fecredit.app.controller.config;

import jakarta.annotation.PostConstruct;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import vn.com.fecredit.app.service.validator.TableFetchRequestValidator;


/**
 * Configuration class for service module tests.
 * Provides necessary beans to ensure the service tests can run properly.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
        "vn.com.fecredit.app.service",
        "vn.com.fecredit.app.service.impl",
        "vn.com.fecredit.app.service.factory",
        "vn.com.fecredit.app.service.validator"
})
@EnableJpaRepositories(basePackages = "vn.com.fecredit.app.repository")
@EntityScan(basePackages = "vn.com.fecredit.app.entity")
@ActiveProfiles("test")
public class TestControllerConfig {

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

    @PostConstruct
    public void logBeansInContext() {
        try {
            System.out.println("=== BEAN DIAGNOSTIC INFORMATION ===");
            System.out.println("Testing if UserService interface is available on classpath:");
            System.out.println(Class.forName("vn.com.fecredit.app.service.UserService").getName());

            System.out.println("\nTrying to find implementation classes:");
            String implementationClassName = "vn.com.fecredit.app.service.impl.UserServiceImpl";
            try {
                System.out.println(Class.forName(implementationClassName).getName() + " found on classpath");
            } catch (ClassNotFoundException e) {
                System.out.println("Implementation class " + implementationClassName + " not found!");
            }

            System.out.println("\nPackage Structure:");
            Package[] packages = Package.getPackages();
            for (Package p : packages) {
                if (p.getName().startsWith("vn.com.fecredit.app")) {
                    System.out.println("- " + p.getName());
                }
            }
        } catch (Exception e) {
            System.out.println("Error during diagnostics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // UserService will be automatically loaded from the service package
    // via component scanning defined in @ComponentScan annotation
}
