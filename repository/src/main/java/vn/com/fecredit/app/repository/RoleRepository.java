package vn.com.fecredit.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.entity.Permission;
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

    /**
     * Find roles that contain any of the specified permissions
     * @param permissions Set of permissions to search for
     * @return List of roles containing any of the specified permissions
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p IN :permissions")
    List<Role> findByPermissionsIn(@Param("permissions") Set<Permission> permissions);
    
    /**
     * Find roles that contain all of the specified permissions
     * @param permissions Set of permissions that must all be present
     * @return List of roles containing all specified permissions
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.permissions) >= SIZE(:permissions) AND " +
           "SIZE((SELECT p FROM r.permissions p WHERE p IN :permissions)) = SIZE(:permissions)")
    List<Role> findByPermissionsContainingAll(@Param("permissions") Set<Permission> permissions);
    
    /**
     * Find roles that contain the specified permission
     * @param permission The permission to search for
     * @return List of roles containing the permission
     */
    List<Role> findByPermissionsContaining(Permission permission);

    List<Role> findByDisplayOrderLessThanOrderByDisplayOrderAsc(Integer displayOrderIsLessThan);

    long countByStatus(@NotNull CommonStatus status);

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
