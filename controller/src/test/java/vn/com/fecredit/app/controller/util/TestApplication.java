package vn.com.fecredit.app.controller.util;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import vn.com.fecredit.app.controller.api.TableDataController;
import vn.com.fecredit.app.controller.config.ControllerTestConfig;
import vn.com.fecredit.app.controller.config.TestSecurityConfig;

/**
 * Test application configuration that excludes database-related auto-configuration
 * to prevent JPA from trying to create a real entity manager factory.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    SecurityAutoConfiguration.class
})
@ComponentScan(basePackages = {"vn.com.fecredit.app.controller.api"})
@Import({ControllerTestConfig.class, TestSecurityConfig.class})
public class TestApplication {
    // Main configuration class for tests
}
