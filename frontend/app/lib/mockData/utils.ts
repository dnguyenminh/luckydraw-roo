// Fix circular import by directly importing from specific files rather than index
import { 
  TableFetchResponse, 
  TableInfo, 
  RelatedLinkedObject, 
  ObjectType 
} from './interfaces';
import { mockParticipantTable } from './participants';
import { mockEventTable } from './events';
import { mockRegionTable } from './regions';
import { mockProvinceTable } from './provinces';
import { mockRewardTable } from './rewards';
import { mockGoldenHourTable } from './goldenHours';
import { mockAuditLogTable } from './auditLog';
import { mockSpinHistoryTable } from './spinHistory';
import { mockUserTable } from './users';
import { mockRoleTable } from './roles';

// Initialize all relationships after all modules are loaded
export function initializeAllRelationships() {
  // This function can be called after all mock data modules are loaded
  // to establish relationships between entities and avoid circular dependencies
  
  // Here we would call all the individual initialization functions
  console.log('Initializing all entity relationships');
  
  // Participant-spin history relationships
  // Events-regions relationships
  // etc.
}

// Helper function to get related table data - UPDATED to use relatedLinkedObjects only
export function getRelatedTableData(
  sourceTable: TableFetchResponse, 
  sourceId: number, 
  relatedTableName: string
): TableFetchResponse | null {
  // Only look for data in relatedLinkedObjects
  if (sourceTable.relatedLinkedObjects?.[relatedTableName]?.[sourceId]) {
    const relatedObjects = sourceTable.relatedLinkedObjects[relatedTableName][sourceId];
    return {
      totalPages: 1,
      currentPage: 0,
      pageSize: relatedObjects.length,
      totalElements: relatedObjects.length,
      tableName: `${sourceTable.tableName}_${relatedTableName}`,
      rows: relatedObjects.map(obj => ({ data: { ...obj } })),
      originalRequest: {
        page: 0,
        size: relatedObjects.length,
        sorts: [],
        filters: [],
        search: {},
        objectType: relatedObjects[0]?.objectType
      },
      statistics: {},
      first: true,
      last: true,
      empty: relatedObjects.length === 0,
      numberOfElements: relatedObjects.length
    };
  }

  return null;
}

// Helper function to get related entity data
export function getRelatedEntityData(
  entityType: string,
  entityId: number | string,
  relationName: string
): RelatedLinkedObject[] {
  const typeToTable: Record<string, any> = {
    'event': mockEventTable,
    'participant': mockParticipantTable,
    'region': mockRegionTable,
    'province': mockProvinceTable,
    'reward': mockRewardTable,
    'goldenHour': mockGoldenHourTable,
    'spinHistory': mockSpinHistoryTable,
    'user': mockUserTable,
    'role': mockRoleTable
  };
  
  const table = typeToTable[entityType];
  
  if (!table || !table.relatedLinkedObjects || !table.relatedLinkedObjects[relationName]) {
    return [];
  }
  
  return table.relatedLinkedObjects[relationName][entityId] || [];
}

// Ensure consistency in status formatting
export function formatStatus(status: string): string {
  const statusLower = status.toLowerCase();
  
  if (statusLower.includes('active')) return 'Active';
  if (statusLower.includes('inactive')) return 'Inactive';
  if (statusLower.includes('complete') || statusLower.includes('finished')) return 'Completed';
  if (statusLower.includes('upcoming') || statusLower.includes('schedule')) return 'Scheduled';
  if (statusLower.includes('pend')) return 'Pending';
  if (statusLower.includes('cancel') || statusLower.includes('deleted')) return 'Cancelled';
  
  return status; // Return original if no match
}

// Helper function to create a consistenty structured entity reference
export function createEntityRef<T extends { id: number; name: string }>(entity: T) {
  return {
    id: entity.id,
    name: entity.name
  };
}

// Convert string entity type to ObjectType enum
export function stringToObjectType(entityType: string): ObjectType {
  // Normalize the string and convert to uppercase
  const normalizedType = entityType.trim().toUpperCase();
  
  // Check if it's a valid ObjectType key
  if (Object.keys(ObjectType).includes(normalizedType)) {
    return ObjectType[normalizedType as keyof typeof ObjectType];
  }
  
  // Handle common variations
  const mappings: Record<string, ObjectType> = {
    'EVENTS': ObjectType.EVENT,
    'REGIONS': ObjectType.REGION,
    'PROVINCES': ObjectType.PROVINCE,
    'REWARDS': ObjectType.REWARD,
    'GOLDENHOURS': ObjectType.GOLDEN_HOUR,
    'GOLDEN_HOURS': ObjectType.GOLDEN_HOUR,
    'GOLDEN-HOURS': ObjectType.GOLDEN_HOUR,
    'PARTICIPANTS': ObjectType.PARTICIPANT,
    'SPINHISTORIES': ObjectType.SPIN_HISTORY,
    'SPIN_HISTORIES': ObjectType.SPIN_HISTORY,
    'SPIN-HISTORIES': ObjectType.SPIN_HISTORY,
    'AUDITLOGS': ObjectType.AUDIT_LOG,
    'AUDIT_LOGS': ObjectType.AUDIT_LOG,
    'AUDIT-LOGS': ObjectType.AUDIT_LOG,
    'STATISTICS': ObjectType.STATIS,
    'STATISTIC': ObjectType.STATIS,
    'USERS': ObjectType.USER,
    'ROLES': ObjectType.ROLE
  };
  
  if (mappings[normalizedType]) {
    return mappings[normalizedType];
  }
  
  // Default to EVENT if not found
  console.warn(`Unknown entity type: ${entityType}, defaulting to EVENT`);
  return ObjectType.EVENT;
}

