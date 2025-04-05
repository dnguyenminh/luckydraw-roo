package vn.com.fecredit.app.service;

import java.util.List;
import java.util.Optional;

import vn.com.fecredit.app.entity.Permission;
import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Service interface for managing permissions.
 * Provides methods for creating, retrieving, and querying permissions.
 */
public interface PermissionService {

    /**
     * Save a permission
     * @param permission the permission to save
     * @return the saved permission
     */
    Permission save(Permission permission);
    
    /**
     * Find permission by ID
     * @param id the ID to search for
     * @return the permission if found
     */
    Optional<Permission> findById(Long id);
    
    /**
     * Find permission by name
     * @param name the permission name to search for
     * @return the permission if found
     */
    Optional<Permission> findByName(String name);
    
    /**
     * Find all permissions
     * @return all permissions
     */
    List<Permission> findAll();
    
    /**
     * Find permissions by status
     * @param status the status to filter by
     * @return matching permissions
     */
    List<Permission> findByStatus(CommonStatus status);
    
    /**
     * Check if a permission exists by name
     * @param name the permission name to check
     * @return true if exists
     */
    boolean existsByName(String name);
    
    /**
     * Delete a permission
     * @param permission the permission to delete
     */
    void delete(Permission permission);
    
    /**
     * Delete a permission by ID
     * @param id the ID of the permission to delete
     */
    void deleteById(Long id);
    
    /**
     * Find permissions assigned to a specific role
     * @param roleId the role ID to search for
     * @return permissions assigned to the role
     */
    List<Permission> findByRoleId(Long roleId);
    
    /**
     * Find active permissions assigned to a specific role
     * @param roleId the role ID to search for
     * @return active permissions assigned to the role
     */
    List<Permission> findActiveByRoleId(Long roleId);
    
    /**
     * Find permissions not assigned to a specific role
     * @param roleId the role ID to exclude
     * @return permissions not assigned to the role
     */
    List<Permission> findNotInRole(Long roleId);
    
    /**
     * Add a permission to a role
     * @param permissionId the permission ID
     * @param roleId the role ID
     */
    void addPermissionToRole(Long permissionId, Long roleId);
    
    /**
     * Remove a permission from a role
     * @param permissionId the permission ID
     * @param roleId the role ID
     */
    void removePermissionFromRole(Long permissionId, Long roleId);
}
