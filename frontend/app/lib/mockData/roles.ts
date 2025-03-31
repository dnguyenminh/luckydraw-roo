import { TableFetchResponse, FieldType, SortType } from './interfaces';
import { mockUserTable, addUserRelationship } from './users';

// Mock role table data
export const mockRoleTable: TableFetchResponse = {
  totalPages: 1,
  currentPage: 0,
  pageSize: 10,
  totalElements: 5,
  tableName: "roles",
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
      displayName: "Role Name",
      filterable: true
    },
    { 
      fieldName: "description", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Description",
      filterable: true
    },
    { 
      fieldName: "userCount", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "Users",
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
      data: { id: 1, name: 'Admin', description: 'System administrator with full access', userCount: 3, status: 'Active' }
    },
    {
      data: { id: 2, name: 'Manager', description: 'Event manager with management capabilities', userCount: 5, status: 'Active' }
    },
    {
      data: { id: 3, name: 'Operator', description: 'System operator with limited access', userCount: 12, status: 'Active' }
    },
    {
      data: { id: 4, name: 'Viewer', description: 'Read-only access to the system', userCount: 8, status: 'Active' }
    },
    {
      data: { id: 5, name: 'Custom Role', description: 'Role with custom permissions', userCount: 2, status: 'Inactive' }
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
  relatedTables: {},
  first: true,
  last: true,
  empty: false,
  numberOfElements: 5
};

// Function to add relationships to roles
export function addRoleRelationship(
  roleId: number, 
  relationName: string, 
  relationData: any
) {
  if (!mockRoleTable.relatedTables) {
    mockRoleTable.relatedTables = {};
  }
  
  if (!mockRoleTable.relatedTables[relationName]) {
    mockRoleTable.relatedTables[relationName] = {};
  }
  
  mockRoleTable.relatedTables[relationName][roleId] = relationData;
}

// Create role-user relationships
function initializeRoleUserRelationships() {
  // Map roles to users
  const roleUserMap: Record<string, any[]> = {
    "1": [
      { id: 1, name: 'John Admin', email: 'john.admin@example.com', role: 'Admin', lastLogin: '2023-09-15 14:32:45', status: 'Active' }
    ],
    "2": [
      { id: 2, name: 'Jane Manager', email: 'jane.manager@example.com', role: 'Manager', lastLogin: '2023-09-14 10:15:20', status: 'Active' }
    ],
    "3": [
      { id: 3, name: 'Bob Operator', email: 'bob.operator@example.com', role: 'Operator', lastLogin: '2023-09-13 09:45:12', status: 'Active' }
    ],
    "4": [
      { id: 4, name: 'Alice Viewer', email: 'alice.viewer@example.com', role: 'Viewer', lastLogin: '2023-09-10 16:22:34', status: 'Inactive' }
    ],
    "5": [
      { id: 5, name: 'Charlie Custom', email: 'charlie.custom@example.com', role: 'Custom Role', lastLogin: '2023-09-08 11:05:57', status: 'Active' }
    ]
  };

  // Add users to roles
  for (const [roleId, users] of Object.entries(roleUserMap)) {
    addRoleRelationship(Number(roleId), "users", {
      totalPages: 1,
      currentPage: 0, 
      pageSize: 10,
      totalElements: users.length,
      tableName: "role_users",
      rows: users.map(user => ({ data: user })),
      first: true,
      last: true,
      empty: users.length === 0,
      numberOfElements: users.length
    });
  }

  // Add roles to users (bidirectional relationship)
  const userRoleMap: Record<string, number> = {
    "1": 1, // User 1 has role 1 (Admin)
    "2": 2, // User 2 has role 2 (Manager)
    "3": 3, // User 3 has role 3 (Operator)
    "4": 4, // User 4 has role 4 (Viewer)
    "5": 5  // User 5 has role 5 (Custom Role)
  };

  for (const [userId, roleId] of Object.entries(userRoleMap)) {
    const roleData = mockRoleTable.rows.find(row => row.data.id === roleId);
    
    if (roleData) {
      addUserRelationship(Number(userId), "role", {
        totalPages: 1,
        currentPage: 0,
        pageSize: 1,
        totalElements: 1,
        tableName: "user_role",
        rows: [roleData],
        first: true,
        last: true,
        empty: false,
        numberOfElements: 1
      });
    }
  }
}

// Mock role details
export const mockRoleDetails: Record<number, any> = {
  1: {
    id: 1,
    name: 'Admin',
    description: 'System administrator with full access',
    userCount: 3,
    status: 'Active',
    created: '2023-01-01',
    lastModified: '2023-05-15',
    permissions: [
      { id: 1, name: 'view_events', description: 'Can view events', granted: true },
      { id: 2, name: 'create_events', description: 'Can create events', granted: true },
      { id: 3, name: 'edit_events', description: 'Can edit events', granted: true },
      { id: 4, name: 'delete_events', description: 'Can delete events', granted: true },
      { id: 5, name: 'view_users', description: 'Can view users', granted: true },
      { id: 6, name: 'create_users', description: 'Can create users', granted: true },
      { id: 7, name: 'edit_users', description: 'Can edit users', granted: true },
      { id: 8, name: 'delete_users', description: 'Can delete users', granted: true },
      { id: 9, name: 'view_reports', description: 'Can view reports', granted: true },
      { id: 10, name: 'export_reports', description: 'Can export reports', granted: true },
      { id: 11, name: 'schedule_reports', description: 'Can schedule reports', granted: true }
    ]
  },
  2: {
    id: 2,
    name: 'Manager',
    description: 'Event manager with management capabilities',
    userCount: 5,
    status: 'Active',
    created: '2023-01-02',
    lastModified: '2023-06-10',
    permissions: [
      { id: 1, name: 'view_events', description: 'Can view events', granted: true },
      { id: 2, name: 'create_events', description: 'Can create events', granted: true },
      { id: 3, name: 'edit_events', description: 'Can edit events', granted: true },
      { id: 4, name: 'delete_events', description: 'Can delete events', granted: false },
      { id: 5, name: 'view_users', description: 'Can view users', granted: true },
      { id: 6, name: 'create_users', description: 'Can create users', granted: false },
      { id: 7, name: 'edit_users', description: 'Can edit users', granted: false },
      { id: 8, name: 'delete_users', description: 'Can delete users', granted: false },
      { id: 9, name: 'view_reports', description: 'Can view reports', granted: true },
      { id: 10, name: 'export_reports', description: 'Can export reports', granted: true },
      { id: 11, name: 'schedule_reports', description: 'Can schedule reports', granted: false }
    ]
  }
};

// Initialize relationships between roles and users
initializeRoleUserRelationships();
