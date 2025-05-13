package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import lombok.extern.slf4j.Slf4j;

/**
 * Test to verify database schema is correctly set up
 */
@Slf4j
@JdbcTest
@ActiveProfiles("test")
// Use our nested config class instead of TestApplication or JdbcTestConfig
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"classpath:schema-h2.sql"})
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true"
})
public class SchemaValidationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testSpinHistoriesTableExists() {
        // Check if table exists
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'SPIN_HISTORIES'",
                Integer.class);

        assertThat(count).isEqualTo(1);
        log.info("SPIN_HISTORIES table exists");
    }

    @Test
    void testSpinHistoriesWinColumnExists() {
        // Check if win column exists in spin_histories table
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_NAME = 'SPIN_HISTORIES' AND COLUMN_NAME = 'WIN'",
                Integer.class);

        assertThat(count).isEqualTo(1);
        log.info("WIN column exists in SPIN_HISTORIES table");
    }

    @Test
    void testParticipantEventsTableExists() {
        // Check if table exists
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'PARTICIPANT_EVENTS'",
                Integer.class);

        assertThat(count).isEqualTo(1);
        log.info("PARTICIPANT_EVENTS table exists");
    }
    
    // Define our test configuration directly in the test class
    // to avoid any dependency on external configuration classes
    @Configuration
    @EnableAutoConfiguration(exclude = {
        HibernateJpaAutoConfiguration.class
    })
    static class TestConfig {
        
        @Bean
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .build();
        }
        
        @Bean
        public JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
    }
}
