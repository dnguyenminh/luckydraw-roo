package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a key for a data object.
 * Contains a list of key field names that uniquely identify an object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataObjectKey implements Serializable {
    /**
     * Serial Version UID for serialization
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * List of field names that constitute the key for the object
     */
    private List<String> keys;
}
