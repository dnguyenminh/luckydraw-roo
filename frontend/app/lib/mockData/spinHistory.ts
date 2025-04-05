import { 
  TableFetchResponse, 
  Column, 
  FieldType, 
  SortType, 
  ObjectType,
  RelatedLinkedObjectsMap,
  RelatedLinkedObject
} from './interfaces';
// Temporarily comment out the import to avoid circular references
// Import participant relationship function from a utility file instead
// import { addParticipantRelationship } from './participants'; 

// Define columns for the spin history table
const spinHistoryColumns: Column[] = [
  { fieldName: 'id', fieldType: FieldType.NUMBER, sortType: SortType.ASCENDING, displayName: 'ID', filterable: true },
  { fieldName: 'participantName', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Participant', filterable: true },
  { fieldName: 'participantEmail', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Email', filterable: true },
  { fieldName: 'eventName', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Event', filterable: true },
  { fieldName: 'timestamp', fieldType: FieldType.DATETIME, sortType: SortType.DESCENDING, displayName: 'Time', filterable: true },
  { fieldName: 'reward', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Reward', filterable: true },
  { fieldName: 'provinceName', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Province', filterable: true },
  { fieldName: 'goldenHour', fieldType: FieldType.BOOLEAN, sortType: SortType.NONE, displayName: 'Golden Hour', filterable: true }
];

// Create related linked objects for spin history
const spinHistoryRelatedObjects: RelatedLinkedObjectsMap = {
  // No related objects needed for spin history in this example
};

// Create mock spin history data
export const mockSpinHistoryTable: TableFetchResponse = {
  totalPages: 2,
  currentPage: 0,
  pageSize: 10,
  totalElements: 15,
  tableName: 'spin_history',
  columns: spinHistoryColumns,
  rows: [
    {
      data: {
        id: 1,
        participantName: 'John Doe',
        participantEmail: 'john.doe@example.com',
        eventName: 'Summer Giveaway',
        timestamp: '2023-06-15T10:30:00Z',
        reward: 'Gold Medal',
        provinceName: 'Province A',
        goldenHour: true
      }
    },
    {
      data: {
        id: 2,
        participantName: 'John Doe',
        participantEmail: 'john.doe@example.com',
        eventName: 'Summer Giveaway',
        timestamp: '2023-06-20T14:45:00Z',
        reward: null,
        provinceName: 'Province A',
        goldenHour: true
      }
    },
    {
      data: {
        id: 3,
        participantName: 'Jane Smith',
        participantEmail: 'jane.smith@example.com',
        eventName: 'Summer Giveaway',
        timestamp: '2023-06-18T09:15:00Z',
        reward: 'Silver Medal',
        provinceName: 'Province A',
        goldenHour: true
      }
    },
    {
      data: {
        id: 4,
        participantName: 'Bob Johnson',
        participantEmail: 'bob.johnson@example.com',
        eventName: 'Spring Festival',
        timestamp: '2023-03-10T11:20:00Z',
        reward: 'Gift Card',
        provinceName: 'Province B',
        goldenHour: false
      }
    },
    {
      data: {
        id: 5,
        participantName: 'Bob Johnson',
        participantEmail: 'bob.johnson@example.com',
        eventName: 'Spring Festival',
        timestamp: '2023-03-15T16:35:00Z',
        reward: null,
        provinceName: 'Province B',
        goldenHour: true
      }
    },
    {
      data: {
        id: 6,
        participantName: 'Alice Brown',
        participantEmail: 'alice.brown@example.com',
        eventName: 'Summer Giveaway',
        timestamp: '2023-06-22T08:50:00Z',
        reward: 'Silver Medal',
        provinceName: 'Province B',
        goldenHour: true
      }
    },
    {
      data: {
        id: 7,
        participantName: 'Alice Brown',
        participantEmail: 'alice.brown@example.com',
        eventName: 'Spring Festival',
        timestamp: '2023-03-20T15:40:00Z',
        reward: 'Gift Card',
        provinceName: 'Province B',
        goldenHour: true
      }
    },
    {
      data: {
        id: 8,
        participantName: 'Frank Miller',
        participantEmail: 'frank.miller@example.com',
        eventName: 'Summer Giveaway',
        timestamp: '2023-06-25T09:10:00Z',
        reward: 'Gold Medal',
        provinceName: 'Province D',
        goldenHour: true
      }
    },
    {
      data: {
        id: 9,
        participantName: 'Frank Miller',
        participantEmail: 'frank.miller@example.com',
        eventName: 'Spring Festival',
        timestamp: '2023-04-05T13:25:00Z',
        reward: 'Gift Card',
        provinceName: 'Province D',
        goldenHour: false
      }
    },
    {
      data: {
        id: 10,
        participantName: 'Grace Taylor',
        participantEmail: 'grace.taylor@example.com',
        eventName: 'Summer Giveaway',
        timestamp: '2023-06-30T16:05:00Z',
        reward: 'Silver Medal',
        provinceName: 'Province D',
        goldenHour: true
      }
    }
    // Additional spin history entries would be included here
  ],
  relatedLinkedObjects: spinHistoryRelatedObjects,
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
    objectType: ObjectType.SPIN_HISTORY
  },
  statistics: {
    totalSpins: 15,
    winningSpins: 9,
    goldenHourSpins: 8,
    regularSpins: 7
  }
};

// Create participant-specific spin history
export const participantSpinHistory: Record<number, any> = {
  1: [ // Spin history for John Doe
    {
      id: 1,
      eventName: 'Summer Giveaway',
      timestamp: '2023-06-15T10:30:00Z',
      reward: 'Gold Medal',
      goldenHour: true
    },
    {
      id: 2,
      eventName: 'Summer Giveaway',
      timestamp: '2023-06-20T14:45:00Z',
      reward: null,
      goldenHour: true
    }
  ],
  2: [ // Spin history for Jane Smith
    {
      id: 3,
      eventName: 'Summer Giveaway',
      timestamp: '2023-06-18T09:15:00Z',
      reward: 'Silver Medal',
      goldenHour: true
    }
  ],
  // Additional participant spin histories would be included here
};

// Function to add relationships to spin history
export function addSpinHistoryRelationship(
  spinHistoryId: number,
  relationName: string,
  relationObjects: RelatedLinkedObject[]
) {
  if (!mockSpinHistoryTable.relatedLinkedObjects) {
    mockSpinHistoryTable.relatedLinkedObjects = {};
  }
  
  if (!mockSpinHistoryTable.relatedLinkedObjects[relationName]) {
    mockSpinHistoryTable.relatedLinkedObjects[relationName] = {};
  }
  
  mockSpinHistoryTable.relatedLinkedObjects[relationName][spinHistoryId] = relationObjects;
}

// Modify the initialization function to avoid circular dependencies
function initializeSpinHistoryParticipantRelationships() {
  // This function would be called later after resolving circular dependencies
  // or through a separate initialization utility
  console.log('Spin history relationships will be initialized separately');
}

// Initialize relationships when module is loaded
initializeSpinHistoryParticipantRelationships();
