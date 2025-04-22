package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Unit tests for the AuditLog entity.
 * Tests the business logic and validation rules of audit logs.
 */
class AuditLogTest  {

    private AuditLog auditLog;
    private final LocalDateTime testTime = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        auditLog = AuditLog.builder()
                .objectType("User")
                .objectId(Long.toString(1l))
                .oldValue("{\"username\":\"oldName\"}")
                .newValue("{\"username\":\"newName\"}")
                .valueType("String")
                .updateTime(testTime)
                .propertyPath("username")
                .status(CommonStatus.ACTIVE)
                .build();

        auditLog.setCreatedBy("testUser");
        auditLog.setUpdatedBy("testUser");
    }

    @Test
    void testValidAuditLogState() {
        // When & Then
        assertDoesNotThrow(() -> auditLog.validateState());
    }

    @Test
    void testValidateState_WithNullObjectType_ShouldThrowException() {
        // Given
        auditLog.setObjectType(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> auditLog.validateState());
    }

    @Test
    void testValidateState_WithEmptyObjectType_ShouldThrowException() {
        // Given
        auditLog.setObjectType("  ");

        // When & Then
        assertThrows(IllegalStateException.class, () -> auditLog.validateState());
    }

    @Test
    void testValidateState_WithNullObjectId_ShouldThrowException() {
        // Given
        auditLog.setObjectId(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> auditLog.validateState());
    }

    @Test
    void testValidateState_WithNullUpdateTime_ShouldAutomaticallySet() {
        // Given
        auditLog.setUpdateTime(null);

        // When
        auditLog.validateState();

        // Then
        assertNotNull(auditLog.getUpdateTime());
    }

    @Test
    void testValidateState_WithBothValuesNull_ShouldThrowException() {
        // Given
        auditLog.setOldValue(null);
        auditLog.setNewValue(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> auditLog.validateState());
    }

    @Test
    void testValidateState_WithBothValuesEmpty_ShouldThrowException() {
        // Given
        auditLog.setOldValue("");
        auditLog.setNewValue("");

        // When & Then
        assertThrows(IllegalStateException.class, () -> auditLog.validateState());
    }

    @Test
    void testValidateState_WithOnlyOldValue_ShouldPass() {
        // Given
        auditLog.setNewValue(null);
        auditLog.setOldValue("oldValue");

        // When & Then
        assertDoesNotThrow(() -> auditLog.validateState());
    }

    @Test
    void testValidateState_WithOnlyNewValue_ShouldPass() {
        // Given
        auditLog.setOldValue(null);
        auditLog.setNewValue("newValue");

        // When & Then
        assertDoesNotThrow(() -> auditLog.validateState());
    }

    @Test
    void testCreateAuditEntry_ShouldCreateValidEntity() {
        // When
        AuditLog createdLog = AuditLog.createAuditEntry(
            "User",
            1L,
            "oldData",
            "newData",
            "String",
            "user.name",
            "testUser"
        );

        // Then
        assertNotNull(createdLog);
        assertEquals("User", createdLog.getObjectType());
        assertEquals(Long.toString(1L), createdLog.getObjectId());
        assertEquals("oldData", createdLog.getOldValue());
        assertEquals("newData", createdLog.getNewValue());
        assertEquals("String", createdLog.getValueType());
        assertEquals("user.name", createdLog.getPropertyPath());
        assertEquals("testUser", createdLog.getCreatedBy());
        assertEquals("testUser", createdLog.getUpdatedBy());
        assertEquals(AuditLog.ActionType.MODIFIED, createdLog.getActionType());
        assertEquals(CommonStatus.ACTIVE, createdLog.getStatus());
        assertNotNull(createdLog.getCreatedAt());
        assertNotNull(createdLog.getUpdatedAt());
        assertNotNull(createdLog.getUpdateTime());
    }
}
