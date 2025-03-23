package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.enums.RoleName;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.repository.UserRepository;
import vn.com.fecredit.app.service.exception.EntityNotFoundException;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .status(CommonStatus.ACTIVE)
                .roles(new HashSet<>())
                .build();

        adminRole = Role.builder()
                .id(1L)
                .roleName(RoleName.ADMIN)
                .status(CommonStatus.ACTIVE)
                .users(new HashSet<>())
                .build();

        userRole = Role.builder()
                .id(2L)
                .roleName(RoleName.USER)
                .status(CommonStatus.ACTIVE)
                .users(new HashSet<>())
                .build();
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findByUsername("testuser");

        // Then
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenUserExists() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findByEmail("test@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenUserExists() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        boolean result = userService.existsByUsername("testuser");

        // Then
        assertTrue(result);
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenUserExists() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        boolean result = userService.existsByEmail("test@example.com");

        // Then
        assertTrue(result);
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void addRole_ShouldAddRoleToUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleName(RoleName.ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userService.addRole(1L, RoleName.ADMIN);

        // Then
        verify(userRepository).findById(1L);
        verify(roleRepository).findByRoleName(RoleName.ADMIN);
        verify(userRepository).save(user);
        assertTrue(user.getRoles().contains(adminRole));
        assertTrue(adminRole.getUsers().contains(user));
    }

    @Test
    void addRole_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> userService.addRole(99L, RoleName.ADMIN));
        verify(userRepository).findById(99L);
        verify(roleRepository, never()).findByRoleName(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void addRole_ShouldThrowException_WhenRoleNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleName(RoleName.MANAGER)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> userService.addRole(1L, RoleName.MANAGER));
        verify(userRepository).findById(1L);
        verify(roleRepository).findByRoleName(RoleName.MANAGER);
        verify(userRepository, never()).save(any());
    }

    @Test
    void removeRole_ShouldRemoveRoleFromUser() {
        // Given
        user.getRoles().add(userRole);
        userRole.getUsers().add(user);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleName(RoleName.USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userService.removeRole(1L, RoleName.USER);

        // Then
        verify(userRepository).findById(1L);
        verify(roleRepository).findByRoleName(RoleName.USER);
        verify(userRepository).save(user);
        assertFalse(user.getRoles().contains(userRole));
        assertFalse(userRole.getUsers().contains(user));
    }

    @Test
    void removeRole_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> userService.removeRole(99L, RoleName.USER));
        verify(userRepository).findById(99L);
        verify(roleRepository, never()).findByRoleName(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void removeRole_ShouldThrowException_WhenRoleNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleName(RoleName.MANAGER)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> userService.removeRole(1L, RoleName.MANAGER));
        verify(userRepository).findById(1L);
        verify(roleRepository).findByRoleName(RoleName.MANAGER);
        verify(userRepository, never()).save(any());
    }
}
