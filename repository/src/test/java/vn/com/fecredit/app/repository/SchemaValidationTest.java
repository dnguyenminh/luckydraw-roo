package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Test to verify database schema is correctly set up
 */
@Slf4j
public class SchemaValidationTest extends AbstractRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

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
}
