package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for user activity statistics.
 * Contains user information and activity counts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityDTO {
    
    /**
     * Username of the user
     */
    private String username;
    
    /**
     * Total number of actions performed by this user
     */
    private Long totalActions;
    
    /**
     * Optional breakdown of actions by type
     */
    @Builder.Default
    private Map<String, Long> actionBreakdown = new HashMap<>();
    
    /**
     * Last activity timestamp
     */
    private String lastActive;
}
