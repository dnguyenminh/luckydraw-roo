import { 
  TableFetchResponse, 
  Column, 
  FieldType, 
  SortType, 
  ObjectType,
  RelatedLinkedObjectsMap,
  RelatedLinkedObject
} from './interfaces';
import { mockRegionTable } from './regions';
import { mockProvinceTable } from './provinces';

// Define columns for the rewards table
const rewardColumns: Column[] = [
  { fieldName: 'id', fieldType: FieldType.NUMBER, sortType: SortType.ASCENDING, displayName: 'ID', filterable: true },
  { fieldName: 'name', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Name', filterable: true },
  { fieldName: 'value', fieldType: FieldType.NUMBER, sortType: SortType.NONE, displayName: 'Value', filterable: true },
  { fieldName: 'quantity', fieldType: FieldType.NUMBER, sortType: SortType.NONE, displayName: 'Quantity', filterable: false },
  { fieldName: 'claimed', fieldType: FieldType.NUMBER, sortType: SortType.NONE, displayName: 'Claimed', filterable: false },
  { fieldName: 'status', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Status', filterable: true },
  { fieldName: 'eventName', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Event', filterable: true }
];

// Create related linked objects for rewards
const rewardRelatedObjects: RelatedLinkedObjectsMap = {
  // Winners (participants) of each reward
  winners: {
    '1': [ // Gold Medal winners
      { objectType: ObjectType.PARTICIPANT, id: 1, name: 'John Doe' },
      { objectType: ObjectType.PARTICIPANT, id: 7, name: 'Frank Miller' }
    ],
    '2': [ // Silver Medal winners
      { objectType: ObjectType.PARTICIPANT, id: 2, name: 'Jane Smith' },
      { objectType: ObjectType.PARTICIPANT, id: 4, name: 'Alice Brown' },
      { objectType: ObjectType.PARTICIPANT, id: 8, name: 'Grace Taylor' }
    ],
    '5': [ // Cash Prize winners
      { objectType: ObjectType.PARTICIPANT, id: 10, name: 'Ivy Martinez' }
    ],
    '6': [ // Gift Card winners
      { objectType: ObjectType.PARTICIPANT, id: 4, name: 'Alice Brown' },
      { objectType: ObjectType.PARTICIPANT, id: 7, name: 'Frank Miller' },
      { objectType: ObjectType.PARTICIPANT, id: 10, name: 'Ivy Martinez' }
    ]
  },
  // Events associated with each reward
  events: {
    '1': [ // Gold Medal events
      { objectType: ObjectType.EVENT, id: 1, name: 'Summer Giveaway' }
    ],
    '2': [ // Silver Medal events
      { objectType: ObjectType.EVENT, id: 1, name: 'Summer Giveaway' }
    ],
    '3': [ // Bronze Medal events
      { objectType: ObjectType.EVENT, id: 2, name: 'Winter Wonderland' }
    ],
    '4': [ // Trophy events
      { objectType: ObjectType.EVENT, id: 2, name: 'Winter Wonderland' }
    ],
    '5': [ // Cash Prize events
      { objectType: ObjectType.EVENT, id: 3, name: 'Spring Festival' }
    ],
    '6': [ // Gift Card events
      { objectType: ObjectType.EVENT, id: 3, name: 'Spring Festival' }
    ]
  }
};

// Create mock reward data
export const mockRewardTable: TableFetchResponse = {
  totalPages: 1,
  currentPage: 0,
  pageSize: 10,
  totalElements: 6,
  tableName: 'rewards',
  columns: rewardColumns,
  rows: [
    {
      data: {
        id: 1,
        name: 'Gold Medal',
        value: 1000,
        quantity: 5,
        claimed: 2,
        status: 'Active',
        eventName: 'Summer Giveaway'
      }
    },
    {
      data: {
        id: 2,
        name: 'Silver Medal',
        value: 500,
        quantity: 10,
        claimed: 3,
        status: 'Active',
        eventName: 'Summer Giveaway'
      }
    },
    {
      data: {
        id: 3,
        name: 'Bronze Medal',
        value: 200,
        quantity: 20,
        claimed: 0,
        status: 'Inactive',
        eventName: 'Winter Wonderland'
      }
    },
    {
      data: {
        id: 4,
        name: 'Trophy',
        value: 800,
        quantity: 3,
        claimed: 0,
        status: 'Inactive',
        eventName: 'Winter Wonderland'
      }
    },
    {
      data: {
        id: 5,
        name: 'Cash Prize',
        value: 1500,
        quantity: 5,
        claimed: 1,
        status: 'Inactive',
        eventName: 'Spring Festival'
      }
    },
    {
      data: {
        id: 6,
        name: 'Gift Card',
        value: 300,
        quantity: 15,
        claimed: 3,
        status: 'Inactive',
        eventName: 'Spring Festival'
      }
    }
  ],
  relatedLinkedObjects: rewardRelatedObjects,
  first: true,
  last: true,
  empty: false,
  numberOfElements: 6,
  originalRequest: {
    page: 0,
    size: 10,
    sorts: [],
    filters: [],
    search: {},
    objectType: ObjectType.REWARD
  },
  statistics: {
    totalRewards: 6,
    activeRewards: 2,
    totalQuantity: 58,
    claimedQuantity: 9
  }
};

// Updated function to add relationships to rewards
export function addRewardRelationship(
  rewardId: number,
  relationName: string,
  relationObjects: RelatedLinkedObject[]
) {
  if (!mockRewardTable.relatedLinkedObjects) {
    mockRewardTable.relatedLinkedObjects = {};
  }
  
  if (!mockRewardTable.relatedLinkedObjects[relationName]) {
    mockRewardTable.relatedLinkedObjects[relationName] = {};
  }
  
  mockRewardTable.relatedLinkedObjects[relationName][rewardId] = relationObjects;
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
    const regionObjects = regionIds.map(regionId => {
      const regionRow = mockRegionTable.rows.find(row => row.data.id === regionId);
      return {
        objectType: ObjectType.REGION,
        id: regionId,
        name: regionRow?.data.name || `Unknown Region ${regionId}`
      };
    }).filter(Boolean);
    
    if (regionObjects.length > 0) {
      addRewardRelationship(Number(rewardId), "regions", regionObjects);
    }
  }
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
    // FIX: Convert to proper RelatedLinkedObject[] format
    const provinceObjects = provinceIds.map(provinceId => {
      const provinceRow = mockProvinceTable.rows.find(row => row.data.id === provinceId);
      return {
        objectType: ObjectType.PROVINCE,
        id: provinceId,
        name: provinceRow?.data.name || `Province ${provinceId}`
      };
    }).filter(Boolean);
    
    if (provinceObjects.length > 0) {
      // Pass array of RelatedLinkedObject directly
      addRewardRelationship(Number(rewardId), "provinces", provinceObjects);
    }
  }
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
  
  // FIX: Convert winners to RelatedLinkedObject[] format
  Object.entries(rewardWinnersMap).forEach(([rewardId, winners]) => {
    const winnerObjects = winners.map(winner => ({
      objectType: ObjectType.PARTICIPANT,
      id: winner.data.id,
      name: winner.data.name,
      email: winner.data.email,
      spinId: winner.data.spinId,
      timestamp: winner.data.timestamp,
      claimed: winner.data.claimed,
      claimDate: winner.data.claimDate
    }));
    
    // Pass array of RelatedLinkedObject directly
    addRewardRelationship(Number(rewardId), "winners", winnerObjects);
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
  
  // FIX: Convert spin history to RelatedLinkedObject[] format with required name property
  Object.entries(rewardSpinHistoryMap).forEach(([rewardId, spinHistory]) => {
    const spinHistoryObjects = spinHistory.map(spin => ({
      objectType: ObjectType.SPIN_HISTORY,
      id: spin.data.id,
      name: `Spin ${spin.data.id} - ${spin.data.participantName}`, // Add required name property
      participantName: spin.data.participantName,
      participantEmail: spin.data.participantEmail,
      timestamp: spin.data.timestamp,
      eventName: spin.data.eventName,
      result: spin.data.result,
      rewardId: spin.data.rewardId,
      rewardName: spin.data.rewardName,
      isWinner: spin.data.isWinner,
      rewardClaimed: spin.data.rewardClaimed
    }));
    
    // Pass array of RelatedLinkedObject directly
    addRewardRelationship(Number(rewardId), "spinHistory", spinHistoryObjects);
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

// Create mock reward details
export const mockRewardDetails: Record<number, any> = {
  1: {
    id: 1,
    name: 'Gold Medal',
    description: 'Premium gold-plated medal for top winners',
    value: 1000,
    quantity: 5,
    claimed: 2,
    available: 3,
    winRate: '5%',
    totalClaimed: 2,
    totalWinners: 2,
    currentEvent: {
      id: 1,
      name: 'Summer Giveaway',
      startTime: '2023-06-01T00:00:00Z',
      endTime: '2023-08-31T23:59:59Z',
      status: 'Active'
    }
  },
  2: {
    id: 2,
    name: 'Silver Medal',
    description: 'Silver-plated medal for second-tier winners',
    value: 500,
    quantity: 10,
    claimed: 3,
    available: 7,
    winRate: '10%',
    totalClaimed: 3,
    totalWinners: 3,
    currentEvent: {
      id: 1,
      name: 'Summer Giveaway',
      startTime: '2023-06-01T00:00:00Z',
      endTime: '2023-08-31T23:59:59Z',
      status: 'Active'
    }
  },
  // Additional reward details for the other rewards would be included here
};
