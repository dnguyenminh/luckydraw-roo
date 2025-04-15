package vn.com.fecredit.app.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

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
        // Set names of all tables to clean in reverse order of dependency
        // Use quoted identifiers to handle case sensitivity in H2
        List<String> tableNames = Arrays.asList(
            "\"spin_histories\"",
            "\"participant_events\"", 
            "\"participants\"",
            "\"event_locations\"",
            "\"golden_hours\"", 
            "\"rewards\"",
            "\"events\"",
            "\"provinces\"", 
            "\"regions\"",
            "\"blacklisted_tokens\"",
            "\"configurations\"", 
            "\"audit_logs\"",
            "\"role_permissions\"", // Use lowercase name with quotes to match actual table name
            "\"user_roles\"",      
            "\"permissions\"", 
            "\"roles\"",
            "\"users\""
        );

        // Clean each table using native SQL with quoted table names to preserve case
        for (String tableName : tableNames) {
            try {
                log.info("Cleaning table: {}", tableName);
                // Using quoted identifiers in the SQL statement
                entityManager.createNativeQuery("DELETE FROM " + tableName)
                    .executeUpdate();
                log.info("Table {} cleaned successfully", tableName);
            } catch (Exception e) {
                log.warn("Error cleaning table {}: {}", tableName, e.getMessage());
                // Continue with other tables
            }
        }
        
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
                // Ensure table name is quoted for case sensitivity
                String quotedName = tableName.startsWith("\"") ? tableName : "\"" + tableName + "\"";
                log.info("Cleaning table: {}", quotedName);
                entityManager.createNativeQuery("DELETE FROM " + quotedName)
                    .executeUpdate();
                log.info("Table {} cleaned successfully", quotedName);
            } catch (Exception e) {
                log.warn("Error cleaning table {}: {}", tableName, e.getMessage());
            }
        }
    }
}
