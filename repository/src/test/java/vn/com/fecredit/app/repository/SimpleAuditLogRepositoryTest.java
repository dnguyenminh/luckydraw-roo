package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.AuditLog;

/**
 * Simple test for AuditLogRepository functionality
 * This test uses JPA with Spring Boot test support
 */
@Slf4j
@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"})
public class SimpleAuditLogRepositoryTest {
    
    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void testFindByObjectType() {
        // Find all audit logs with object type User
        List<AuditLog> userLogs = auditLogRepository.findByObjectType("User");
        
        // Verify results
        assertThat(userLogs).isNotEmpty();
        assertThat(userLogs).hasSize(1);
        log.info("Found {} audit logs with object_type = 'User'", userLogs.size());
        
        // Verify the User audit log properties
        AuditLog userLog = userLogs.get(0);
        assertThat(userLog.getObjectId()).isEqualTo("1");
        assertThat(userLog.getPropertyPath()).isEqualTo("username");
        assertThat(userLog.getNewValue()).isEqualTo("admin");
    }

    @Test
    void testFindByPropertyPath() {
        // Find all audit logs with property path "name"
        List<AuditLog> nameLogs = auditLogRepository.findByPropertyPath("name");
        
        // Verify results
        assertThat(nameLogs).isNotEmpty();
        assertThat(nameLogs).hasSize(1);
        log.info("Found {} audit logs with property_path = 'name'", nameLogs.size());
        
        // Verify the name audit log properties
        AuditLog nameLog = nameLogs.get(0);
        assertThat(nameLog.getObjectType()).isEqualTo("Event");
        assertThat(nameLog.getOldValue()).isEqualTo("Old Event Name");
        assertThat(nameLog.getNewValue()).isEqualTo("Summer Festival");
    }
}
