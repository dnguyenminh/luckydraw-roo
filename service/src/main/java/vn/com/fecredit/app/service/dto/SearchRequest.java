package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines a search operation to be applied when fetching data.
 * Specifies field and search value.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    
    /**
     * Field/column name to search in
     */
    private String field;
    
    /**
     * Value to search for
     */
    private String value;
}
