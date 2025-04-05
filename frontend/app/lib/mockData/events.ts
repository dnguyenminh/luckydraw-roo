import { 
  TableFetchResponse, 
  Column, 
  FieldType, 
  SortType,
  ObjectType,
  RelatedLinkedObjectsMap,
  RelatedLinkedObject // Add missing import
} from './interfaces';

// Define columns for the events table
const eventColumns: Column[] = [
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
    fieldName: 'description', 
    fieldType: FieldType.STRING, 
    sortType: SortType.NONE, 
    displayName: 'Description', 
    filterable: true 
  },
  { 
    fieldName: 'startDate', 
    fieldType: FieldType.DATE, 
    sortType: SortType.NONE, 
    displayName: 'Start Date', 
    filterable: true 
  },
  { 
    fieldName: 'endDate', 
    fieldType: FieldType.DATE, 
    sortType: SortType.NONE, 
    displayName: 'End Date', 
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
    fieldName: 'participantCount', 
    fieldType: FieldType.NUMBER, 
    sortType: SortType.NONE, 
    displayName: 'Participants', 
    filterable: false 
  },
  { 
    fieldName: 'winnerCount', 
    fieldType: FieldType.NUMBER, 
    sortType: SortType.NONE, 
    displayName: 'Winners', 
    filterable: false 
  },
  { 
    fieldName: 'spinCount', 
    fieldType: FieldType.NUMBER, 
    sortType: SortType.NONE, 
    displayName: 'Spins', 
    filterable: false 
  },
  { 
    fieldName: 'rewardCount', 
    fieldType: FieldType.NUMBER, 
    sortType: SortType.NONE, 
    displayName: 'Rewards', 
    filterable: false 
  }
];

// Create related linked objects for events
const eventRelatedObjects: RelatedLinkedObjectsMap = {
  // Regions related to events
  regions: {
    '1': [
      { objectType: ObjectType.REGION, id: 1, name: 'North' },
      { objectType: ObjectType.REGION, id: 2, name: 'South' }
    ],
    '2': [
      { objectType: ObjectType.REGION, id: 3, name: 'East' },
      { objectType: ObjectType.REGION, id: 4, name: 'West' }
    ],
    '3': [
      { objectType: ObjectType.REGION, id: 1, name: 'North' },
      { objectType: ObjectType.REGION, id: 4, name: 'West' }
    ]
  },
  // Rewards related to events
  rewards: {
    '1': [
      { objectType: ObjectType.REWARD, id: 1, name: 'Gold Medal', quantity: 5 },
      { objectType: ObjectType.REWARD, id: 2, name: 'Silver Medal', quantity: 10 }
    ],
    '2': [
      { objectType: ObjectType.REWARD, id: 3, name: 'Bronze Medal', quantity: 20 },
      { objectType: ObjectType.REWARD, id: 4, name: 'Trophy', quantity: 3 }
    ],
    '3': [
      { objectType: ObjectType.REWARD, id: 5, name: 'Cash Prize', quantity: 5 },
      { objectType: ObjectType.REWARD, id: 6, name: 'Gift Card', quantity: 15 }
    ]
  }
};

