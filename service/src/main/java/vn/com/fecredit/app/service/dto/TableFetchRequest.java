package vn.com.fecredit.app.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Represents a request to fetch table data.
 * Contains all parameters needed to search, filter, sort and paginate data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableFetchRequest implements Serializable {
    /**
     * Serial Version UID for serialization
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The type of object to fetch
     */
    private ObjectType objectType;
    
    /**
     * The name of the entity to fetch (must match an existing entity name)
     * Should be one of: User, Role, Permission, Event, Region, Province, etc.
     * as defined in the ObjectType enum
     */
    @Pattern(regexp = "^(User|Role|Permission|Event|EventLocation|GoldenHour|Region|Province"
            + "|Reward|Participant|ParticipantEvent|SpinHistory|AuditLog|BlacklistedToken|Configuration)$",
            message = "Entity name must be a valid entity type")
    private String entityName;
    
    /**
     * The page number to fetch (0-based)
     */
    @Min(value = 0, message = "Page number must be non-negative")
    private int page;
    
    /**
     * The number of items per page
     */
    @Min(value = 1, message = "Page size must be positive")
    private int size;
    
    /**
     * The sort criteria to apply
     */
    private List<SortRequest> sorts;
    
    /**
     * The filter criteria to apply
     */
    private List<FilterRequest> filters;
    
    /**
     * Search criteria for different object types
     */
    private Map<ObjectType, DataObjectKeyValues> search;
    
    /**
     * Validates that either objectType or entityName is provided (but not necessarily both)
     * 
     * @return true if the request is valid
     */
    public boolean isValid() {
        return objectType != null || (entityName != null && !entityName.isEmpty());
    }
}
