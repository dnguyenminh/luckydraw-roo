package vn.com.fecredit.app.repository;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Utility class for database cleanup in tests.
 * Handles deletion of test data in the correct order to respect foreign key constraints.
 */
@Component
public class DatabaseCleanupUtil {

    private final EntityManager entityManager;

    @Autowired
    public DatabaseCleanupUtil(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Cleans up all test data in the correct order.
     * When using ON DELETE CASCADE in the schema, you can delete parent tables directly.
     */
    @Transactional
    public void cleanAllTables() {
        // With ON DELETE CASCADE, we can simply disable referential integrity
        // for faster cleanup and re-enable it after
        cleanAllTablesWithoutCascade();
    }
    
    /**
     * Disables referential integrity constraints, cleans all tables, and re-enables constraints.
     * This is the most efficient way to clean test data in H2.
     */
    @Transactional
    public void cleanAllTablesWithoutCascade() {
        // Disable foreign key checks temporarily (for H2 database)
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        
        // Delete from all tables
        entityManager.createNativeQuery("DELETE FROM spin_histories").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM participant_events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM rewards").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM golden_hours").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM event_locations").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM participants").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM provinces").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM regions").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM user_roles").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM blacklisted_tokens").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM roles").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM audit_logs").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM configurations").executeUpdate();
        
        // Reset identity sequences
        entityManager.createNativeQuery("ALTER TABLE events ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE regions ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE event_locations ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE golden_hours ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE rewards ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE provinces ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE participants ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE participant_events ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE spin_histories ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE users ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE roles ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE blacklisted_tokens ALTER COLUMN id RESTART WITH 1").executeUpdate();
        
        // Re-enable foreign key checks
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }
    
    /**
     * Inserts test data needed for most repository tests.
     * Use this when you need to ensure base test data is available.
     */
    @Transactional
    public void insertBasicTestData() {
        // Insert test events
        entityManager.createNativeQuery(
            "INSERT INTO events (id, version, code, name, description, start_time, end_time, status, created_by, updated_by, deleted) " +
            "VALUES (1, 0, 'EVENT001', 'Test Event 1', 'Test Description 1', " +
            "DATEADD('HOUR', -1, CURRENT_TIMESTAMP), DATEADD('HOUR', 23, CURRENT_TIMESTAMP), " +
            "'ACTIVE', 'system', 'system', false)"
        ).executeUpdate();
        
        // Insert test regions
        entityManager.createNativeQuery(
            "INSERT INTO regions (id, version, code, name, status, created_by, updated_by, deleted) " +
            "VALUES (1, 0, 'REGION001', 'North Region', 'ACTIVE', 'system', 'system', false)"
        ).executeUpdate();
        
        // Insert test event locations
        entityManager.createNativeQuery(
            "INSERT INTO event_locations (id, version, event_id, region_id, status, max_spin, name, code, created_by, updated_by, deleted) " +
            "VALUES (1, 0, 1, 1, 'ACTIVE', 3, 'Location 1', 'LOC001', 'system', 'system', false)"
        ).executeUpdate();
    }
    
    /**
     * Inserts event location test data specifically for the EventLocationRepository tests.
     * This method creates data that matches exactly the expectations in EventLocationRepositoryTest.
     */
    @Transactional
    public void insertEventLocationTestData() {
        // Insert multiple events with fixed IDs - match test's expected data
        entityManager.createNativeQuery(
            "INSERT INTO events (id, version, code, name, description, start_time, end_time, status, created_by, updated_by, deleted) VALUES " +
            "(1, 0, 'EVENT001', 'Test Event 1', 'Test Description 1', DATEADD('HOUR', -1, CURRENT_TIMESTAMP), DATEADD('HOUR', 23, CURRENT_TIMESTAMP), 'ACTIVE', 'system', 'system', false)"
        ).executeUpdate();
        
        // Insert regions with fixed IDs - match test's expected data
        entityManager.createNativeQuery(
            "INSERT INTO regions (id, version, code, name, status, created_by, updated_by, deleted) VALUES " +
            "(1, 0, 'REGION001', 'North Region', 'ACTIVE', 'system', 'system', false)," +
            "(2, 0, 'REGION002', 'South Region', 'ACTIVE', 'system', 'system', false)"
        ).executeUpdate();
        
        // Insert exactly 4 event locations for Event ID 1 with codes that match test expectations
        entityManager.createNativeQuery(
            "INSERT INTO event_locations (id, version, event_id, region_id, status, max_spin, name, code, created_by, updated_by, deleted) VALUES " +
            "(1, 0, 1, 1, 'ACTIVE', 3, 'Location 1', 'LOC1', 'system', 'system', false)," +
            "(2, 0, 1, 1, 'INACTIVE', 0, 'Location 2', 'LOC2', 'system', 'system', false)," +
            "(3, 0, 1, 1, 'ACTIVE', 0, 'Location 3', 'LOC3', 'system', 'system', false)," +
            "(4, 0, 1, 2, 'ACTIVE', 5, 'Location 4', 'LOC4', 'system', 'system', false)"
        ).executeUpdate();
    }
}
