import { 
  TableFetchResponse, 
  Column, 
  FieldType, 
  SortType, 
  ObjectType,
  RelatedLinkedObjectsMap,
  RelatedLinkedObject
} from './interfaces';

// Define columns for the users table
const userColumns: Column[] = [
  { fieldName: 'id', fieldType: FieldType.NUMBER, sortType: SortType.ASCENDING, displayName: 'ID', filterable: true },
  { fieldName: 'username', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Username', filterable: true },
  { fieldName: 'fullName', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Full Name', filterable: true },
  { fieldName: 'email', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Email', filterable: true },
  { fieldName: 'role', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Role', filterable: true },
  { fieldName: 'lastLogin', fieldType: FieldType.DATETIME, sortType: SortType.NONE, displayName: 'Last Login', filterable: true },
  { fieldName: 'status', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Status', filterable: true }
];

// Create related linked objects for users
const userRelatedObjects: RelatedLinkedObjectsMap = {
  roles: {
    '1': [
      { objectType: ObjectType.ROLE, id: 1, name: 'ADMIN' }
    ],
    '2': [
      { objectType: ObjectType.ROLE, id: 2, name: 'MANAGER' }
    ],
    '3': [
      { objectType: ObjectType.ROLE, id: 3, name: 'USER' }
    ],
    '4': [
      { objectType: ObjectType.ROLE, id: 2, name: 'MANAGER' },
      { objectType: ObjectType.ROLE, id: 3, name: 'USER' }
    ]
  }
};

// Create mock user data
export const mockUserTable: TableFetchResponse = {
  totalPages: 1,
  currentPage: 0,
  pageSize: 10,
  totalElements: 4,
  tableName: 'users',
  columns: userColumns,
  rows: [
    {
      data: {
        id: 1,
        username: 'admin',
        fullName: 'System Administrator',
        email: 'admin@example.com',
        role: 'ADMIN',
        lastLogin: '2023-06-30T08:45:00Z',
        status: 'Active'
      }
    },
    {
      data: {
        id: 2,
        username: 'manager',
        fullName: 'Event Manager',
        email: 'manager@example.com',
        role: 'MANAGER',
        lastLogin: '2023-06-29T14:30:00Z',
        status: 'Active'
      }
    },
    {
      data: {
        id: 3,
        username: 'user1',
        fullName: 'Regular User',
        email: 'user1@example.com',
        role: 'USER',
        lastLogin: '2023-06-28T10:15:00Z',
        status: 'Active'
      }
    },
    {
      data: {
        id: 4,
        username: 'supervisor',
        fullName: 'Department Supervisor',
        email: 'supervisor@example.com',
        role: 'MANAGER',
        lastLogin: '2023-06-25T16:20:00Z',
        status: 'Active'
      }
    }
  ],
  relatedLinkedObjects: userRelatedObjects,
  first: true,
  last: true,
  empty: false,
  numberOfElements: 4,
  originalRequest: {
    page: 0,
    size: 10,
    sorts: [],
    filters: [],
    search: {},
    objectType: ObjectType.USER
  },
  statistics: {
    totalUsers: 4,
    activeUsers: 4,
    inactiveUsers: 0
  }
};

// Function to add relationships to users
export function addUserRelationship(
  userId: number,
  relationName: string,
  relationObjects: RelatedLinkedObject[]
) {
  if (!mockUserTable.relatedLinkedObjects) {
    mockUserTable.relatedLinkedObjects = {};
  }
  
  if (!mockUserTable.relatedLinkedObjects[relationName]) {
    mockUserTable.relatedLinkedObjects[relationName] = {};
  }
  
  mockUserTable.relatedLinkedObjects[relationName][userId] = relationObjects;
}

// Create mock user details
export const mockUserDetails: Record<number, any> = {
  1: {
    id: 1,
    username: 'admin',
    fullName: 'System Administrator',
    email: 'admin@example.com',
    phone: '+1-555-123-4567',
    department: 'IT',
    roles: ['ADMIN'],
    permissions: ['FULL_ACCESS'],
    lastLogin: '2023-06-30T08:45:00Z',
    created: '2023-01-01T00:00:00Z',
    status: 'Active'
  },
  // Additional user details would be included here
};