// Helper function to create bidirectional relationships between entities - UPDATED
export function linkEntities(
  sourceTable: TableFetchResponse,
  sourceId: number,
  targetTable: TableFetchResponse,
  targetId: number,
  sourceRelationName: string,
  targetRelationName: string,
  sourceObjectType: ObjectType,
  targetObjectType: ObjectType
) {
  // Initialize related linked objects if they don't exist
  if (!sourceTable.relatedLinkedObjects) sourceTable.relatedLinkedObjects = {};
  if (!targetTable.relatedLinkedObjects) targetTable.relatedLinkedObjects = {};
  
  if (!sourceTable.relatedLinkedObjects[sourceRelationName]) 
    sourceTable.relatedLinkedObjects[sourceRelationName] = {};
  
  if (!targetTable.relatedLinkedObjects[targetRelationName]) 
    targetTable.relatedLinkedObjects[targetRelationName] = {};
  
  // Create entry in source table's related objects pointing to target
  const targetData = targetTable.rows.find(row => row.data.id === targetId);
  if (targetData) {
    if (!sourceTable.relatedLinkedObjects[sourceRelationName][sourceId]) {
      sourceTable.relatedLinkedObjects[sourceRelationName][sourceId] = [];
    }
    
    // Create properly typed linked object
    const targetLinkedObject: RelatedLinkedObject = {
      objectType: targetObjectType,
      id: targetId,
      name: targetData.data.name || `${String(targetObjectType)}_${targetId}`
    };
    
    // Copy additional data properties, properly typed
    Object.entries(targetData.data).forEach(([key, value]) => {
      if (key !== 'objectType' && key !== 'id' && key !== 'name') {
        (targetLinkedObject as Record<string, any>)[key] = value;
      }
    });
    
    // Avoid duplicates
    const exists = sourceTable.relatedLinkedObjects[sourceRelationName][sourceId]
      .some(obj => obj.id === targetId && obj.objectType === targetObjectType);
      
    if (!exists) {
      sourceTable.relatedLinkedObjects[sourceRelationName][sourceId].push(targetLinkedObject);
    }
  }
  
  // Create entry in target table's related objects pointing to source
  const sourceData = sourceTable.rows.find(row => row.data.id === sourceId);
  if (sourceData) {
    if (!targetTable.relatedLinkedObjects[targetRelationName][targetId]) {
      targetTable.relatedLinkedObjects[targetRelationName][targetId] = [];
    }
    
    // Create properly typed linked object
    const sourceLinkedObject: RelatedLinkedObject = {
      objectType: sourceObjectType,
      id: sourceId,
      name: sourceData.data.name || `${String(sourceObjectType)}_${sourceId}`
    };
    
    // Copy additional data properties, properly typed
    Object.entries(sourceData.data).forEach(([key, value]) => {
      if (key !== 'objectType' && key !== 'id' && key !== 'name') {
        (sourceLinkedObject as Record<string, any>)[key] = value;
      }
    });
    
    // Avoid duplicates
    const exists = targetTable.relatedLinkedObjects[targetRelationName][targetId]
      .some(obj => obj.id === sourceId && obj.objectType === sourceObjectType);
      
    if (!exists) {
      targetTable.relatedLinkedObjects[targetRelationName][targetId].push(sourceLinkedObject);
    }
  }
  
  // NO RELATEDTABLES CODE HERE - We've completely removed this
}

// Export entity maps for easy access
export const entityMaps = {
  events: mockEventTable,
  regions: mockRegionTable,
  provinces: mockProvinceTable,
  participants: mockParticipantTable,
  rewards: mockRewardTable,
  goldenHours: mockGoldenHourTable,
  spinHistories: mockSpinHistoryTable,
  auditLogs: mockAuditLogTable,
  users: mockUserTable,
  roles: mockRoleTable
};

