package vn.com.fecredit.app.controller.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Utility class for cleaning up the database during controller tests.
 * Provides methods to truncate tables and reset sequences.
 */
@Component
public class DatabaseCleanupUtil {

    private static final List<String> TABLE_NAMES = List.of(
            "spin_histories",
            "participant_events",
            "event_locations",
            "events",
            "participants",
            "provinces",
            "regions",
            "rewards",
            "golden_hours",
            "blacklisted_tokens",
            "user_roles",
            "permissions",
            "role_permissions",
            "roles",
            "users",
            "audit_logs",
            "configurations"
    );

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Clean all tables in the test database
     */
    @Transactional
    public void cleanAllTables() {
        entityManager.flush();
        entityManager.clear();
        
        // Disable foreign key checks for cleanup
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        
        // Truncate each table and reset identity
        for (String tableName : TABLE_NAMES) {
            try {
                entityManager.createNativeQuery("TRUNCATE TABLE " + tableName + " RESTART IDENTITY").executeUpdate();
            } catch (Exception e) {
                // Table might not exist in current test context, just continue
            }
        }
        
        // Re-enable foreign key checks
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }
    
    /**
     * Clean specific tables
     * @param tableNames names of tables to clean
     */
    @Transactional
    public void cleanTables(String... tableNames) {
        entityManager.flush();
        entityManager.clear();
        
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        
        for (String tableName : tableNames) {
            try {
                entityManager.createNativeQuery("TRUNCATE TABLE " + tableName + " RESTART IDENTITY").executeUpdate();
            } catch (Exception e) {
                // Table might not exist, just continue
            }
        }
        
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }
}
