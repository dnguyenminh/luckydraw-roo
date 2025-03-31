import { TableFetchResponse, FieldType, SortType } from './interfaces';
import { addParticipantRelationship } from './participants';

// Mock spin history table data
export const mockSpinHistoryTable: TableFetchResponse = {
  totalPages: 25,
  currentPage: 0,
  pageSize: 20,
  totalElements: 487,
  tableName: "spin_history",
  columns: [
    { 
      fieldName: "id", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "ID",
      filterable: false
    },
    { 
      fieldName: "participantName", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Participant Name",
      filterable: true
    },
    { 
      fieldName: "participantEmail", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Email",
      filterable: true
    },
    { 
      fieldName: "timestamp", 
      fieldType: FieldType.DATETIME, 
      sortType: SortType.DESCENDING,
      displayName: "Date & Time",
      filterable: true
    },
    { 
      fieldName: "eventName", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Event",
      filterable: true
    },
    { 
      fieldName: "result", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Result",
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
      fieldName: "isWinner", 
      fieldType: FieldType.BOOLEAN, 
      sortType: SortType.ASCENDING,
      displayName: "Is Winner",
      filterable: true
    },
    { 
      fieldName: "rewardClaimed", 
      fieldType: FieldType.BOOLEAN, 
      sortType: SortType.ASCENDING,
      displayName: "Claimed",
      filterable: true
    }
  ],
  rows: [
    {
      data: { 
        id: 1, 
        participantName: 'John Smith',
        participantEmail: 'john.smith@example.com',
        timestamp: '2023-09-15 14:32:45',
        result: 'Gift Card ($50)',
        province: 'Western Province',
        eventName: 'Summer Giveaway',
        isWinner: true,
        rewardClaimed: true
      }
    },
    {
      data: { 
        id: 2, 
        participantName: 'Jane Doe',
        participantEmail: 'jane.doe@example.com',
        timestamp: '2023-09-15 12:15:22',
        result: 'Try Again',
        province: 'Eastern Province',
        eventName: 'Summer Giveaway',
        isWinner: false,
        rewardClaimed: false
      }
    },
    {
      data: { 
        id: 3, 
        participantName: 'Robert Johnson',
        participantEmail: 'robert.j@example.com',
        timestamp: '2023-09-14 17:45:11',
        result: 'Free Product',
        province: 'Northern Province',
        eventName: 'Fall Promotion',
        isWinner: true,
        rewardClaimed: false
      }
    },
    {
      data: { 
        id: 4, 
        participantName: 'Sarah Williams',
        participantEmail: 'sarah.w@example.com',
        timestamp: '2023-09-14 09:22:38',
        result: 'Discount Coupon (10%)',
        province: 'Central Province',
        eventName: 'Summer Giveaway',
        isWinner: true,
        rewardClaimed: true
      }
    },
    {
      data: { 
        id: 5, 
        participantName: 'Michael Brown',
        participantEmail: 'michael.b@example.com',
        timestamp: '2023-09-13 16:07:29',
        result: 'Try Again',
        province: 'Western Province',
        eventName: 'Fall Promotion',
        isWinner: false,
        rewardClaimed: false
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
  statistics: { charts: {} },
  first: true,
  last: false,
  empty: false,
  numberOfElements: 20
};

// Import spin history mock data from participants.ts to avoid circular dependencies
import { mockParticipantSpinHistory } from './participants';

// Detailed spin history for specific participants
export const participantSpinHistory: Record<string, TableFetchResponse> = {
  "1": {
    totalPages: 1,
    currentPage: 0,
    pageSize: 10,
    totalElements: 5,
    tableName: "participant_spin_history",
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
        displayName: "Date & Time",
        filterable: true
      },
      { 
        fieldName: "eventName", 
        fieldType: FieldType.STRING, 
        sortType: SortType.ASCENDING,
        displayName: "Event",
        filterable: true
      },
      { 
        fieldName: "result", 
        fieldType: FieldType.STRING, 
        sortType: SortType.ASCENDING,
        displayName: "Result",
        filterable: true
      },
      { 
        fieldName: "isWinner", 
        fieldType: FieldType.BOOLEAN, 
        sortType: SortType.ASCENDING,
        displayName: "Is Winner",
        filterable: true
      },
      { 
        fieldName: "rewardClaimed", 
        fieldType: FieldType.BOOLEAN, 
        sortType: SortType.ASCENDING,
        displayName: "Claimed",
        filterable: true
      }
    ],
    rows: [
      {
        data: { 
          id: 101, 
          timestamp: '2023-09-15 14:32:45',
          eventId: 1,
          eventName: 'Summer Giveaway',
          result: 'Gift Card ($50)',
          isWinner: true,
          rewardId: 1,
          rewardName: 'Gift Card',
          rewardClaimed: true
        }
      },
      // ...other rows
    ],
    originalRequest: {
      page: 0,
      size: 10,
      sorts: [{ field: "timestamp", order: "desc" }],
      filters: [],
      search: {}
    },
    statistics: {},
    first: true,
    last: true,
    empty: false,
    numberOfElements: 5
  },
  "2": {
    // ...similar data for participant 2
    totalPages: 1,
    currentPage: 0,
    pageSize: 10,
    totalElements: 3,
    tableName: "participant_spin_history",
    columns: [
      // Same columns as above
    ],
    rows: [
      {
        data: { 
          id: 201, 
          timestamp: '2023-09-15 09:22:15',
          eventName: 'Fall Promotion',
          result: 'Gift Voucher ($30)',
          isWinner: true,
          rewardClaimed: true
        }
      },
      // ...other rows
    ],
    originalRequest: {
      page: 0,
      size: 10,
      sorts: [{ field: "timestamp", order: "desc" }],
      filters: [],
      search: {}
    },
    statistics: {},
    first: true,
    last: true,
    empty: false,
    numberOfElements: 3
  }
};

// Function to initialize relationships between spin histories and participants
function initializeSpinHistoryParticipantRelationships() {
  // Link spin histories to participants
  const participantIds = ["1", "2"];
  
  participantIds.forEach(participantId => {
    const participantSpins = participantSpinHistory[participantId];
    if (participantSpins) {
      // Add this spin history to the participant's related tables
      addParticipantRelationship(
        Number(participantId),
        "spinHistory",
        participantSpins
      );
    }
  });
}

// Initialize relationships when module is loaded
initializeSpinHistoryParticipantRelationships();
