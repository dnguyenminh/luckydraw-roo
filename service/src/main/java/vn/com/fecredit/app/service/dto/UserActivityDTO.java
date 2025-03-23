package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing user activity statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityDTO {
    private String username;
    private Long actionCount;
}
