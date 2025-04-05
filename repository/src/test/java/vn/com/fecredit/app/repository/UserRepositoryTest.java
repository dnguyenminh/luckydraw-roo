package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleType;

public class UserRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    
    @Test
    public void whenFindByUsername_thenReturnUser() {
        // Create a User with all required fields properly set
        User user = User.builder()
            .username("testuser")
            .password("password")
            .email("test@example.com")
            .fullName("Test User")  // Add the full_name which is required
            .enabled(true)
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .createdBy("test-user")
            .updatedBy("test-user")
            .roles(new HashSet<>())
            .role(RoleType.ROLE_USER) // Set the required role field to avoid null constraint violation
            .blacklistedTokens(new HashSet<>())
            .build();
        
        // Save the user
        userRepository.save(user);
        
        // Find the user
        User found = userRepository.findByUsername(user.getUsername()).orElse(null);
        
        // Assert that the user is found
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo(user.getUsername());
        assertThat(found.getFullName()).isEqualTo("Test User");
    }
}
