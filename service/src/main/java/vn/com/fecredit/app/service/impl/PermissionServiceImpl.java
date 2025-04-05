package vn.com.fecredit.app.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.Permission;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.repository.PermissionRepository;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.service.PermissionService;

/**
 * Implementation of the PermissionService interface.
 * Provides permission management operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    
    @Override
    public Permission save(Permission permission) {
        log.debug("Saving permission: {}", permission);
        return permissionRepository.save(permission);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Permission> findById(Long id) {
        log.debug("Finding permission by ID: {}", id);
        return permissionRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Permission> findByName(String name) {
        log.debug("Finding permission by name: {}", name);
        return permissionRepository.findByName(name);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Permission> findAll() {
        log.debug("Finding all permissions");
        return permissionRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Permission> findByStatus(CommonStatus status) {
        log.debug("Finding permissions by status: {}", status);
        return permissionRepository.findByStatus(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        log.debug("Checking if permission exists by name: {}", name);
        return permissionRepository.existsByName(name);
    }
    
    @Override
    public void delete(Permission permission) {
        log.debug("Deleting permission: {}", permission);
        permissionRepository.delete(permission);
    }
    
    @Override
    public void deleteById(Long id) {
        log.debug("Deleting permission by ID: {}", id);
        permissionRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Permission> findByRoleId(Long roleId) {
        log.debug("Finding permissions by role ID: {}", roleId);
        return permissionRepository.findByRoleId(roleId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Permission> findActiveByRoleId(Long roleId) {
        log.debug("Finding active permissions by role ID: {}", roleId);
        return permissionRepository.findActiveByRoleId(roleId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Permission> findNotInRole(Long roleId) {
        log.debug("Finding permissions not in role: {}", roleId);
        return permissionRepository.findNotInRole(roleId);
    }
    
    @Override
    @Transactional
    public void addPermissionToRole(Long permissionId, Long roleId) {
        log.debug("Adding permission {} to role {}", permissionId, roleId);
        
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionId));
            
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
            
        role.addPermission(permission);
        roleRepository.save(role);
    }
    
    @Override
    @Transactional
    public void removePermissionFromRole(Long permissionId, Long roleId) {
        log.debug("Removing permission {} from role {}", permissionId, roleId);
        
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionId));
            
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
            
        role.removePermission(permission);
        roleRepository.save(role);
    }
}