// Create mock event data
export const mockEventTable: TableFetchResponse = {
  totalPages: 2,
  currentPage: 0,
  pageSize: 10,
  totalElements: 13,
  tableName: 'events',
  columns: eventColumns,
  rows: [
    {
      data: {
        id: 1,
        name: 'Summer Giveaway',
        description: 'Summer season lucky draw event',
        startDate: '2023-06-01',
        endDate: '2023-08-31',
        status: 'Active',
        participantCount: 2500,
        winnerCount: 120,
        spinCount: 5000,
        rewardCount: 150
      }
    },
    {
      data: {
        id: 2,
        name: 'Winter Wonderland',
        description: 'Winter holiday special event',
        startDate: '2023-12-01',
        endDate: '2024-01-15',
        status: 'Upcoming',
        participantCount: 0,
        winnerCount: 0,
        spinCount: 0,
        rewardCount: 200
      }
    },
    {
      data: {
        id: 3,
        name: 'Spring Festival',
        description: 'Celebrate spring with prizes',
        startDate: '2023-03-01',
        endDate: '2023-04-15',
        status: 'Completed',
        participantCount: 1800,
        winnerCount: 95,
        spinCount: 3600,
        rewardCount: 100
      }
    },
    // Add more event data as needed
  ],
  relatedLinkedObjects: eventRelatedObjects,
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
    objectType: ObjectType.EVENT
  },
  statistics: {
    totalEvents: 13,
    activeEvents: 5,
    completedEvents: 7,
    upcomingEvents: 1
  }
};

// Function to add relationships to events
export function addEventRelationship(
  eventId: number,
  relationName: string,
  relationObjects: RelatedLinkedObject[] // Now properly typed with the imported type
) {
  if (!mockEventTable.relatedLinkedObjects) {
    mockEventTable.relatedLinkedObjects = {};
  }
  
  if (!mockEventTable.relatedLinkedObjects[relationName]) {
    mockEventTable.relatedLinkedObjects[relationName] = {};
  }
  
  mockEventTable.relatedLinkedObjects[relationName][eventId] = relationObjects;
}

// Create mock event details
export const mockEventDetails: Record<number, any> = {
  1: {
    id: 1,
    name: 'Summer Giveaway',
    description: 'Summer season lucky draw event with amazing prizes for participants across all regions. Join and win big!',
    startDate: '2023-06-01',
    endDate: '2023-08-31',
    status: 'Active',
    participantCount: 2500,
    winnerCount: 120,
    spinCount: 5000,
    rewardCount: 150,
    createdBy: 'admin',
    createdDate: '2023-05-15T09:30:00Z',
    modifiedBy: 'manager',
    modifiedDate: '2023-05-28T14:45:00Z',
    regions: [
      { id: 1, name: 'North' },
      { id: 2, name: 'South' }
    ],
    rewards: [
      { id: 1, name: 'Gold Medal', quantity: 5 },
      { id: 2, name: 'Silver Medal', quantity: 10 }
    ]
  },
  2: {
    id: 2,
    name: 'Winter Wonderland',
    description: 'Winter holiday special event with festive prizes and special rewards for the holiday season.',
    startDate: '2023-12-01',
    endDate: '2024-01-15',
    status: 'Upcoming',
    participantCount: 0,
    winnerCount: 0,
    spinCount: 0,
    rewardCount: 200,
    createdBy: 'admin',
    createdDate: '2023-09-10T10:15:00Z',
    modifiedBy: 'admin',
    modifiedDate: '2023-10-05T16:30:00Z',
    regions: [
      { id: 3, name: 'East' },
      { id: 4, name: 'West' }
    ],
    rewards: [
      { id: 3, name: 'Bronze Medal', quantity: 20 },
      { id: 4, name: 'Trophy', quantity: 3 }
    ]
  },
  3: {
    id: 3,
    name: 'Spring Festival',
    description: 'Celebrate spring with prizes and special rewards for all participants. A season of growth and winning!',
    startDate: '2023-03-01',
    endDate: '2023-04-15',
    status: 'Completed',
    participantCount: 1800,
    winnerCount: 95,
    spinCount: 3600,
    rewardCount: 100,
    createdBy: 'manager',
    createdDate: '2023-01-20T08:45:00Z',
    modifiedBy: 'supervisor',
    modifiedDate: '2023-02-15T11:20:00Z',
    regions: [
      { id: 1, name: 'North' },
      { id: 4, name: 'West' }
    ],
    rewards: [
      { id: 5, name: 'Cash Prize', quantity: 5 },
      { id: 6, name: 'Gift Card', quantity: 15 }
    ]
  }
};
