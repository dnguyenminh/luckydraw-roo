package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.enums.RoleName;
import vn.com.fecredit.app.repository.RoleRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role adminRole;
    private Role userRole;
    private Role managerRole;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder()
                .id(1L)
                .roleName(RoleName.ADMIN)
                .displayOrder(100)
                .description("Administrator role")
                .status(CommonStatus.ACTIVE)
                .build();

        userRole = Role.builder()
                .id(2L)
                .roleName(RoleName.USER)
                .displayOrder(1)
                .description("Regular user role")
                .status(CommonStatus.ACTIVE)
                .build();

        managerRole = Role.builder()
                .id(3L)
                .roleName(RoleName.MANAGER)
                .displayOrder(50)
                .description("Manager role")
                .status(CommonStatus.INACTIVE)
                .build();
    }

    @Test
    void findByRoleName_ShouldReturnRole_WhenRoleExists() {
        // Given
        when(roleRepository.findByRoleName(RoleName.ADMIN)).thenReturn(Optional.of(adminRole));

        // When
        Optional<Role> result = roleService.findByRoleName(RoleName.ADMIN);

        // Then
        assertTrue(result.isPresent());
        assertEquals(RoleName.ADMIN, result.get().getRoleName());
        verify(roleRepository).findByRoleName(RoleName.ADMIN);
    }

    @Test
    void findByRoleName_ShouldReturnEmpty_WhenRoleDoesNotExist() {
        // Given
        when(roleRepository.findByRoleName(RoleName.GUEST)).thenReturn(Optional.empty());

        // When
        Optional<Role> result = roleService.findByRoleName(RoleName.GUEST);

        // Then
        assertTrue(result.isEmpty());
        verify(roleRepository).findByRoleName(RoleName.GUEST);
    }

    @Test
    void findByDisplayOrderGreaterThan_ShouldReturnFilteredRoles() {
        // Given
        int minOrder = 10;
        when(roleRepository.findByDisplayOrderGreaterThan(minOrder))
                .thenReturn(Arrays.asList(adminRole, managerRole));

        // When
        List<Role> result = roleService.findByDisplayOrderGreaterThan(minOrder);

        // Then
        assertEquals(2, result.size());
        verify(roleRepository).findByDisplayOrderGreaterThan(minOrder);
    }

    @Test
    void findByUserEmail_ShouldReturnRoles() {
        // Given
        String email = "user@example.com";
        when(roleRepository.findByUsersEmail(email))
                .thenReturn(Collections.singletonList(userRole));

        // When
        List<Role> result = roleService.findByUserEmail(email);

        // Then
        assertEquals(1, result.size());
        assertEquals(RoleName.USER, result.get(0).getRoleName());
        verify(roleRepository).findByUsersEmail(email);
    }

    @Test
    void findByStatus_ShouldReturnFilteredRoles() {
        // Given
        when(roleRepository.findByStatus(CommonStatus.ACTIVE))
                .thenReturn(Arrays.asList(adminRole, userRole));

        // When
        List<Role> result = roleService.findByStatus(CommonStatus.ACTIVE);

        // Then
        assertEquals(2, result.size());
        verify(roleRepository).findByStatus(CommonStatus.ACTIVE);
    }
}
