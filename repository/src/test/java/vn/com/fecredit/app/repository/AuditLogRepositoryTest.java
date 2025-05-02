package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@ExtendWith(MockitoExtension.class)
class AuditLogRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        // Clear existing data first
        auditLogRepository.deleteAllInBatch();

        now = LocalDateTime.now();

        // Create sample audit logs with all required fields
        auditLogRepository.save(createTestAuditLog("User", 1L, "username", "oldUser", "newUser"));
        auditLogRepository.save(createTestAuditLog("User", 2L, "email", "old@test.com", "new@test.com"));
        auditLogRepository.save(createTestAuditLog("Role", 1L, "name", "USER", "ADMIN"));
    }

    @Test
    void testFindByObjectType() {
        // When
        List<AuditLog> foundLogs = auditLogRepository.findByObjectType("User");

        // Then
        assertThat(foundLogs).hasSize(2);
    }

    // Helper method to create test audit logs
    private AuditLog createTestAuditLog(String objectType, Long objectId, String propertyPath,
                                        String oldValue, String newValue) {
        AuditLog log = AuditLog.builder()
                .objectType(objectType)
                .objectId(Long.toString(objectId))
                .propertyPath(propertyPath)
                .oldValue(oldValue)
                .newValue(newValue)
                .valueType("String")
                .actionType(AuditLog.ActionType.MODIFIED)
                .updateTime(now)
                .status(CommonStatus.ACTIVE)
                .createdBy("test-user")
                .updatedBy("test-user")
                .build();

        // Set all required fields that might be missing
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        log.setVersion(0L);

        return log;
    }
}
