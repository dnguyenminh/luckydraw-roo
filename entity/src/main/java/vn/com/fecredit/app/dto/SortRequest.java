package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for sort specifications, as defined in
 * CommonAPIRequestAndResponse.puml
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortRequest {
    /**
     * Field name to sort by
     */
    private String field;

    /**
     * Sort type: ASCENDING, DESCENDING, or NONE
     */
    private SortType type;

    /**
     * Set sort type to ASCENDING
     */
    public void setAscending() {
        this.type = SortType.ASCENDING;
    }

    /**
     * Set sort type to DESCENDING
     */
    public void setDescending() {
        this.type = SortType.DESCENDING;
    }

    /**
     * Set sort type to NONE
     */
    public void setNoSort() {
        this.type = SortType.NONE;
    }
}
