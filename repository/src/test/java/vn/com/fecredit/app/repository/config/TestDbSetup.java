package vn.com.fecredit.app.repository.config;

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
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScript("classpath:schema-h2.sql")
                .addScript("classpath:data-h2.sql")
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
