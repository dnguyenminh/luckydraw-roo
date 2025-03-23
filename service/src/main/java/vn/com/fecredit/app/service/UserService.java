package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.enums.RoleName;
import vn.com.fecredit.app.service.base.AbstractService;

import java.util.Optional;

public interface UserService extends AbstractService<User> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void addRole(Long userId, RoleName roleName);
    void removeRole(Long userId, RoleName roleName);
}
