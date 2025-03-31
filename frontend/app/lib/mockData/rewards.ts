import { TableFetchResponse, FieldType, SortType } from './interfaces';
import { mockRegionTable } from './regions';
import { mockProvinceTable } from './provinces';

// Mock reward table data
export const mockRewardTable: TableFetchResponse = {
  totalPages: 2,
  currentPage: 0,
  pageSize: 10,
  totalElements: 12,
  tableName: "rewards",
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
      fieldName: "value", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "Value",
      filterable: true
    },
    { 
      fieldName: "quantity", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "Quantity",
      filterable: false
    },
    { 
      fieldName: "claimed", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "Claimed",
      filterable: false
    },
    { 
      fieldName: "status", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Status",
      filterable: true
    },
    { 
      fieldName: "eventName", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Event",
      filterable: true
    }
  ],
  rows: [
    {
      data: { id: 1, name: "Gift Card", value: 50, quantity: 100, claimed: 35, status: "Available", eventId: 1, eventName: "Summer Giveaway" }
    },
    {
      data: { id: 2, name: "Free Product", value: 25, quantity: 200, claimed: 75, status: "Available", eventId: 1, eventName: "Summer Giveaway" }
    },
    {
      data: { id: 3, name: "Discount Coupon", value: 10, quantity: 500, claimed: 120, status: "Available", eventId: 2, eventName: "Fall Promotion" }
    },
    {
      data: { id: 4, name: "Premium Pass", value: 100, quantity: 10, claimed: 10, status: "Depleted", eventId: 1, eventName: "Summer Giveaway" }
    },
    {
      data: { id: 5, name: "Gift Voucher", value: 30, quantity: 150, claimed: 45, status: "Available", eventId: 2, eventName: "Fall Promotion" }
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
  relatedTables: {}, // Initialize empty - will be populated by other modules
  first: true,
  last: false,
  empty: false,
  numberOfElements: 5
};

// Function to add relationships to rewards
export function addRewardRelationship(
  rewardId: number,
  relationName: string,
  relationData: any
) {
  if (!mockRewardTable.relatedTables) {
    mockRewardTable.relatedTables = {};
  }
  
  if (!mockRewardTable.relatedTables[relationName]) {
    mockRewardTable.relatedTables[relationName] = {};
  }
  
  mockRewardTable.relatedTables[relationName][rewardId] = relationData;
}

// Create region-reward relationships
function initializeRewardRegionRelationships() {
  // Map rewards to regions
  const rewardRegionsMap: Record<string, number[]> = {
    "1": [1, 2], // Gift Card is available in North and South regions
    "2": [1, 3], // Free Product is available in North and East regions
    "3": [2, 4], // Discount Coupon is available in South and West regions
    "4": [3, 4], // Premium Pass is available in East and West regions
    "5": [1, 5]  // Gift Voucher is available in North and Central regions
  };
  
  // Set up reward -> regions relationships
  for (const [rewardId, regionIds] of Object.entries(rewardRegionsMap)) {
    const regionRows = regionIds.map(regionId => {
      return mockRegionTable.rows.find(row => row.data.id === regionId);
    }).filter(Boolean); // Filter out undefined
    
    if (regionRows.length > 0) {
      addRewardRelationship(Number(rewardId), "regions", {
        totalPages: 1,
        currentPage: 0,
        pageSize: 10,
        totalElements: regionRows.length,
        tableName: "reward_regions",
        rows: regionRows,
        first: true,
        last: true,
        empty: false,
        numberOfElements: regionRows.length
      });
    }
  }
  
  // Region -> rewards relationships would be handled in the regions.ts file
}

// Create province-reward relationships
function initializeRewardProvinceRelationships() {
  // Map rewards to provinces
  const rewardProvincesMap: Record<string, number[]> = {
    "1": [1, 3, 4], // Gift Card is available in provinces 1, 3, 4
    "2": [2, 3, 5], // Free Product is available in provinces 2, 3, 5
    "3": [6, 7, 8], // Discount Coupon is available in provinces 6, 7, 8
    "4": [9, 10],   // Premium Pass is available in provinces 9, 10
    "5": [1, 2, 3]  // Gift Voucher is available in provinces 1, 2, 3
  };
  
  // Set up reward -> provinces relationships
  for (const [rewardId, provinceIds] of Object.entries(rewardProvincesMap)) {
    const provinceRows = provinceIds.map(provinceId => {
      return mockProvinceTable.rows.find(row => row.data.id === provinceId);
    }).filter(Boolean); // Filter out undefined
    
    if (provinceRows.length > 0) {
      addRewardRelationship(Number(rewardId), "provinces", {
        totalPages: 1,
        currentPage: 0,
        pageSize: 10,
        totalElements: provinceRows.length,
        tableName: "reward_provinces",
        rows: provinceRows,
        first: true,
        last: true,
        empty: false,
        numberOfElements: provinceRows.length
      });
    }
  }
  
  // Province -> rewards relationships would be handled in the provinces.ts file
}

// Initialize winners and spin history relationships - these we'll keep since they're 
// part of the essential functionality but will be implemented differently
function initializeWinnersAndSpinHistoryRelationships() {
  // Set up winners relationships
  const rewardWinnersMap = {
    "1": [
      { 
        data: { 
          id: 1, 
          name: "John Smith", 
          email: "john.smith@example.com", 
          spinId: 101,
          timestamp: "2023-09-15 14:32:45",
          claimed: true,
          claimDate: "2023-09-15 15:22:17"
        }
      },
      { 
        data: { 
          id: 3, 
          name: "Bob Johnson", 
          email: "bob.j@example.com", 
          spinId: 103,
          timestamp: "2023-09-13 16:45:33",
          claimed: true,
          claimDate: "2023-09-13 17:12:05"
        }
      }
    ],
    "2": [
      { 
        data: { 
          id: 2, 
          name: "Jane Doe", 
          email: "jane.doe@example.com", 
          spinId: 102,
          timestamp: "2023-09-14 10:30:12",
          claimed: false,
          claimDate: null
        }
      },
      { 
        data: { 
          id: 4, 
          name: "Alice Williams", 
          email: "alice.w@example.com", 
          spinId: 104,
          timestamp: "2023-09-12 11:24:56",
          claimed: true,
          claimDate: "2023-09-12 16:05:23"
        }
      },
      { 
        data: { 
          id: 5, 
          name: "Charlie Brown", 
          email: "charlie.b@example.com", 
          spinId: 105, 
          timestamp: "2023-09-10 09:15:22",
          claimed: true,
          claimDate: "2023-09-10 14:37:42"
        }
      }
    ]
  };
  
  // Add winners to the relatedTables
  Object.entries(rewardWinnersMap).forEach(([rewardId, winners]) => {
    addRewardRelationship(Number(rewardId), "winners", {
      totalPages: 1,
      currentPage: 0,
      pageSize: 10,
      totalElements: winners.length,
      tableName: "reward_winners",
      rows: winners,
      first: true,
      last: true,
      empty: false,
      numberOfElements: winners.length
    });
  });
  
  // Set up spin history relationships
  const rewardSpinHistoryMap = {
    "1": [
      { 
        data: { 
          id: 1, 
          participantName: 'John Smith',
          participantEmail: 'john.smith@example.com',
          timestamp: '2023-09-15 14:32:45',
          eventName: 'Summer Giveaway',
          result: 'Gift Card ($50)',
          rewardId: 1,
          rewardName: 'Gift Card',
          isWinner: true,
          rewardClaimed: true
        }
      },
      { 
        data: { 
          id: 3, 
          participantName: 'Bob Johnson',
          participantEmail: 'bob.j@example.com',
          timestamp: '2023-09-13 16:45:33',
          eventName: 'Summer Giveaway',
          result: 'Gift Card ($50)',
          rewardId: 1,
          rewardName: 'Gift Card',
          isWinner: true,
          rewardClaimed: true
        }
      }
    ]
  };
  
  // Add spin history to the relatedTables
  Object.entries(rewardSpinHistoryMap).forEach(([rewardId, spinHistory]) => {
    addRewardRelationship(Number(rewardId), "spinHistory", {
      totalPages: 1,
      currentPage: 0,
      pageSize: 10,
      totalElements: spinHistory.length,
      tableName: "reward_spin_history",
      rows: spinHistory,
      first: true,
      last: true,
      empty: false,
      numberOfElements: spinHistory.length
    });
  });
}

// Initialize relationships when the module is loaded
function initializeRewardRelationships() {
  // Initialize region relationships
  initializeRewardRegionRelationships();
  
  // Initialize province relationships
  initializeRewardProvinceRelationships();
  
  // Initialize winners and spin history relationships
  initializeWinnersAndSpinHistoryRelationships();
}

// Call initializer
initializeRewardRelationships();

// Mock reward details
export const mockRewardDetails: Record<number, any> = {
  1: {
    id: 1,
    name: "Gift Card",
    description: "$50 gift card for online shopping",
    value: 50,
    quantity: 100,
    claimed: 35,
    available: 65,
    winRate: "10%",
    totalClaimed: 35,
    totalWinners: 35,
    currentEvent: {
      id: 1,
      name: "Summer Giveaway",
      startTime: "2023-06-01",
      endTime: "2023-08-31",
      status: "Active"
    }
  },
  2: {
    id: 2,
    name: "Free Product",
    description: "Free product sample",
    value: 25,
    quantity: 200,
    claimed: 75,
    available: 125,
    winRate: "20%",
    totalClaimed: 75,
    totalWinners: 75,
    currentEvent: {
      id: 1,
      name: "Summer Giveaway",
      startTime: "2023-06-01",
      endTime: "2023-08-31",
      status: "Active"
    }
  }
};
