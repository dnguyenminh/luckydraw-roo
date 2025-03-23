package vn.com.fecredit.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.entity.Permission;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.enums.PermissionType;
import vn.com.fecredit.app.repository.PermissionRepository;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.service.PermissionService;
import vn.com.fecredit.app.service.base.AbstractServiceImpl;
import vn.com.fecredit.app.service.exception.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class PermissionServiceImpl extends AbstractServiceImpl<Permission> implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        super(permissionRepository);
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> findByStatus(CommonStatus status) {
        return permissionRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Permission> findByName(String name) {
        return permissionRepository.findByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> findByType(PermissionType type) {
        return permissionRepository.findByType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> findByRoleId(Long roleId) {
        return permissionRepository.findByRolesId(roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> findByUserRole(Long userId, Long roleId) {
        return permissionRepository.findByUserRole(userId, roleId);
    }

    @Override
    @Transactional
    public void addPermissionToRole(Long permissionId, Long roleId) {
        Permission permission = findById(permissionId)
            .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));
            
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));
            
        role.getPermissions().add(permission);
        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void removePermissionFromRole(Long permissionId, Long roleId) {
        Permission permission = findById(permissionId)
            .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));
            
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));
            
        role.getPermissions().remove(permission);
        roleRepository.save(role);
    }
}
