// Fix circular import by directly importing from specific files rather than index
import { TableFetchResponse, TableInfo } from './interfaces';
import { mockParticipantTable } from './participants';
import { mockRegionTable } from './regions';
import { mockProvinceTable } from './provinces';
import { mockRewardTable } from './rewards';
import { mockGoldenHourTable } from './goldenHours';
import { mockAuditLogTable } from './auditLog';
import { mockSpinHistoryTable } from './spinHistory';

// Helper function to get related table data
export function getRelatedTableData(
  sourceTable: TableFetchResponse, 
  sourceId: number, 
  relatedTableName: string
): TableFetchResponse | null {
  // Look for related tables at the TableInfo level
  if (!sourceTable.relatedTables?.[relatedTableName]) {
    return null;
  }

  // Find the specific related table for this source ID
  const sourceIdKey = String(sourceId);
  const relatedTable = sourceTable.relatedTables[relatedTableName][sourceIdKey];
  if (!relatedTable) {
    return null;
  }

  // Return a proper TableFetchResponse based on the TableInfo data
  const responseFetchTable: TableFetchResponse = {
    // Copy all TableInfo properties
    ...relatedTable,
    // Add TableFetchResponse specific properties
    originalRequest: {
      page: relatedTable.currentPage || 0,
      size: relatedTable.pageSize || 10,
      sorts: [{ field: "id", order: "asc" }],
      filters: [],
      search: {}
    },
    statistics: {}
  };

  return responseFetchTable;
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

// Helper function to create bidirectional relationships between entities
export function linkEntities(
  sourceTable: TableFetchResponse,
  sourceId: number,
  targetTable: TableFetchResponse,
  targetId: number,
  sourceRelationName: string,
  targetRelationName: string
) {
  // Initialize related tables if they don't exist
  if (!sourceTable.relatedTables) sourceTable.relatedTables = {};
  if (!targetTable.relatedTables) targetTable.relatedTables = {};
  
  if (!sourceTable.relatedTables[sourceRelationName]) 
    sourceTable.relatedTables[sourceRelationName] = {};
  
  if (!targetTable.relatedTables[targetRelationName]) 
    targetTable.relatedTables[targetRelationName] = {};
  
  // Create entry in source table's related tables pointing to target
  if (!sourceTable.relatedTables[sourceRelationName][sourceId]) {
    const targetData = targetTable.rows.find(row => row.data.id === targetId);
    if (targetData) {
      sourceTable.relatedTables[sourceRelationName][sourceId] = {
        totalPages: 1,
        currentPage: 0,
        pageSize: 10,
        totalElements: 1,
        tableName: sourceRelationName,
        rows: [targetData]
      };
    }
  }
  
  // Create entry in target table's related tables pointing to source
  if (!targetTable.relatedTables[targetRelationName][targetId]) {
    const sourceData = sourceTable.rows.find(row => row.data.id === sourceId);
    if (sourceData) {
      targetTable.relatedTables[targetRelationName][targetId] = {
        totalPages: 1,
        currentPage: 0,
        pageSize: 10,
        totalElements: 1,
        tableName: targetRelationName,
        rows: [sourceData]
      };
    }
  }
}
