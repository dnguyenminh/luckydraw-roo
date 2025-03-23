package vn.com.fecredit.app.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PermissionTest {

    @Test
    void testSetAndGetFields() {
        Permission permission = new Permission();
        // ...existing code...
        permission.setName("READ");
        assertEquals("READ", permission.getName());
    }
}
