package vn.com.fecredit.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleType;

/**
 * Repository interface for Role entity operations.
 * Provides standard CRUD operations plus custom queries.
 */
@Repository
@Transactional(readOnly = true)
public interface RoleRepository extends SimpleObjectRepository<Role> {
    
    /**
     * Find a role by its role type
     * @param roleType the role type to search for
     * @return the matching role or empty if not found
     */
    Optional<Role> findByRoleType(RoleType roleType);
    
    /**
     * Check if a role with the specified role type exists
     * @param roleType the role type to check
     * @return true if exists, false otherwise
     */
    boolean existsByRoleType(RoleType roleType);
    
    /**
     * Find roles by status, ordered by display order ascending
     * @param status the status to filter by
     * @return list of roles matching the criteria
     */
    List<Role> findByStatusOrderByDisplayOrderAsc(CommonStatus status);
    
    /**
     * Find roles by status
     * @param status the status to filter by
     * @return list of roles with the specified status
     */
    List<Role> findByStatus(CommonStatus status);
    
    /**
     * Find roles that have a specific permission
     * @param permissionName the permission name to search for
     * @return list of roles with the permission
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);
    
    /**
     * Find roles assigned to a specific user
     * @param userId the user ID to search for
     * @return list of roles assigned to the user
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findByUserId(@Param("userId") Long userId);
    
    // @Override
    // @Transactional
    // <S extends Role> S save(@NonNull S entity);
    
    // @Override
    // @Transactional
    // void delete(@NonNull Role entity);
    
    // @Override
    // @Transactional
    // void deleteById(@NonNull Long id);
    
    // @Override
    // @NonNull Optional<Role> findById(@NonNull Long id);
    
    // @Override
    // @NonNull List<Role> findAll();
    
    // @Override
    // @NonNull List<Role> findAllById(@NonNull Iterable<Long> ids);
    
    // @Override
    // boolean existsById(@NonNull Long id);
    
    // @Override
    // long count();
}