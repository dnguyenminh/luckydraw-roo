package vn.com.fecredit.app.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum CommonStatus {
    ACTIVE("A", "Active"),
    INACTIVE("I", "Inactive"),
    DELETED("D", "Deleted");

    private final String code;
    private final String description;

    CommonStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isInactive() {
        return this == INACTIVE;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }

    public static CommonStatus fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (CommonStatus status : CommonStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid CommonStatus code: " + code);
    }

    @Converter(autoApply = true)
    public static class CommonStatusConverter implements AttributeConverter<CommonStatus, String> {
        @Override
        public String convertToDatabaseColumn(CommonStatus attribute) {
            return attribute != null ? attribute.getCode() : null;
        }

        @Override
        public CommonStatus convertToEntityAttribute(String dbData) {
            return dbData != null ? CommonStatus.fromCode(dbData) : null;
        }
    }
}