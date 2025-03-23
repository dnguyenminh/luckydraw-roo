package vn.com.fecredit.app.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditLogTest {

    @Test
    void testSetAndGetFields() {
        AuditLog auditLog = new AuditLog();
        // ...existing code...
        auditLog.setUsername("testUser");
        assertEquals("testUser", auditLog.getUsername());
    }
}
