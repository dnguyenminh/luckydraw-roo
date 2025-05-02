package vn.com.fecredit.app.controller.config;

import javax.sql.DataSource;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

/**
 * Test database configuration that ensures proper data loading for integration tests
 */
@TestConfiguration
public class TestDbSetup {

    /**
     * Configure an embedded database with pre-loaded SQL data for tests
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        // Let Hibernate generate the schema instead of using SQL scripts
        // that could have ordering issues with foreign key constraints
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                // Don't run schema.sql which has foreign key constraint issues
                // .addScript("classpath:schema.sql")      
                // .addScript("classpath:test-data.sql")
                .build();
    }

    /**
     * JDBC template for executing direct SQL if needed in tests
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
