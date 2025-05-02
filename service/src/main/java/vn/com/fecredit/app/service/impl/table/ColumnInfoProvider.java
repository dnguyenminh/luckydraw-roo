package vn.com.fecredit.app.service.impl.table;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.com.fecredit.app.service.dto.ColumnInfo;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.factory.RepositoryFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Provider for column information for different object types
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ColumnInfoProvider {

    private final RepositoryFactory repositoryFactory;
    private final EntityFinder entityFinder;

    /**
     * Get column information for an object type
     */
    public Map<String, ColumnInfo> getColumnInfo(ObjectType objectType, TableFetchRequest request) {
        Map<String, ColumnInfo> columnInfo = new HashMap<>();
        
        try {
            // Get entity class for object type
            Class<?> entityClass = repositoryFactory.getEntityClass(objectType);
            if (entityClass == null) {
                log.error("Could not find entity class for object type: {}", objectType);
                return columnInfo;
            }
            
            // Get column metadata from view columns if provided
            if (request.getViewColumns() != null && !request.getViewColumns().isEmpty()) {
                request.getViewColumns().forEach(column -> 
                    columnInfo.put(column.getFieldName(), column));
            } else {
                // Otherwise, get default columns from entity
                Map<String, ColumnInfo> defaultColumns = entityFinder.getDefaultColumns(entityClass);
                columnInfo.putAll(defaultColumns);
            }
            
        } catch (Exception e) {
            log.error("Error getting column info for {}: {}", objectType, e.getMessage());
        }
        
        return columnInfo;
    }
}
