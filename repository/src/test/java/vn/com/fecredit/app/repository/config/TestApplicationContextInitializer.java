package vn.com.fecredit.app.repository.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom application context initializer for repository tests.
 * This class ensures proper configuration of entity scanning properties
 * without modifying existing Java configuration classes.
 */
public class TestApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        // Add additional properties for testing
        Map<String, Object> testProps = new HashMap<>();
        
        // Enable component scanning for entity classes
        testProps.put("spring.boot.test.autoconfigure.entity-scan", "vn.com.fecredit.app.entity");
        
        // Increase context failure threshold
        testProps.put("spring.test.context.failure-threshold", 5);
        
        // Ensure consistent database configuration
        testProps.put("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        testProps.put("spring.jpa.hibernate.ddl-auto", "none");
        testProps.put("spring.sql.init.mode", "always");
        
        // Improve H2 configuration for testing
        testProps.put("spring.datasource.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE");
        
        // Add these properties to the environment
        MapPropertySource testPropertySource = new MapPropertySource("testProperties", testProps);
        environment.getPropertySources().addFirst(testPropertySource);
    }
}