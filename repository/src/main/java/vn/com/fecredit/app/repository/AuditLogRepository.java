package vn.com.fecredit.app.repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Repository interface for AuditLog entities.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface AuditLogRepository extends AbstractRepository<AuditLog, Long> {


    /**
     * Find audit logs by username
     *
     * @param username the username who performed the actions
     * @return list of matching audit logs
     */
    List<AuditLog> findByCreatedBy(String username);

    /**
     * Find audit logs by username (creator)
     * Thread-safe implementation that handles null values
     *
     * @param username the username who performed the actions
     * @return list of matching audit logs
     */
    default List<AuditLog> findByUsername(String username) {
        // Create a defensive copy of username to avoid race conditions
        final String safeUsername = username;
        if (safeUsername == null || safeUsername.trim().isEmpty()) {
            return Collections.emptyList(); // Return empty immutable list instead of new ArrayList
        }
        return findByCreatedBy(safeUsername);
    }

    /**
     * Find audit logs by creation time range
     *
     * @param startTime the start of the time range
     * @param endTime   the end of the time range
     * @return list of matching audit logs
     */
    List<AuditLog> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find audit logs by update time range
     *
     * @param startTime the start of the time range
     * @param endTime   the end of the time range
     * @return list of matching audit logs
     */
    List<AuditLog> findByUpdateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find audit logs by status
     *
     * @param status the status to filter by
     * @return list of matching audit logs
     */
    List<AuditLog> findByStatus(CommonStatus status);

    /**
     * Find audit logs by object type
     *
     * @param objectType the object type
     * @return list of matching audit logs
     */
    List<AuditLog> findByObjectType(String objectType);

    /**
     * Find audit logs by property path
     *
     * @param propertyPath the property path
     * @return list of matching audit logs
     */
    List<AuditLog> findByPropertyPath(String propertyPath);

    /**
     * Find audit logs by object type, object id, and status
     *
     * @param objectType the object type
     * @param objectId   the object id
     * @param status     the status
     * @return list of matching audit logs
     */
    List<AuditLog> findByObjectTypeAndObjectIdAndStatus(String objectType, String objectId, CommonStatus status);

    /**
     * Find audit logs by object type and status.
     *
     * @param objectType the type of object
     * @param status     the status to filter by
     * @param pageable   pagination information
     * @return page of audit logs
     */
    Page<AuditLog> findByObjectTypeAndStatus(String objectType, CommonStatus status, Pageable pageable);
    /**
     * Find all audit logs with pagination.
     *
     * @param pageable pagination information
     * @return page of audit logs
     */
    @Override
    @NonNull
    Page<AuditLog> findAll(@NonNull Pageable pageable);

    /**
     * Find audit logs by object type with pagination.
     *
     * @param objectType the type of object
     * @param pageable   pagination information
     * @return page of audit logs
     */
    Page<AuditLog> findByObjectType(String objectType, Pageable pageable);

    /**
     * Find audit logs by action type with pagination.
     *
     * @param actionType the type of action
     * @param pageable   pagination information
     * @return page of audit logs
     */
    Page<AuditLog> findByActionType(String actionType, Pageable pageable);

    /**
     * Search for audit logs by object type with pagination
     *
     * @param objectType the object type
     * @param limit      max number of results
     * @return list of matching audit logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.objectType = :objectType ORDER BY a.updateTime DESC LIMIT :limit")
    List<AuditLog> findRecentByObjectType(@Param("objectType") String objectType, @Param("limit") int limit);
}
