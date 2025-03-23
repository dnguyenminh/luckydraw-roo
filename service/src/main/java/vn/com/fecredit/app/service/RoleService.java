package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.enums.RoleName;
import vn.com.fecredit.app.service.base.AbstractService;

import java.util.List;
import java.util.Optional;

public interface RoleService extends AbstractService<Role> {
    Optional<Role> findByRoleName(RoleName name);
    List<Role> findByDisplayOrderGreaterThan(Integer minOrder);
    List<Role> findByUserEmail(String userEmail);
}
