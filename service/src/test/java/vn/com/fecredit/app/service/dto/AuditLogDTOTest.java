package vn.com.fecredit.app.service.dto;

import org.junit.jupiter.api.Test;
import vn.com.fecredit.app.entity.enums.ActionType;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogDTOTest {

    @Test
    void actionCountDTO_ShouldStoreAndRetrieveValues() {
        // Given
        ActionCountDTO dto = new ActionCountDTO(ActionType.LOGIN, 42L);
        
        // Then
        assertEquals(ActionType.LOGIN, dto.getActionType());
        assertEquals(42L, dto.getCount());
    }
    
    @Test
    void actionCountDTO_ShouldHandleNoArgsConstruction() {
        // Given
        ActionCountDTO dto = new ActionCountDTO();
        
        // When
        dto.setActionType(ActionType.UPDATE);
        dto.setCount(10L);
        
        // Then
        assertEquals(ActionType.UPDATE, dto.getActionType());
        assertEquals(10L, dto.getCount());
    }
    
    @Test
    void userActivityDTO_ShouldStoreAndRetrieveValues() {
        // Given
        UserActivityDTO dto = new UserActivityDTO("testUser", 15L);
        
        // Then
        assertEquals("testUser", dto.getUsername());
        assertEquals(15L, dto.getActionCount());
    }
    
    @Test
    void userActivityDTO_ShouldHandleNoArgsConstruction() {
        // Given
        UserActivityDTO dto = new UserActivityDTO();
        
        // When
        dto.setUsername("admin");
        dto.setActionCount(30L);
        
        // Then
        assertEquals("admin", dto.getUsername());
        assertEquals(30L, dto.getActionCount());
    }
    
    @Test
    void testEqualsAndHashCode() {
        // ActionCountDTO equals and hashCode
        ActionCountDTO dto1 = new ActionCountDTO(ActionType.CREATE, 5L);
        ActionCountDTO dto2 = new ActionCountDTO(ActionType.CREATE, 5L);
        ActionCountDTO dto3 = new ActionCountDTO(ActionType.DELETE, 5L);
        
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        // UserActivityDTO equals and hashCode
        UserActivityDTO user1 = new UserActivityDTO("user", 10L);
        UserActivityDTO user2 = new UserActivityDTO("user", 10L);
        UserActivityDTO user3 = new UserActivityDTO("admin", 10L);
        
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
    }
}
