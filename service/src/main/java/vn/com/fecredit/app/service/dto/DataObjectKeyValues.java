package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents key-value pairs for searching data objects.
 * Contains a map of search criteria that can be used to filter data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataObjectKeyValues implements Serializable {
    /**
     * Serial Version UID for serialization
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Map of field name to search value
     */
    private Map<String, Object> searchCriteria;
}
