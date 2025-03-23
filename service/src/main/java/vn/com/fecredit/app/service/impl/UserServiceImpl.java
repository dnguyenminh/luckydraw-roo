package vn.com.fecredit.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.enums.RoleName;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.repository.UserRepository;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.service.UserService;
import vn.com.fecredit.app.service.base.AbstractServiceImpl;
import vn.com.fecredit.app.service.exception.EntityNotFoundException;

import java.util.Optional;
import java.util.List;

@Slf4j
@Service
@Transactional
public class UserServiceImpl extends AbstractServiceImpl<User> implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        super(userRepository);
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true) 
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByStatus(CommonStatus status) {
        return userRepository.findByStatus(status);
    }

    @Override
    public void addRole(Long userId, RoleName roleName) {
        User user = findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Role role = roleRepository.findByRoleName(roleName)
            .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
        user.addRole(role);
        userRepository.save(user);
    }

    @Override
    public void removeRole(Long userId, RoleName roleName) {
        User user = findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Role role = roleRepository.findByRoleName(roleName)
            .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
        user.removeRole(role);
        userRepository.save(user);
    }
}
