package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleType;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.service.impl.RoleServiceImpl;


class RoleServiceTest extends AbstractServiceTest{

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder()
                .id(1L)
                .roleType(RoleType.ROLE_ADMIN)
                .description("Admin Role")
                .displayOrder(1)
                .status(CommonStatus.ACTIVE)
                .build();

        userRole = Role.builder()
                .id(2L)
                .roleType(RoleType.ROLE_USER)
                .description("User Role")
                .displayOrder(2)
                .status(CommonStatus.ACTIVE)
                .build();
    }

    @Test
    void findById_ShouldReturnRole_WhenExists() {
        // given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));

        // when
        Optional<Role> result = roleService.findById(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getRoleType()).isEqualTo(RoleType.ROLE_ADMIN);
        verify(roleRepository).findById(1L);
    }

    @Test
    void findByRoleType_ShouldReturnRole_WhenExists() {
        // given
        when(roleRepository.findByRoleType(RoleType.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));

        // when
        Optional<Role> result = roleService.findByRoleType(RoleType.ROLE_ADMIN);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(roleRepository).findByRoleType(RoleType.ROLE_ADMIN);
    }

    @Test
    void findAll_ShouldReturnAllRoles() {
        // given
        List<Role> roles = Arrays.asList(adminRole, userRole);
        when(roleRepository.findAll()).thenReturn(roles);

        // when
        List<Role> result = roleService.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("roleType")  // Fixed from "roleName" to "roleType"
                .containsExactly(RoleType.ROLE_ADMIN, RoleType.ROLE_USER);
        verify(roleRepository).findAll();
    }

    @Test
    void findByStatus_ShouldReturnFilteredRoles() {
        // given
        List<Role> roles = Arrays.asList(adminRole, userRole);
        when(roleRepository.findByStatus(CommonStatus.ACTIVE)).thenReturn(roles);

        // when
        List<Role> result = roleService.findByStatus(CommonStatus.ACTIVE);

        // then
        assertThat(result).hasSize(2);
        verify(roleRepository).findByStatus(CommonStatus.ACTIVE);
    }

    @Test
    void findByStatusOrdered_ShouldReturnOrderedRoles() {
        // given
        List<Role> roles = Arrays.asList(adminRole, userRole);
        when(roleRepository.findByStatusOrderByDisplayOrderAsc(CommonStatus.ACTIVE)).thenReturn(roles);

        // when
        List<Role> result = roleService.findByStatusOrdered(CommonStatus.ACTIVE);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDisplayOrder()).isLessThan(result.get(1).getDisplayOrder());
        verify(roleRepository).findByStatusOrderByDisplayOrderAsc(CommonStatus.ACTIVE);
    }

    @Test
    void existsByRoleType_ShouldReturnTrue_WhenExists() {
        // given
        when(roleRepository.existsByRoleType(RoleType.ROLE_ADMIN)).thenReturn(true);
        when(roleRepository.existsByRoleType(RoleType.ROLE_MANAGER)).thenReturn(false);

        // when & then
        assertThat(roleService.existsByRoleType(RoleType.ROLE_ADMIN)).isTrue();
        assertThat(roleService.existsByRoleType(RoleType.ROLE_MANAGER)).isFalse();
    }

    @Test
    void save_ShouldReturnSavedRole() {
        // given
        Role roleToSave = adminRole.toBuilder().build();
        when(roleRepository.save(any(Role.class))).thenReturn(roleToSave);

        // when
        Role savedRole = roleService.save(roleToSave);

        // then
        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getRoleType()).isEqualTo(RoleType.ROLE_ADMIN);
        verify(roleRepository).save(roleToSave);
    }

    @Test
    void delete_ShouldCallRepositoryDelete() {
        // when
        roleService.delete(adminRole);

        // then
        verify(roleRepository).delete(adminRole);
    }

    @Test
    void deleteById_ShouldCallRepositoryDeleteById() {
        // when
        roleService.deleteById(1L);

        // then
        verify(roleRepository).deleteById(1L);
    }

    @Test
    void findByUserId_ShouldReturnRoles() {
        // given
        List<Role> roles = Arrays.asList(adminRole);
        when(roleRepository.findByUserId(anyLong())).thenReturn(roles);

        // when
        List<Role> result = roleService.findByUserId(1L);

        // then
        assertThat(result).hasSize(1);
        verify(roleRepository).findByUserId(1L);
    }

    @Test
    void findByPermissionName_ShouldReturnRoles() {
        // given
        List<Role> roles = Arrays.asList(adminRole);
        when(roleRepository.findByPermissionName(anyString())).thenReturn(roles);

        // when
        List<Role> result = roleService.findByPermissionName("VIEW_USERS");

        // then
        assertThat(result).hasSize(1);
        verify(roleRepository).findByPermissionName("VIEW_USERS");
    }
}
