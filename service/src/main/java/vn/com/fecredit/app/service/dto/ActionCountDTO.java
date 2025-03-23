package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.fecredit.app.entity.enums.ActionType;

/**
 * DTO representing a count of actions by action type.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionCountDTO {
    private ActionType actionType;
    private Long count;
}
