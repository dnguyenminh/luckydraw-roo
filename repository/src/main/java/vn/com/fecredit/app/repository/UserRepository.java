package vn.com.fecredit.app.repository;

import jakarta.validation.constraints.NotNull;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.enums.CommonStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository for User entity operations.
 */
public interface UserRepository extends SimpleObjectRepository<User> {
    /**
     * Checks if a user with the given username exists
     *
     * @param username the username to check
     * @return true if a user with the username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user with the given email exists
     *
     * @param email the email to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user by their username
     *
     * @param username the username to search for
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address
     *
     * @param email the email address to search for
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<User> findByEmail(String email);

    List<User> findByStatus(@NotNull CommonStatus status);

    List<User> findByUsernameContainingIgnoreCase(String username);
}
