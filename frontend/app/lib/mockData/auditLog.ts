import { 
  TableFetchResponse, 
  Column, 
  FieldType, 
  SortType, 
  ObjectType
} from './interfaces';

// Define columns for the audit log table
const auditLogColumns: Column[] = [
  { fieldName: 'id', fieldType: FieldType.NUMBER, sortType: SortType.ASCENDING, displayName: 'ID', filterable: true },
  { fieldName: 'timestamp', fieldType: FieldType.DATETIME, sortType: SortType.DESCENDING, displayName: 'Time', filterable: true },
  { fieldName: 'username', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'User', filterable: true },
  { fieldName: 'action', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Action', filterable: true },
  { fieldName: 'objectType', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Object Type', filterable: true },
  { fieldName: 'objectId', fieldType: FieldType.NUMBER, sortType: SortType.NONE, displayName: 'Object ID', filterable: true },
  { fieldName: 'details', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Details', filterable: true },
  { fieldName: 'ipAddress', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'IP Address', filterable: true }
];

// Create mock audit log data
export const mockAuditLogTable: TableFetchResponse = {
  totalPages: 3,
  currentPage: 0,
  pageSize: 10,
  totalElements: 25,
  tableName: 'audit_log',
  columns: auditLogColumns,
  rows: [
    {
      data: {
        id: 1,
        timestamp: '2023-06-15T10:25:00Z',
        username: 'admin',
        action: 'CREATE',
        objectType: 'EVENT',
        objectId: 1,
        details: 'Created event: Summer Giveaway',
        ipAddress: '192.168.1.100'
      }
    },
    {
      data: {
        id: 2,
        timestamp: '2023-06-15T10:30:15Z',
        username: 'admin',
        action: 'CREATE',
        objectType: 'REWARD',
        objectId: 1,
        details: 'Created reward: Gold Medal for event: Summer Giveaway',
        ipAddress: '192.168.1.100'
      }
    },
    {
      data: {
        id: 3,
        timestamp: '2023-06-15T10:35:30Z',
        username: 'admin',
        action: 'CREATE',
        objectType: 'REWARD',
        objectId: 2,
        details: 'Created reward: Silver Medal for event: Summer Giveaway',
        ipAddress: '192.168.1.100'
      }
    },
    {
      data: {
        id: 4,
        timestamp: '2023-06-15T11:15:45Z',
        username: 'manager',
        action: 'UPDATE',
        objectType: 'EVENT',
        objectId: 1,
        details: 'Updated event details for: Summer Giveaway',
        ipAddress: '192.168.1.101'
      }
    },
    {
      data: {
        id: 5,
        timestamp: '2023-06-16T09:20:10Z',
        username: 'admin',
        action: 'CREATE',
        objectType: 'GOLDEN_HOUR',
        objectId: 1,
        details: 'Created golden hour: Morning Rush for event: Summer Giveaway',
        ipAddress: '192.168.1.100'
      }
    },
    {
      data: {
        id: 6,
        timestamp: '2023-06-16T09:45:22Z',
        username: 'admin',
        action: 'CREATE',
        objectType: 'GOLDEN_HOUR',
        objectId: 2,
        details: 'Created golden hour: Lunch Break for event: Summer Giveaway',
        ipAddress: '192.168.1.100'
      }
    },
    {
      data: {
        id: 7,
        timestamp: '2023-06-20T14:30:50Z',
        username: 'system',
        action: 'SYSTEM',
        objectType: 'SPIN_HISTORY',
        objectId: 1,
        details: 'Participant: John Doe won reward: Gold Medal in event: Summer Giveaway',
        ipAddress: '192.168.1.200'
      }
    },
    {
      data: {
        id: 8,
        timestamp: '2023-06-20T14:45:15Z',
        username: 'system',
        action: 'SYSTEM',
        objectType: 'SPIN_HISTORY',
        objectId: 2,
        details: 'Participant: John Doe did not win in event: Summer Giveaway',
        ipAddress: '192.168.1.200'
      }
    },
    {
      data: {
        id: 9,
        timestamp: '2023-09-10T10:10:00Z',
        username: 'admin',
        action: 'CREATE',
        objectType: 'EVENT',
        objectId: 2,
        details: 'Created event: Winter Wonderland',
        ipAddress: '192.168.1.100'
      }
    },
    {
      data: {
        id: 10,
        timestamp: '2023-09-10T10:25:30Z',
        username: 'admin',
        action: 'CREATE',
        objectType: 'REWARD',
        objectId: 3,
        details: 'Created reward: Bronze Medal for event: Winter Wonderland',
        ipAddress: '192.168.1.100'
      }
    }
    // Additional audit log entries would be included here
  ],
  relatedLinkedObjects: {},
  first: true,
  last: false,
  empty: false,
  numberOfElements: 10,
  originalRequest: {
    page: 0,
    size: 10,
    sorts: [],
    filters: [],
    search: {},
    objectType: ObjectType.AUDIT_LOG
  },
  statistics: {
    totalActions: 25,
    createActions: 12,
    updateActions: 6,
    deleteActions: 2,
    systemActions: 5
  }
};