// Mapping from type strings to ObjectType enum - FIX TYPE ERRORS
export const entityTypeMap: Record<string, ObjectType> = {
  'event': ObjectType.EVENT,
  'region': ObjectType.REGION,
  'province': ObjectType.PROVINCE,
  'reward': ObjectType.REWARD,
  'goldenHour': ObjectType.GOLDEN_HOUR,
  'participant': ObjectType.PARTICIPANT,
  'spinHistory': ObjectType.SPIN_HISTORY,
  'auditLog': ObjectType.AUDIT_LOG,
  'statistics': ObjectType.STATIS,
  'user': ObjectType.USER,
  'role': ObjectType.ROLE
};

// Add a utility function to initialize relationships that works with RelatedLinkedObjects
export function initializeEntityRelationship(
  sourceTable: TableFetchResponse,
  sourceId: number,
  relationName: string,
  relationObjects: RelatedLinkedObject[]
): void {
  if (!sourceTable.relatedLinkedObjects) {
    sourceTable.relatedLinkedObjects = {};
  }
  
  if (!sourceTable.relatedLinkedObjects[relationName]) {
    sourceTable.relatedLinkedObjects[relationName] = {};
  }
  
  // Set the relationship with proper validation
  const validatedObjects = relationObjects.map(obj => {
    const validatedObj: RelatedLinkedObject = {
      objectType: obj.objectType || ObjectType.EVENT, // Default to EVENT if missing
      id: obj.id,
      name: obj.name || `Object_${obj.id}`
    };
    
    // Copy additional properties
    Object.keys(obj).forEach(key => {
      if (key !== 'objectType' && key !== 'id' && key !== 'name') {
        (validatedObj as Record<string, any>)[key] = (obj as Record<string, any>)[key];
      }
    });
    
    return validatedObj;
  });
  
  sourceTable.relatedLinkedObjects[relationName][sourceId] = validatedObjects;
}


// Helper function to determine ObjectType from relation name
function determineObjectTypeFromRelation(relationName: string): ObjectType {
  // Common mapping patterns
  const relationToType: Record<string, ObjectType> = {
    'events': ObjectType.EVENT,
    'regions': ObjectType.REGION,
    'provinces': ObjectType.PROVINCE,
    'rewards': ObjectType.REWARD,
    'goldenHours': ObjectType.GOLDEN_HOUR,
    'golden_hours': ObjectType.GOLDEN_HOUR,
    'participants': ObjectType.PARTICIPANT,
    'spinHistories': ObjectType.SPIN_HISTORY,
    'spin_history': ObjectType.SPIN_HISTORY,
    'auditLogs': ObjectType.AUDIT_LOG,
    'audit_log': ObjectType.AUDIT_LOG,
    'users': ObjectType.USER,
    'roles': ObjectType.ROLE,
  };
  
  return relationToType[relationName] || ObjectType.EVENT;
}

// Batch process to migrate all mock data tables
export function migrateAllMockDataToLinkedObjects(): void {
  const allTables = [
    mockEventTable, 
    mockRegionTable, 
    mockProvinceTable, 
    mockParticipantTable,
    mockRewardTable,
    mockGoldenHourTable, 
    mockSpinHistoryTable,
    mockUserTable,
    mockRoleTable
  ];
  
}

// Check that all tables have proper relatedLinkedObjects
export function validateEntityRelationships(): void {
}

// Add a function to convert from TableFetchResponse to RelatedLinkedObject array
export function convertTableToRelatedLinkedObjects(
  sourceTable: TableFetchResponse,
  objectType: ObjectType
): RelatedLinkedObject[] {
  if (!sourceTable.rows || sourceTable.rows.length === 0) {
    return [];
  }
  
  return sourceTable.rows.map(row => {
    // Create properly typed linked object
    const linkedObj: RelatedLinkedObject = {
      objectType: objectType,
      id: row.data.id,
      name: row.data.name || `${String(objectType)}_${row.data.id}`
    };
    
    // Copy additional data properties, properly typed
    Object.entries(row.data).forEach(([key, value]) => {
      if (key !== 'objectType' && key !== 'id' && key !== 'name') {
        (linkedObj as Record<string, any>)[key] = value;
      }
    });
    
    return linkedObj;
  });
}

// Add a utility for bulk object relationship creation  
export function createRelatedObjectsFromTable(
  sourceTable: TableFetchResponse,
  objectType: ObjectType
): RelatedLinkedObject[] {
  return sourceTable.rows.map(row => ({
    objectType: objectType,
    id: row.data.id,
    name: row.data.name || `Unknown ${String(objectType)}`,
    // Don't spread all data to avoid type conflicts
    // Just take standard metadata fields
    ...(row.data.status ? { status: row.data.status } : {}),
    ...(row.data.description ? { description: row.data.description } : {})
  }));
}

// Helper function to use with migrated data
export function ensureRelatedLinkedObjects(table: TableFetchResponse): void {
  // Add a new helper function to initialize empty relatedLinkedObjects if needed
  if (!table.relatedLinkedObjects) {
    table.relatedLinkedObjects = {};
  }
  
}
