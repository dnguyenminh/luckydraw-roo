import { 
  TableFetchResponse, 
  Column, 
  FieldType, 
  SortType, 
  ObjectType,
  RelatedLinkedObjectsMap,
  RelatedLinkedObject
} from './interfaces';

// Define columns for the participants table
const participantColumns: Column[] = [
  { 
    fieldName: 'id', 
    fieldType: FieldType.NUMBER, 
    sortType: SortType.ASCENDING, 
    displayName: 'ID', 
    filterable: true 
  },
  { 
    fieldName: 'name', 
    fieldType: FieldType.STRING, 
    sortType: SortType.NONE, 
    displayName: 'Name', 
    filterable: true 
  },
  { 
    fieldName: 'email', 
    fieldType: FieldType.STRING, 
    sortType: SortType.NONE, 
    displayName: 'Email', 
    filterable: true 
  },
  { 
    fieldName: 'province', 
    fieldType: FieldType.STRING, 
    sortType: SortType.NONE, 
    displayName: 'Province', 
    filterable: true 
  },
  { 
    fieldName: 'status', 
    fieldType: FieldType.STRING, 
    sortType: SortType.NONE, 
    displayName: 'Status', 
    filterable: true 
  },
  { 
    fieldName: 'spins', 
    fieldType: FieldType.NUMBER, 
    sortType: SortType.NONE, 
    displayName: 'Spins', 
    filterable: false 
  },
  { 
    fieldName: 'wins', 
    fieldType: FieldType.NUMBER, 
    sortType: SortType.NONE, 
    displayName: 'Wins', 
    filterable: false 
  }
];

// Create related linked objects for participants
const participantRelatedObjects: RelatedLinkedObjectsMap = {
  // Spin histories related to participants
  spinHistory: {
    '1': [
      { 
        objectType: ObjectType.SPIN_HISTORY, 
        id: 1, 
        name: `Spin #1 - Summer Giveaway`, // Add required name property
        eventName: 'Summer Giveaway', 
        timestamp: '2023-06-15T10:30:00Z', 
        reward: 'Gold Medal' 
      },
      { 
        objectType: ObjectType.SPIN_HISTORY, 
        id: 2, 
        name: `Spin #2 - Summer Giveaway`, // Add required name property
        eventName: 'Summer Giveaway', 
        timestamp: '2023-06-20T14:45:00Z', 
        reward: null 
      }
    ],
    '2': [
      { 
        objectType: ObjectType.SPIN_HISTORY, 
        id: 3, 
        name: `Spin #3 - Summer Giveaway`, // Add required name property
        eventName: 'Summer Giveaway', 
        timestamp: '2023-06-18T09:15:00Z', 
        reward: 'Silver Medal' 
      }
    ],
    '3': [
      { 
        objectType: ObjectType.SPIN_HISTORY, 
        id: 4, 
        name: `Spin #4 - Spring Festival`, // Add required name property
        eventName: 'Spring Festival', 
        timestamp: '2023-03-10T11:20:00Z', 
        reward: 'Gift Card' 
      },
      { 
        objectType: ObjectType.SPIN_HISTORY, 
        id: 5,
        name: `Spin #5 - Spring Festival`, // Add required name property
        eventName: 'Spring Festival', 
        timestamp: '2023-03-15T16:35:00Z', 
        reward: null 
      }
    ]
    // Add more spin history for other participants as needed
  }
};

