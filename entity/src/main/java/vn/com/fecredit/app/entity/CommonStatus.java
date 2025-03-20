package vn.com.fecredit.app.entity;

import jakarta.persistence.Converter;
import jakarta.persistence.AttributeConverter;
import vn.com.fecredit.app.entity.base.EntityStatus;

/**
 * Base enum for common status values used throughout the application.
 * Can be extended by other status enums for specific entity types.
 */
public enum CommonStatus implements EntityStatus {
    /**
     * Entity is active and available for normal operations
     */
    ACTIVE(true, false),

    /**
     * Entity is temporarily inactive but can be reactivated
     */
    INACTIVE(false, false),

    /**
     * Entity is in draft state, not yet ready for normal operations
     */
    DRAFT(false, false),

    /**
     * Entity has been archived and is kept for historical purposes
     */
    ARCHIVED(false, false),

    /**
     * Entity has been marked as deleted (soft delete)
     */
    DELETED(false, true);

    private final boolean active;
    private final boolean deleted;

    CommonStatus(boolean active, boolean deleted) {
        this.active = active;
        this.deleted = deleted;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Checks if the status is inactive
     * @return true if status is INACTIVE
     */
    public boolean isInactive() {
        return this == INACTIVE;
    }

    /**
     * Checks if the status is draft
     * @return true if status is DRAFT
     */
    public boolean isDraft() {
        return this == DRAFT;
    }

    /**
     * Checks if the status is archived
     * @return true if status is ARCHIVED
     */
    public boolean isArchived() {
        return this == ARCHIVED;
    }

    /**
     * Checks if the entity can be modified (not deleted or archived)
     * @return true if the status allows modifications
     */
    public boolean canModify() {
        return this != DELETED && this != ARCHIVED;
    }

    /**
     * JPA converter for CommonStatus enum
     */
    @Converter(autoApply = true)
    public static class CommonStatusConverter implements AttributeConverter<CommonStatus, String> {
        
        @Override
        public String convertToDatabaseColumn(CommonStatus status) {
            return status != null ? status.name() : null;
        }

        @Override
        public CommonStatus convertToEntityAttribute(String dbData) {
            return dbData != null ? CommonStatus.valueOf(dbData) : null;
        }
    }
}
