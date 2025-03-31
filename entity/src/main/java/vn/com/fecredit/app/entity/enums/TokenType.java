package vn.com.fecredit.app.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum TokenType {
    ACCESS,
    REFRESH;

    public boolean isAccess() {
        return this == ACCESS;
    }

    public boolean isRefresh() {
        return this == REFRESH;
    }

    @Converter(autoApply = true)
    public static class TokenTypeConverter implements AttributeConverter<TokenType, String> {

        @Override
        public String convertToDatabaseColumn(TokenType tokenType) {
            return tokenType != null ? tokenType.name() : null;
        }

        @Override
        public TokenType convertToEntityAttribute(String dbData) {
            if (dbData == null) {
                return null;
            }
            try {
                return TokenType.valueOf(dbData);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown token type: " + dbData);
            }
        }
    }
}