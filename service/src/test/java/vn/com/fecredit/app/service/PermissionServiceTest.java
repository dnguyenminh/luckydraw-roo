package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import vn.com.fecredit.app.entity.Permission;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleType;
import vn.com.fecredit.app.repository.PermissionRepository;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.service.impl.PermissionServiceImpl;


class PermissionServiceTest extends AbstractServiceTest{

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private Permission viewUsersPermission;
    private Permission createUserPermission;

    @BeforeEach
    void setUp() {
        viewUsersPermission = Permission.builder()
                .id(1L)
                .name("VIEW_USERS")
                .description("Permission to view users")
                .status(CommonStatus.ACTIVE)
                .build();

        createUserPermission = Permission.builder()
                .id(2L)
                .name("CREATE_USER")
                .description("Permission to create users")
                .status(CommonStatus.ACTIVE)
                .build();
    }

    @Test
    void findById_ShouldReturnPermission_WhenExists() {
        // given
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(viewUsersPermission));

        // when
        Optional<Permission> result = permissionService.findById(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("VIEW_USERS");
        verify(permissionRepository).findById(1L);
    }

    @Test
    void findByName_ShouldReturnPermission_WhenExists() {
        // given
        when(permissionRepository.findByName("VIEW_USERS")).thenReturn(Optional.of(viewUsersPermission));

        // when
        Optional<Permission> result = permissionService.findByName("VIEW_USERS");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(permissionRepository).findByName("VIEW_USERS");
    }

    @Test
    void findAll_ShouldReturnAllPermissions() {
        // given
        List<Permission> permissions = Arrays.asList(viewUsersPermission, createUserPermission);
        when(permissionRepository.findAll()).thenReturn(permissions);

        // when
        List<Permission> result = permissionService.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name")
                .containsExactly("VIEW_USERS", "CREATE_USER");
        verify(permissionRepository).findAll();
    }

    @Test
    void findByStatus_ShouldReturnFilteredPermissions() {
        // given
        List<Permission> activePermissions = Arrays.asList(viewUsersPermission, createUserPermission);
        when(permissionRepository.findByStatus(CommonStatus.ACTIVE)).thenReturn(activePermissions);

        // when
        List<Permission> result = permissionService.findByStatus(CommonStatus.ACTIVE);

        // then
        assertThat(result).hasSize(2);
        verify(permissionRepository).findByStatus(CommonStatus.ACTIVE);
    }

    @Test
    void existsByName_ShouldReturnTrue_WhenExists() {
        // given
        when(permissionRepository.existsByName("VIEW_USERS")).thenReturn(true);
        when(permissionRepository.existsByName("UNKNOWN")).thenReturn(false);

        // when & then
        assertThat(permissionService.existsByName("VIEW_USERS")).isTrue();
        assertThat(permissionService.existsByName("UNKNOWN")).isFalse();
    }

    @Test
    void save_ShouldReturnSavedPermission() {
        // given
        Permission toSave = viewUsersPermission.toBuilder().build();
        when(permissionRepository.save(any())).thenReturn(toSave);

        // when
        Permission saved = permissionService.save(toSave);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("VIEW_USERS");
        verify(permissionRepository).save(toSave);
    }

    @Test
    void delete_ShouldCallRepositoryDelete() {
        // when
        permissionService.delete(viewUsersPermission);

        // then
        verify(permissionRepository).delete(viewUsersPermission);
    }

    @Test
    void deleteById_ShouldCallRepositoryDeleteById() {
        // when
        permissionService.deleteById(1L);

        // then
        verify(permissionRepository).deleteById(1L);
    }

    @Test
    void findByRoleId_ShouldReturnPermissions() {
        // given
        List<Permission> permissions = Arrays.asList(viewUsersPermission);
        when(permissionRepository.findByRoleId(anyLong())).thenReturn(permissions);

        // when
        List<Permission> result = permissionService.findByRoleId(1L);

        // then
        assertThat(result).hasSize(1);
        verify(permissionRepository).findByRoleId(1L);
    }

    @Test
    void findActiveByRoleId_ShouldReturnActivePermissions() {
        // given
        List<Permission> permissions = Arrays.asList(viewUsersPermission);
        when(permissionRepository.findActiveByRoleId(anyLong())).thenReturn(permissions);

        // when
        List<Permission> result = permissionService.findActiveByRoleId(1L);

        // then
        assertThat(result).hasSize(1);
        verify(permissionRepository).findActiveByRoleId(1L);
    }

    @Test
    void findNotInRole_ShouldReturnUnassignedPermissions() {
        // given
        List<Permission> permissions = Arrays.asList(createUserPermission);
        when(permissionRepository.findNotInRole(anyLong())).thenReturn(permissions);

        // when
        List<Permission> result = permissionService.findNotInRole(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("CREATE_USER");
        verify(permissionRepository).findNotInRole(1L);
    }

    @Test
    void addPermissionToRole_ShouldAddPermissionToRole() {
        // given
        Role role = new Role();
        role.setId(1L);
        role.setRoleType(RoleType.ROLE_ADMIN);
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(viewUsersPermission));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        
        // when
        permissionService.addPermissionToRole(1L, 1L);
        
        // then
        verify(permissionRepository).findById(1L);
        verify(roleRepository).findById(1L);
        verify(roleRepository).save(role);
        assertThat(role.getPermissions()).contains(viewUsersPermission);
    }

    @Test
    void removePermissionFromRole_ShouldRemovePermissionFromRole() {
        // given
        Role role = new Role();
        role.setId(1L);
        role.setRoleType(RoleType.ROLE_ADMIN);
        role.addPermission(viewUsersPermission);
        
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(viewUsersPermission));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        
        // when
        permissionService.removePermissionFromRole(1L, 1L);
        
        // then
        verify(permissionRepository).findById(1L);
        verify(roleRepository).findById(1L);
        verify(roleRepository).save(role);
        assertThat(role.getPermissions()).doesNotContain(viewUsersPermission);
    }
}
