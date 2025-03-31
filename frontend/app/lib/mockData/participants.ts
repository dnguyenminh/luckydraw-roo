import { TableFetchResponse, FieldType, SortType } from './interfaces';

// Mock participant table data
export const mockParticipantTable: TableFetchResponse = {
  totalPages: 3,
  currentPage: 0,
  pageSize: 10,
  totalElements: 25,
  tableName: "participants",
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
      fieldName: "province", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Province",
      filterable: true
    },
    { 
      fieldName: "status", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Status",
      filterable: true
    },
    { 
      fieldName: "spins", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "Spins",
      filterable: true
    },
    { 
      fieldName: "wins", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "Wins",
      filterable: true
    }
  ],
  rows: [
    {
      data: { id: 1, name: "John Smith", email: "john.smith@example.com", province: "Western Province", status: "Active", spins: 45, wins: 12 }
    },
    {
      data: { id: 2, name: "Jane Doe", email: "jane.doe@example.com", province: "Eastern Province", status: "Active", spins: 32, wins: 8 }
    },
    {
      data: { id: 3, name: "Bob Johnson", email: "bob.j@example.com", province: "Northern Province", status: "Active", spins: 27, wins: 5 }
    },
    {
      data: { id: 4, name: "Alice Williams", email: "alice.w@example.com", province: "Southern Province", status: "Inactive", spins: 18, wins: 3 }
    },
    {
      data: { id: 5, name: "Charlie Brown", email: "charlie.b@example.com", province: "Central Province", status: "Active", spins: 22, wins: 7 }
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

// Function to add relationships to participant table
export function addParticipantRelationship(
  participantId: number, 
  relationName: string, 
  relationData: any
) {
  if (!mockParticipantTable.relatedTables) {
    mockParticipantTable.relatedTables = {};
  }
  
  if (!mockParticipantTable.relatedTables[relationName]) {
    mockParticipantTable.relatedTables[relationName] = {};
  }
  
  mockParticipantTable.relatedTables[relationName][participantId] = relationData;
}

// Mock data for participant profiles
export const mockParticipantProfiles: Record<number, any> = {
  1: {
    id: 1,
    name: "John Smith",
    email: "john.smith@example.com",
    phone: "+1234567890",
    province: "Western Province",
    address: "123 Main St",
    registrationDate: "2023-01-15",
    status: "Active",
    totalSpins: 45,
    spinsRemaining: 5,
    winCount: 12
  },
  2: {
    id: 2,
    name: "Jane Doe",
    email: "jane.doe@example.com",
    phone: "+0987654321",
    province: "Eastern Province",
    address: "456 Oak Ave",
    registrationDate: "2023-02-20",
    status: "Active",
    totalSpins: 32,
    spinsRemaining: 0,
    winCount: 8
  }
};

// Mock data for participant details
export const mockParticipantDetails: Record<number, any> = {
  1: {
    id: 1,
    fullName: "John Smith",
    province: "Western Province",
    address: "123 Main St",
    joinDate: "2023-01-15",
    status: "Active",
    totalSpins: 45,
    availableSpins: 5,
    winCount: 12
  },
  2: {
    id: 2,
    fullName: "Jane Doe",
    province: "Eastern Province",
    address: "456 Oak Ave",
    joinDate: "2023-02-20",
    status: "Active",
    totalSpins: 32,
    availableSpins: 0,
    winCount: 8
  }
};

// Mock data for participated events by participant
export const mockParticipatedEvents: Record<string, any[]> = {
  "1": [
    { id: 1, name: "Summer Giveaway", startTime: "2023-06-01", endTime: "2023-08-31", status: "Active", spins: 35, wins: 10 },
    { id: 2, name: "Fall Promotion", startTime: "2023-09-01", endTime: "2023-11-30", status: "Active", spins: 10, wins: 2 }
  ],
  "2": [
    { id: 1, name: "Summer Giveaway", startTime: "2023-06-01", endTime: "2023-08-31", status: "Active", spins: 22, wins: 5 },
    { id: 3, name: "Back to School", startTime: "2023-08-15", endTime: "2023-09-15", status: "Active", spins: 10, wins: 3 }
  ]
};
