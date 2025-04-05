package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for specifying sorting parameters in data fetch requests.
 * Defines which field to sort by and in what direction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortRequest {
    
    /**
     * Field/property name to sort by
     */
    private String field;
    
    /**
     * Direction of the sort (ASC or DESC)
     */
    private SortType sortType;
    
    /**
     * Default constructor with field name and sort direction
     * @param field field to sort by
     * @param direction sort direction as string ("asc"/"desc")
     */
    public SortRequest(String field, String direction) {
        this.field = field;
        this.sortType = SortType.fromString(direction);
    }
}
