package vn.com.fecredit.app.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.service.UserService;

/**
 * Implementation of the UserService interface for testing purposes.
 * This implementation provides basic functionality needed for tests.
 */
@Service
public class UserServiceImpl implements UserService {

    // Mock user storage
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    /**
     * Basic implementation that returns a test user by username
     * @param username the username to look up
     * @return an Optional containing the user if found
     */
    public Optional<User> findByUsername(String username) {
        return users.values().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    /**
     * Find a user by their ID
     * @param id the user ID
     * @return an Optional containing the user if found
     */
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    /**
     * Get all users in the system
     * @return a list of all users
     */
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    /**
     * Save a user to the system
     * @param user the user to save
     * @return the saved user with ID assigned
     */
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(nextId++);
        }
        users.put(user.getId(), user);
        return user;
    }

    /**
     * Delete a user by ID
     * @param id the ID of the user to delete
     */
    public void deleteById(Long id) {
        users.remove(id);
    }

    /**
     * Check if a username is already taken
     * @param username the username to check
     * @return true if the username is already in use
     */
    public boolean existsByUsername(String username) {
        return users.values().stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    /**
     * Check if an email is already registered
     * @param email the email to check
     * @return true if the email is already in use
     */
    public boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    // Add any other methods from the UserService interface that are needed for your tests
    // The exact methods will depend on your actual UserService interface
}
