package vn.com.fecredit.app.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleType;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.service.RoleService;

/**
 * Implementation of the RoleService interface.
 * Provides business logic for role management operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {
    
    private final RoleRepository roleRepository;
    
    @Override
    public Role save(Role role) {
        log.debug("Saving Role: {}", role);
        return roleRepository.save(role);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findById(Long id) {
        log.debug("Finding Role by ID: {}", id);
        return roleRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByRoleType(RoleType roleType) {
        log.debug("Finding Role by type: {}", roleType);
        return roleRepository.findByRoleType(roleType);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Role> findAll() {
        log.debug("Finding all Roles");
        return roleRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Role> findByStatus(CommonStatus status) {
        log.debug("Finding Roles by status: {}", status);
        return roleRepository.findByStatus(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Role> findByStatusOrdered(CommonStatus status) {
        log.debug("Finding Roles by status ordered by display order: {}", status);
        return roleRepository.findByStatusOrderByDisplayOrderAsc(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByRoleType(RoleType roleType) {
        log.debug("Checking if Role exists by type: {}", roleType);
        return roleRepository.existsByRoleType(roleType);
    }
    
    @Override
    public void delete(Role role) {
        log.debug("Deleting Role: {}", role);
        roleRepository.delete(role);
    }
    
    @Override
    public void deleteById(Long id) {
        log.debug("Deleting Role by ID: {}", id);
        roleRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Role> findByUserId(Long userId) {
        log.debug("Finding Roles by user ID: {}", userId);
        return roleRepository.findByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Role> findByPermissionName(String permissionName) {
        log.debug("Finding Roles by permission name: {}", permissionName);
        return roleRepository.findByPermissionName(permissionName);
    }
}
