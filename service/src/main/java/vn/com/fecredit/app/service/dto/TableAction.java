package vn.com.fecredit.app.service.dto;

/**
 * Enum representing possible actions that can be performed on tables.
 */
public enum TableAction {
    /**
     * Add a new record
     */
    ADD,
    
    /**
     * Update an existing record
     */
    UPDATE,
    
    /**
     * Delete a record
     */
    DELETE,
    
    /**
     * View record details
     */
    VIEW,
    
    /**
     * Export table data
     */
    EXPORT,
    
    /**
     * Import data into table
     */
    IMPORT
}
