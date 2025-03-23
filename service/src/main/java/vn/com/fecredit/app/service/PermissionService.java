package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.Permission;
import vn.com.fecredit.app.entity.enums.PermissionType;
import vn.com.fecredit.app.service.base.AbstractService;

import java.util.List;
import java.util.Optional;

public interface PermissionService extends AbstractService<Permission> {
    Optional<Permission> findByName(String name);
    List<Permission> findByType(PermissionType type);
    List<Permission> findByRoleId(Long roleId);
    List<Permission> findByUserRole(Long userId, Long roleId);
    void addPermissionToRole(Long permissionId, Long roleId);
    void removePermissionFromRole(Long permissionId, Long roleId);
}
