package vn.com.fecredit.app.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Enum defining types of authentication tokens used in the system.
 * Supports the JWT authentication system with different token purposes.
 */
public enum TokenType {
    /**
     * Short-lived token used for authentication during API calls
     */
    ACCESS,
    
    /**
     * Longer-lived token used to obtain new access tokens
     */
    REFRESH;

    /**
     * Check if token is an access token
     * @return true if this is an access token
     */
    public boolean isAccess() {
        return this == ACCESS;
    }

    /**
     * Check if token is a refresh token
     * @return true if this is a refresh token
     */
    public boolean isRefresh() {
        return this == REFRESH;
    }

    /**
     * JPA converter for TokenType enum to handle database conversion
     */
    @Converter(autoApply = true)
    public static class TokenTypeConverter implements AttributeConverter<TokenType, String> {

        /**
         * Default constructor.
         * Creates a new TokenTypeConverter for converting between enum values and database strings.
         */
        public TokenTypeConverter() {
            // Default constructor
        }

        /**
         * JPA AttributeConverter implementation for TokenType enum.
         * <p>
         * Handles conversion between TokenType enum values and their string
         * representation for database storage and retrieval.
         */

        /**
         * Convert TokenType enum to database column value
         * @param tokenType the enum to convert
         * @return string representation for database
         */
        @Override
        public String convertToDatabaseColumn(TokenType tokenType) {
            return tokenType != null ? tokenType.name() : null;
        }

        /**
         * Convert database value to TokenType enum
         * @param dbData the string from database
         * @return TokenType enum value
         * @throws IllegalArgumentException if value is invalid
         */
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