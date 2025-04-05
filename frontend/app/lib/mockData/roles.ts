import { 
  TableFetchResponse, 
  Column, 
  FieldType, 
  SortType, 
  ObjectType,
  RelatedLinkedObjectsMap
} from './interfaces';
import { mockUserTable, addUserRelationship } from './users';

// Define columns for the roles table
const roleColumns: Column[] = [
  { fieldName: 'id', fieldType: FieldType.NUMBER, sortType: SortType.ASCENDING, displayName: 'ID', filterable: true },
  { fieldName: 'name', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Name', filterable: true },
  { fieldName: 'description', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Description', filterable: true },
  { fieldName: 'userCount', fieldType: FieldType.NUMBER, sortType: SortType.NONE, displayName: 'Users', filterable: false },
  { fieldName: 'permissionCount', fieldType: FieldType.NUMBER, sortType: SortType.NONE, displayName: 'Permissions', filterable: false }
];

// Create related linked objects for roles
const roleRelatedObjects: RelatedLinkedObjectsMap = {
  users: {
    '1': [
      { objectType: ObjectType.USER, id: 1, name: 'admin' }
    ],
    '2': [
      { objectType: ObjectType.USER, id: 2, name: 'manager' },
      { objectType: ObjectType.USER, id: 4, name: 'supervisor' }
    ],
    '3': [
      { objectType: ObjectType.USER, id: 3, name: 'user1' },
      { objectType: ObjectType.USER, id: 4, name: 'supervisor' }
    ]
  },
  permissions: {
    '1': [
      { objectType: ObjectType.ROLE, id: 1, name: 'FULL_ACCESS' },
      { objectType: ObjectType.ROLE, id: 2, name: 'USER_MANAGEMENT' },
      { objectType: ObjectType.ROLE, id: 3, name: 'EVENT_MANAGEMENT' },
      { objectType: ObjectType.ROLE, id: 4, name: 'REWARD_MANAGEMENT' },
      { objectType: ObjectType.ROLE, id: 5, name: 'AUDIT_VIEW' }
    ],
    '2': [
      { objectType: ObjectType.ROLE, id: 3, name: 'EVENT_MANAGEMENT' },
      { objectType: ObjectType.ROLE, id: 4, name: 'REWARD_MANAGEMENT' },
      { objectType: ObjectType.ROLE, id: 5, name: 'AUDIT_VIEW' }
    ],
    '3': [
      { objectType: ObjectType.ROLE, id: 6, name: 'BASIC_ACCESS' }
    ]
  }
};

// Create mock role data
export const mockRoleTable: TableFetchResponse = {
  totalPages: 1,
  currentPage: 0,
  pageSize: 10,
  totalElements: 3,
  tableName: 'roles',
  columns: roleColumns,
  rows: [
    {
      data: {
        id: 1,
        name: 'ADMIN',
        description: 'System administrator with full access',
        userCount: 1,
        permissionCount: 5
      }
    },
    {
      data: {
        id: 2,
        name: 'MANAGER',
        description: 'Event and reward management access',
        userCount: 2,
        permissionCount: 3
      }
    },
    {
      data: {
        id: 3,
        name: 'USER',
        description: 'Basic system access',
        userCount: 2,
        permissionCount: 1
      }
    }
  ],
  relatedLinkedObjects: roleRelatedObjects,
  first: true,
  last: true,
  empty: false,
  numberOfElements: 3,
  originalRequest: {
    page: 0,
    size: 10,
    sorts: [],
    filters: [],
    search: {},
    objectType: ObjectType.ROLE
  },
  statistics: {
    totalRoles: 3,
    totalUsers: 4,
    totalPermissions: 6
  }
};

// Function to add relationships to roles
export function addRoleRelationship(
  roleId: number, 
  relationName: string, 
  relationData: any
) {
  if (!mockRoleTable.relatedLinkedObjects) {
    mockRoleTable.relatedLinkedObjects = {};
  }
  
  if (!mockRoleTable.relatedLinkedObjects[relationName]) {
    mockRoleTable.relatedLinkedObjects[relationName] = {};
  }
  
  mockRoleTable.relatedLinkedObjects[relationName][roleId] = relationData;
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
      // FIX: Convert to proper RelatedLinkedObject[] format
      const roleObject = {
        objectType: ObjectType.ROLE,
        id: roleData.data.id,
        name: roleData.data.name || `Role_${roleData.data.id}`
      };
      
      // Pass an array of RelatedLinkedObject instead of a table structure
      addUserRelationship(Number(userId), "role", [roleObject]);
    }
  }
}

// Create mock role details
export const mockRoleDetails: Record<number, any> = {
  1: {
    id: 1,
    name: 'ADMIN',
    description: 'System administrator with full access to all features',
    created: '2023-01-01T00:00:00Z',
    lastModified: '2023-01-01T00:00:00Z',
    users: [
      { id: 1, username: 'admin', fullName: 'System Administrator' }
    ],
    permissions: [
      { id: 1, name: 'FULL_ACCESS', description: 'Full system access' },
      { id: 2, name: 'USER_MANAGEMENT', description: 'User management' },
      { id: 3, name: 'EVENT_MANAGEMENT', description: 'Event management' },
      { id: 4, name: 'REWARD_MANAGEMENT', description: 'Reward management' },
      { id: 5, name: 'AUDIT_VIEW', description: 'Audit log access' }
    ]
  },
  // Additional role details would be included here
};

// Initialize relationships between roles and users
initializeRoleUserRelationships();
