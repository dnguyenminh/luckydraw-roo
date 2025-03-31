import { TableFetchResponse, FieldType, SortType } from './interfaces';

// Mock audit log table data
export const mockAuditLogTable: TableFetchResponse = {
  totalPages: 10,
  currentPage: 0,
  pageSize: 20,
  totalElements: 185,
  tableName: "audit_log",
  columns: [
    { 
      fieldName: "id", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "ID",
      filterable: false
    },
    { 
      fieldName: "timestamp", 
      fieldType: FieldType.DATETIME, 
      sortType: SortType.DESCENDING,
      displayName: "Timestamp",
      filterable: true
    },
    { 
      fieldName: "user", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "User",
      filterable: true
    },
    { 
      fieldName: "action", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Action",
      filterable: true
    },
    { 
      fieldName: "details", 
      fieldType: FieldType.STRING, 
      sortType: SortType.NONE,
      displayName: "Details",
      filterable: true
    },
    { 
      fieldName: "entityType", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Entity Type",
      filterable: true
    },
    { 
      fieldName: "entityId", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "Entity ID",
      filterable: true
    }
  ],
  rows: [
    {
      data: { 
        id: 1, 
        timestamp: '2023-09-15 14:32:45', 
        user: 'admin@example.com', 
        action: 'Update Event', 
        details: 'Updated event "Summer Giveaway" status to Active',
        entityType: 'Event',
        entityId: 1
      }
    },
    {
      data: { 
        id: 2, 
        timestamp: '2023-09-15 13:25:10', 
        user: 'admin@example.com', 
        action: 'Add Reward', 
        details: 'Added new reward "Gift Card" to Summer Giveaway event',
        entityType: 'Reward',
        entityId: 1
      }
    },
    {
      data: { 
        id: 3, 
        timestamp: '2023-09-15 11:17:33', 
        user: 'manager@example.com', 
        action: 'Delete Participant', 
        details: 'Deleted participant "Test User" from Summer Giveaway event',
        entityType: 'Participant',
        entityId: 10
      }
    },
    {
      data: { 
        id: 4, 
        timestamp: '2023-09-14 16:45:22', 
        user: 'admin@example.com', 
        action: 'Schedule Golden Hour', 
        details: 'Scheduled new golden hour "Evening Rush" for Summer Giveaway event',
        entityType: 'GoldenHour',
        entityId: 1
      }
    },
    {
      data: { 
        id: 5, 
        timestamp: '2023-09-14 10:08:55', 
        user: 'manager@example.com', 
        action: 'Update Reward', 
        details: 'Updated reward "Gift Card" quantity to 100',
        entityType: 'Reward',
        entityId: 1
      }
    }
  ],
  originalRequest: {
    page: 0,
    size: 20,
    sorts: [{ field: "timestamp", order: "desc" }],
    filters: [],
    search: {}
  },
  statistics: {},
  first: true,
  last: false,
  empty: false,
  numberOfElements: 20
};