// Create mock participant data
export const mockParticipantTable: TableFetchResponse = {
  totalPages: 2,
  currentPage: 0,
  pageSize: 10,
  totalElements: 16,
  tableName: 'participants',
  columns: participantColumns,
  rows: [
    {
      data: {
        id: 1,
        name: 'John Doe',
        email: 'john.doe@example.com',
        province: 'Province A',
        status: 'Active',
        spins: 5,
        wins: 1
      }
    },
    {
      data: {
        id: 2,
        name: 'Jane Smith',
        email: 'jane.smith@example.com',
        province: 'Province A',
        status: 'Active',
        spins: 3,
        wins: 1
      }
    },
    {
      data: {
        id: 3,
        name: 'Bob Johnson',
        email: 'bob.johnson@example.com',
        province: 'Province B',
        status: 'Active',
        spins: 4,
        wins: 0
      }
    },
    {
      data: {
        id: 4,
        name: 'Alice Brown',
        email: 'alice.brown@example.com',
        province: 'Province B',
        status: 'Active',
        spins: 6,
        wins: 2
      }
    },
    {
      data: {
        id: 5,
        name: 'Charlie Davis',
        email: 'charlie.davis@example.com',
        province: 'Province C',
        status: 'Active',
        spins: 2,
        wins: 0
      }
    },
    {
      data: {
        id: 6,
        name: 'Eva Wilson',
        email: 'eva.wilson@example.com',
        province: 'Province C',
        status: 'Inactive',
        spins: 0,
        wins: 0
      }
    },
    {
      data: {
        id: 7,
        name: 'Frank Miller',
        email: 'frank.miller@example.com',
        province: 'Province D',
        status: 'Active',
        spins: 7,
        wins: 3
      }
    },
    {
      data: {
        id: 8,
        name: 'Grace Taylor',
        email: 'grace.taylor@example.com',
        province: 'Province D',
        status: 'Active',
        spins: 4,
        wins: 1
      }
    },
    {
      data: {
        id: 9,
        name: 'Henry Clark',
        email: 'henry.clark@example.com',
        province: 'Province E',
        status: 'Active',
        spins: 3,
        wins: 0
      }
    },
    {
      data: {
        id: 10,
        name: 'Ivy Martinez',
        email: 'ivy.martinez@example.com',
        province: 'Province E',
        status: 'Active',
        spins: 5,
        wins: 2
      }
    }
    // More participant data would be added for the remaining participants
  ],
  relatedLinkedObjects: participantRelatedObjects,
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
    objectType: ObjectType.PARTICIPANT
  },
  statistics: {
    totalParticipants: 16,
    activeParticipants: 15,
    totalSpins: 52,
    totalWins: 12
  }
};

// Function to add relationships to participants
export function addParticipantRelationship(
  participantId: number,
  relationName: string,
  relationObjects: RelatedLinkedObject[]
) {
  if (!mockParticipantTable.relatedLinkedObjects) {
    mockParticipantTable.relatedLinkedObjects = {};
  }
  
  if (!mockParticipantTable.relatedLinkedObjects[relationName]) {
    mockParticipantTable.relatedLinkedObjects[relationName] = {};
  }
  
  mockParticipantTable.relatedLinkedObjects[relationName][participantId] = relationObjects;
}

// Create mock participant profiles/details
export const mockParticipantProfiles: Record<number, any> = {
  1: {
    id: 1,
    fullName: 'John Doe',
    email: 'john.doe@example.com',
    phone: '+1-555-123-4567',
    province: 'Province A',
    address: '123 Main St, City A',
    joinDate: '2023-05-01',
    status: 'Active',
    totalSpins: 5,
    availableSpins: 2,
    winCount: 1
  },
  2: {
    id: 2,
    fullName: 'Jane Smith',
    email: 'jane.smith@example.com',
    phone: '+1-555-234-5678',
    province: 'Province A',
    address: '456 Elm St, City A',
    joinDate: '2023-05-05',
    status: 'Active',
    totalSpins: 3,
    availableSpins: 1,
    winCount: 1
  },
  3: {
    id: 3,
    fullName: 'Bob Johnson',
    email: 'bob.johnson@example.com',
    phone: '+1-555-345-6789',
    province: 'Province B',
    address: '789 Oak St, City B',
    joinDate: '2023-05-10',
    status: 'Active',
    totalSpins: 4,
    availableSpins: 2,
    winCount: 0
  },
  4: {
    id: 4,
    fullName: 'Alice Brown',
    email: 'alice.brown@example.com',
    phone: '+1-555-456-7890',
    province: 'Province B',
    address: '101 Pine St, City B',
    joinDate: '2023-05-15',
    status: 'Active',
    totalSpins: 6,
    availableSpins: 0,
    winCount: 2
  },
  5: {
    id: 5,
    fullName: 'Charlie Davis',
    email: 'charlie.davis@example.com',
    phone: '+1-555-567-8901',
    province: 'Province C',
    address: '202 Maple St, City C',
    joinDate: '2023-05-20',
    status: 'Active',
    totalSpins: 2,
    availableSpins: 2,
    winCount: 0
  }
  // Additional profiles would be added for the remaining participants
};
