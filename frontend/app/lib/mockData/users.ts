import { TableFetchResponse, FieldType, SortType } from './interfaces';

// Mock user table data
export const mockUserTable: TableFetchResponse = {
  totalPages: 2,
  currentPage: 0,
  pageSize: 10,
  totalElements: 15,
  tableName: "users",
  columns: [
    { 
      fieldName: "id", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "ID",
      filterable: false
    },
    { 
      fieldName: "name", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Name",
      filterable: true
    },
    { 
      fieldName: "email", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Email",
      filterable: true
    },
    { 
      fieldName: "role", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Role",
      filterable: true
    },
    { 
      fieldName: "lastLogin", 
      fieldType: FieldType.DATETIME, 
      sortType: SortType.DESCENDING,
      displayName: "Last Login",
      filterable: false
    },
    { 
      fieldName: "status", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Status",
      filterable: true
    }
  ],
  rows: [
    {
      data: { id: 1, name: 'John Admin', email: 'john.admin@example.com', role: 'Admin', roleId: 1, lastLogin: '2023-09-15 14:32:45', status: 'Active' }
    },
    {
      data: { id: 2, name: 'Jane Manager', email: 'jane.manager@example.com', role: 'Manager', roleId: 2, lastLogin: '2023-09-14 10:15:20', status: 'Active' }
    },
    {
      data: { id: 3, name: 'Bob Operator', email: 'bob.operator@example.com', role: 'Operator', roleId: 3, lastLogin: '2023-09-13 09:45:12', status: 'Active' }
    },
    {
      data: { id: 4, name: 'Alice Viewer', email: 'alice.viewer@example.com', role: 'Viewer', roleId: 4, lastLogin: '2023-09-10 16:22:34', status: 'Inactive' }
    },
    {
      data: { id: 5, name: 'Charlie Custom', email: 'charlie.custom@example.com', role: 'Custom Role', roleId: 5, lastLogin: '2023-09-08 11:05:57', status: 'Active' }
    }
  ],
  originalRequest: {
    page: 0,
    size: 10,
    sorts: [{ field: "name", order: "asc" }],
    filters: [],
    search: {}
  },
  statistics: {},
  relatedTables: {}, // Initialize with empty object, to be populated by other modules
  first: true,
  last: false,
  empty: false,
  numberOfElements: 5
};

// Function to add relationships to user table
export function addUserRelationship(
  userId: number, 
  relationName: string, 
  relationData: any
) {
  if (!mockUserTable.relatedTables) {
    mockUserTable.relatedTables = {};
  }
  
  if (!mockUserTable.relatedTables[relationName]) {
    mockUserTable.relatedTables[relationName] = {};
  }
  
  mockUserTable.relatedTables[relationName][userId] = relationData;
}

// Mock user details
export const mockUserDetails: Record<number, any> = {
  1: {
    id: 1,
    name: 'John Admin',
    email: 'john.admin@example.com',
    role: 'Admin',
    lastLogin: '2023-09-15 14:32:45',
    status: 'Active',
    created: '2023-01-15',
    lastPasswordChange: '2023-08-01',
    permissions: [
      'view_events', 'create_events', 'edit_events', 'delete_events',
      'view_users', 'create_users', 'edit_users', 'delete_users',
      'view_reports', 'export_reports', 'schedule_reports'
    ]
  },
  2: {
    id: 2,
    name: 'Jane Manager',
    email: 'jane.manager@example.com',
    role: 'Manager',
    lastLogin: '2023-09-14 10:15:20',
    status: 'Active',
    created: '2023-02-10',
    lastPasswordChange: '2023-07-15',
    permissions: [
      'view_events', 'create_events', 'edit_events',
      'view_users', 'view_reports', 'export_reports'
    ]
  }
};

// Mock login history data for specific users
export const mockUserLoginHistory: Record<string, any[]> = {
  "1": [
    { id: 1, timestamp: '2023-09-15 14:32:45', ipAddress: '192.168.1.100', device: 'Chrome on Windows', success: true },
    { id: 2, timestamp: '2023-09-14 09:15:22', ipAddress: '192.168.1.100', device: 'Chrome on Windows', success: true },
    { id: 3, timestamp: '2023-09-13 16:44:10', ipAddress: '192.168.1.100', device: 'Chrome on Windows', success: true }
  ],
  "2": [
    { id: 4, timestamp: '2023-09-14 10:15:20', ipAddress: '192.168.1.101', device: 'Safari on macOS', success: true },
    { id: 5, timestamp: '2023-09-13 11:22:33', ipAddress: '192.168.1.101', device: 'Safari on macOS', success: true },
    { id: 6, timestamp: '2023-09-12 13:45:12', ipAddress: '10.0.0.50', device: 'Mobile Safari on iOS', success: true }
  ]
};
