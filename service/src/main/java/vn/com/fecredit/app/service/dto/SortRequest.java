package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents a request to sort data.
 * Contains information about which field to sort by and in which direction.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SortRequest implements Serializable {
    /**
     * Serial Version UID for serialization
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The field to sort on
     */
    private String field;
    
    /**
     * The direction of the sort
     */
    private SortType sortType;
}
