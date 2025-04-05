package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import vn.com.fecredit.app.entity.enums.ActionType;

/**
 * DTO for action count statistics.
 * Contains the action type and its count.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionCountDTO {
    
    /**
     * Type of action
     */
    private ActionType actionType;
    
    /**
     * Count of occurrences of this action type
     */
    private Long count;
    
    /**
     * Optional description or additional information
     */
    private String description;
}
