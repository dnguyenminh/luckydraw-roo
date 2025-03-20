package vn.com.fecredit.app.entity.util;

import java.util.UUID;

/**
 * Utility class for entity tests
 */
public class TestUtil {

    /**
     * Generate a unique code for testing
     * @return unique code string
     */
    public static String generateUniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Generate metadata map for testing
     * @param prefix prefix for metadata values
     * @return metadata map
     */
    public static String generateMetadata(String prefix) {
        return "{\"testKey\":\"" + prefix + "_" + generateUniqueCode() + "\"}";
    }

    private TestUtil() {
        // Private constructor to prevent instantiation
    }
}