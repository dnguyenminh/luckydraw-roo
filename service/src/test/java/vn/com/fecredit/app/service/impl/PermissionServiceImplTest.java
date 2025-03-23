package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.Permission;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.enums.PermissionType;
import vn.com.fecredit.app.entity.enums.RoleName;
import vn.com.fecredit.app.repository.PermissionRepository;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.service.exception.EntityNotFoundException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private Permission readPermission;
    private Permission writePermission;
    private Permission adminPermission;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        readPermission = Permission.builder()
                .id(1L)
                .name("user:read")
                .type(PermissionType.READ)
                .status(CommonStatus.ACTIVE)
                .roles(new HashSet<>())
                .build();

        writePermission = Permission.builder()
                .id(2L)
                .name("user:write")
                .type(PermissionType.WRITE)
                .status(CommonStatus.ACTIVE)
                .roles(new HashSet<>())
                .build();

        adminPermission = Permission.builder()
                .id(3L)
                .name("user:admin")
                .type(PermissionType.ADMIN)
                .status(CommonStatus.ACTIVE)
                .roles(new HashSet<>())
                .build();

        adminRole = Role.builder()
                .id(1L)
                .roleName(RoleName.ADMIN)
                .permissions(new HashSet<>())
                .users(new HashSet<>())
                .status(CommonStatus.ACTIVE)
                .build();

        // Setup initial permissions on role
        adminRole.getPermissions().add(readPermission);
        adminRole.getPermissions().add(writePermission);
        readPermission.getRoles().add(adminRole);
        writePermission.getRoles().add(adminRole);
    }

    @Test
    void findByName_ShouldReturnPermission_WhenPermissionExists() {
        // Given
        when(permissionRepository.findByName("user:read")).thenReturn(Optional.of(readPermission));

        // When
        Optional<Permission> result = permissionService.findByName("user:read");

        // Then
        assertTrue(result.isPresent());
        assertEquals("user:read", result.get().getName());
        verify(permissionRepository).findByName("user:read");
    }

    @Test
    void findByType_ShouldReturnPermissions() {
        // Given
        when(permissionRepository.findByType(PermissionType.READ))
                .thenReturn(Collections.singletonList(readPermission));

        // When
        List<Permission> result = permissionService.findByType(PermissionType.READ);

        // Then
        assertEquals(1, result.size());
        assertEquals("user:read", result.get(0).getName());
        verify(permissionRepository).findByType(PermissionType.READ);
    }

    @Test
    void findByRoleId_ShouldReturnPermissions() {
        // Given
        Long roleId = 1L;
        when(permissionRepository.findByRolesId(roleId))
                .thenReturn(Arrays.asList(readPermission, writePermission));

        // When
        List<Permission> result = permissionService.findByRoleId(roleId);

        // Then
        assertEquals(2, result.size());
        verify(permissionRepository).findByRolesId(roleId);
    }

    @Test
    void findByUserRole_ShouldReturnPermissions() {
        // Given
        Long userId = 1L;
        Long roleId = 1L;
        when(permissionRepository.findByUserRole(userId, roleId))
                .thenReturn(Arrays.asList(readPermission, writePermission));

        // When
        List<Permission> result = permissionService.findByUserRole(userId, roleId);

        // Then
        assertEquals(2, result.size());
        verify(permissionRepository).findByUserRole(userId, roleId);
    }

    @Test
    void addPermissionToRole_ShouldAddPermission() {
        // Given
        when(permissionRepository.findById(3L)).thenReturn(Optional.of(adminPermission));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        permissionService.addPermissionToRole(3L, 1L);

        // Then
        verify(permissionRepository).findById(3L);
        verify(roleRepository).findById(1L);
        verify(roleRepository).save(adminRole);
        assertTrue(adminRole.getPermissions().contains(adminPermission));
    }

    @Test
    void addPermissionToRole_ShouldThrowException_WhenPermissionNotFound() {
        // Given
        when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> permissionService.addPermissionToRole(99L, 1L));
        verify(permissionRepository).findById(99L);
        verify(roleRepository, never()).findById(anyLong());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void addPermissionToRole_ShouldThrowException_WhenRoleNotFound() {
        // Given
        when(permissionRepository.findById(3L)).thenReturn(Optional.of(adminPermission));
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> permissionService.addPermissionToRole(3L, 99L));
        verify(permissionRepository).findById(3L);
        verify(roleRepository).findById(99L);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void removePermissionFromRole_ShouldRemovePermission() {
        // Given
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(readPermission));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        permissionService.removePermissionFromRole(1L, 1L);

        // Then
        verify(permissionRepository).findById(1L);
        verify(roleRepository).findById(1L);
        verify(roleRepository).save(adminRole);
        assertFalse(adminRole.getPermissions().contains(readPermission));
    }

    @Test
    void removePermissionFromRole_ShouldThrowException_WhenPermissionNotFound() {
        // Given
        when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> permissionService.removePermissionFromRole(99L, 1L));
        verify(permissionRepository).findById(99L);
        verify(roleRepository, never()).findById(anyLong());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void removePermissionFromRole_ShouldThrowException_WhenRoleNotFound() {
        // Given
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(readPermission));
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> permissionService.removePermissionFromRole(1L, 99L));
        verify(permissionRepository).findById(1L);
        verify(roleRepository).findById(99L);
        verify(roleRepository, never()).save(any(Role.class));
    }
}
