import { TableFetchResponse, FieldType, SortType, TableInfo } from './interfaces';

// Mock event table data
export const mockEventTable: TableFetchResponse = {
  totalPages: 2,
  currentPage: 0,
  pageSize: 10,
  totalElements: 15,
  tableName: "events",
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
      fieldName: "description", 
      fieldType: FieldType.STRING, 
      sortType: SortType.NONE,
      displayName: "Description",
      filterable: true
    },
    { 
      fieldName: "startDate", 
      fieldType: FieldType.DATE, 
      sortType: SortType.ASCENDING,
      displayName: "Start Date",
      filterable: true
    },
    { 
      fieldName: "endDate", 
      fieldType: FieldType.DATE, 
      sortType: SortType.ASCENDING,
      displayName: "End Date",
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
      fieldName: "participantCount", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "Participants",
      filterable: false
    },
    { 
      fieldName: "winnerCount", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "Winners",
      filterable: false
    },
    { 
      fieldName: "spinCount", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "Spins",
      filterable: false
    },
    { 
      fieldName: "rewardCount", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "Rewards",
      filterable: false
    }
  ],
  rows: [
    {
      data: { 
        id: 1, 
        name: "Summer Giveaway", 
        description: "Special summer promotion with exclusive rewards", 
        startDate: "2023-06-01", 
        endDate: "2023-08-31",
        status: "Active",
        participantCount: 12450,
        winnerCount: 3250,
        spinCount: 45680,
        rewardCount: 6
      }
    },
    {
      data: { 
        id: 2, 
        name: "Fall Promotion", 
        description: "Back to school season special offers", 
        startDate: "2023-09-01", 
        endDate: "2023-11-30",
        status: "Active",
        participantCount: 8720,
        winnerCount: 2180,
        spinCount: 25430,
        rewardCount: 5
      }
    },
    {
      data: { 
        id: 3, 
        name: "Winter Special", 
        description: "Holiday season promotions and giveaways", 
        startDate: "2023-12-01", 
        endDate: "2024-01-31",
        status: "Upcoming",
        participantCount: 0,
        winnerCount: 0,
        spinCount: 0,
        rewardCount: 4
      }
    },
    {
      data: { 
        id: 4, 
        name: "Spring Festival", 
        description: "Spring season celebration with special rewards", 
        startDate: "2023-03-01", 
        endDate: "2023-05-31",
        status: "Completed",
        participantCount: 10240,
        winnerCount: 2785,
        spinCount: 37920,
        rewardCount: 5
      }
    },
    {
      data: { 
        id: 5, 
        name: "Back to School", 
        description: "Special promotion for students", 
        startDate: "2023-08-15", 
        endDate: "2023-09-15",
        status: "Active",
        participantCount: 6340,
        winnerCount: 1520,
        spinCount: 18750,
        rewardCount: 4
      }
    }
  ],
  originalRequest: {
    page: 0,
    size: 10,
    sorts: [{ field: "startDate", order: "desc" }],
    filters: [],
    search: {}
  },
  statistics: {},
  relatedTables: {}, 
  first: true,
  last: false,
  empty: false,
  numberOfElements: 5
};

// Expose a function to add relationships to events
export function addEventRelationship(
  eventId: number, 
  relationName: string, 
  relationData: TableInfo
) {
  if (!mockEventTable.relatedTables) {
    mockEventTable.relatedTables = {};
  }
  
  if (!mockEventTable.relatedTables[relationName]) {
    mockEventTable.relatedTables[relationName] = {};
  }
  
  mockEventTable.relatedTables[relationName][eventId] = relationData;
}

// Mock event details
export const mockEventDetails = {
  1: {
    id: 1,
    name: "Summer Giveaway",
    description: "Special summer promotion with exclusive rewards",
    startDate: "2023-06-01",
    endDate: "2023-08-31",
    status: "Active",
    participantCount: 12450,
    winnerCount: 3250,
    spinCount: 45680,
    rewardCount: 6,
    createdBy: "admin@example.com",
    createdDate: "2023-05-15",
    modifiedBy: "admin@example.com",
    modifiedDate: "2023-05-30",
    regions: [
      { id: 1, name: 'North Region' },
      { id: 2, name: 'South Region' }
    ],
    rewards: [
      { id: 1, name: "Gift Card", quantity: 100 },
      { id: 2, name: "Free Product", quantity: 200 },
      { id: 3, name: "Discount Coupon", quantity: 500 },
      { id: 4, name: "Premium Pass", quantity: 10 }
    ]
  },
  2: {
    id: 2,
    name: "Fall Promotion",
    description: "Back to school season special offers",
    startDate: "2023-09-01",
    endDate: "2023-11-30",
    status: "Active",
    participantCount: 8720,
    winnerCount: 2180,
    spinCount: 25430,
    rewardCount: 5,
    createdBy: "admin@example.com",
    createdDate: "2023-08-15",
    modifiedBy: "admin@example.com",
    modifiedDate: "2023-08-25",
    regions: [
      { id: 3, name: 'East Region' },
      { id: 4, name: 'West Region' }
    ],
    rewards: [
      { id: 5, name: "Gift Voucher", quantity: 150 },
      { id: 6, name: "Fall Special", quantity: 300 }
    ]
  }
};
