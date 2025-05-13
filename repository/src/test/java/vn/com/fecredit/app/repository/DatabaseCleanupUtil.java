package vn.com.fecredit.app.repository;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to clean database tables between tests
 */
@Component
@Slf4j
public class DatabaseCleanupUtil {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Cleans all tables in the database
     */
    @Transactional
    public void cleanAllTables() {
        log.info("Cleaning all tables in the database");
        entityManager.createNativeQuery("DELETE FROM spin_histories").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM golden_hours").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM reward_events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM rewards").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM participant_events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM participants").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM event_locations").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM region_province").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM provinces").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM regions").executeUpdate();
        entityManager.flush();
        log.info("All tables cleaned");
    }

    /**
     * Alternative method to clean specific tables by name with case handling
     * This can be used when you need to clean only specific tables
     * 
     * @param tableNames List of table names to clean
     */
    @Transactional
    public void cleanTables(List<String> tableNames) {
        for (String tableName : tableNames) {
            try {
                // Don't add quotes automatically as it can cause issues with some databases
                // Only use the table name as provided by the caller
                log.info("Cleaning table: {}", tableName);
                entityManager.createNativeQuery("DELETE FROM " + tableName)
                        .executeUpdate();
                log.info("Table {} cleaned successfully", tableName);
            } catch (Exception e) {
                log.warn("Error cleaning table {}: {}", tableName, e.getMessage());
            }
        }
    }
}
