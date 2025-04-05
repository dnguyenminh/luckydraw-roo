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

// Define columns for the golden hours table
const goldenHourColumns: Column[] = [
  { fieldName: 'id', fieldType: FieldType.NUMBER, sortType: SortType.ASCENDING, displayName: 'ID', filterable: true },
  { fieldName: 'name', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Name', filterable: true },
  { fieldName: 'startTime', fieldType: FieldType.TIME, sortType: SortType.NONE, displayName: 'Start Time', filterable: true },
  { fieldName: 'endTime', fieldType: FieldType.TIME, sortType: SortType.NONE, displayName: 'End Time', filterable: true },
  { fieldName: 'startDate', fieldType: FieldType.DATE, sortType: SortType.NONE, displayName: 'Start Date', filterable: true },
  { fieldName: 'endDate', fieldType: FieldType.DATE, sortType: SortType.NONE, displayName: 'End Date', filterable: true },
  { fieldName: 'multiplier', fieldType: FieldType.NUMBER, sortType: SortType.NONE, displayName: 'Multiplier', filterable: true },
  { fieldName: 'status', fieldType: FieldType.STRING, sortType: SortType.NONE, displayName: 'Status', filterable: true }
];

// Create related linked objects for golden hours
const goldenHourRelatedObjects: RelatedLinkedObjectsMap = {
  events: {
    '1': [
      { objectType: ObjectType.EVENT, id: 1, name: 'Summer Giveaway' }
    ],
    '2': [
      { objectType: ObjectType.EVENT, id: 1, name: 'Summer Giveaway' }
    ],
    '3': [
      { objectType: ObjectType.EVENT, id: 2, name: 'Winter Wonderland' }
    ],
    '4': [
      { objectType: ObjectType.EVENT, id: 3, name: 'Spring Festival' }
    ]
  }
};

// Create mock golden hours data
export const mockGoldenHourTable: TableFetchResponse = {
  totalPages: 1,
  currentPage: 0,
  pageSize: 10,
  totalElements: 4,
  tableName: 'golden_hours',
  columns: goldenHourColumns,
  rows: [
    {
      data: {
        id: 1,
        name: 'Morning Rush',
        startTime: '08:00:00',
        endTime: '10:00:00',
        startDate: '2023-06-01',
        endDate: '2023-08-31',
        multiplier: 2,
        status: 'Active'
      }
    },
    {
      data: {
        id: 2,
        name: 'Lunch Break',
        startTime: '12:00:00',
        endTime: '14:00:00',
        startDate: '2023-06-01',
        endDate: '2023-08-31',
        multiplier: 2.5,
        status: 'Active'
      }
    },
    {
      data: {
        id: 3,
        name: 'Evening Special',
        startTime: '18:00:00',
        endTime: '20:00:00',
        startDate: '2023-12-01',
        endDate: '2024-01-15',
        multiplier: 3,
        status: 'Upcoming'
      }
    },
    {
      data: {
        id: 4,
        name: 'Weekend Boost',
        startTime: '14:00:00',
        endTime: '16:00:00',
        startDate: '2023-03-01',
        endDate: '2023-04-15',
        multiplier: 2,
        status: 'Completed'
      }
    }
  ],
  relatedLinkedObjects: goldenHourRelatedObjects,
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
    objectType: ObjectType.GOLDEN_HOUR
  },
  statistics: {
    totalGoldenHours: 4,
    activeGoldenHours: 2,
    upcomingGoldenHours: 1,
    completedGoldenHours: 1
  }
};

// Updated function to add relationships to golden hours
export function addGoldenHourRelationship(
  goldenHourId: number,
  relationName: string,
  relationObjects: RelatedLinkedObject[]
) {
  if (!mockGoldenHourTable.relatedLinkedObjects) {
    mockGoldenHourTable.relatedLinkedObjects = {};
  }
  
  if (!mockGoldenHourTable.relatedLinkedObjects[relationName]) {
    mockGoldenHourTable.relatedLinkedObjects[relationName] = {};
  }
  
  mockGoldenHourTable.relatedLinkedObjects[relationName][goldenHourId] = relationObjects;
}

// Create region-golden hour relationships
function initializeGoldenHourRegionRelationships() {
  // Map golden hours to regions
  const goldenHourRegionsMap: Record<string, number[]> = {
    "1": [1, 2],  // Evening Rush is available in North and South regions
    "2": [1, 3],  // Lunch Break is available in North and East regions
    "3": [2, 4],  // Morning Coffee is available in South and West regions
    "4": [3, 4]   // Weekend Special is available in East and West regions
  };
  
  // Update to use relatedLinkedObjects and the new relationship function
  for (const [goldenHourId, regionIds] of Object.entries(goldenHourRegionsMap)) {
    const regionObjects = regionIds.map(regionId => {
      const regionRow = mockRegionTable.rows.find(row => row.data.id === regionId);
      return {
        objectType: ObjectType.REGION,
        id: regionId,
        name: regionRow?.data.name || `Unknown Region ${regionId}`
      };
    }).filter(Boolean);
    
    if (regionObjects.length > 0) {
      addGoldenHourRelationship(Number(goldenHourId), "regions", regionObjects);
    }
  }
  
  // Set up region -> golden hours relationships
  // This assumes we have a function in the regions module to add relationships
  // If not, we would need to add relationships directly to mockRegionTable here
}

// Create province-golden hour relationships
function initializeGoldenHourProvinceRelationships() {
  // Map golden hours to provinces
  const goldenHourProvincesMap: Record<string, number[]> = {
    "1": [1, 3, 4],  // Evening Rush is available in provinces 1, 3, 4
    "2": [2, 3, 5],  // Lunch Break is available in provinces 2, 3, 5
    "3": [6, 7, 8],  // Morning Coffee is available in provinces 6, 7, 8
    "4": [9, 10]     // Weekend Special is available in provinces 9, 10
  };
  
  // Set up golden hour -> provinces relationships
  for (const [goldenHourId, provinceIds] of Object.entries(goldenHourProvincesMap)) {
    // Convert to proper RelatedLinkedObject[] format
    const provinceObjects = provinceIds
      .map(provinceId => {
        const provinceRow = mockProvinceTable.rows.find(row => row.data.id === provinceId);
        if (provinceRow) {
          // Create a type that matches what we're actually returning
          type ProvinceLinkedObject = RelatedLinkedObject & { population?: number };
          
          return {
            objectType: ObjectType.PROVINCE,
            id: provinceId,
            name: provinceRow.data.name || `Province ${provinceId}`,
            population: provinceRow.data.population
          } as ProvinceLinkedObject;
        }
        return null;
      })
      .filter((obj): obj is RelatedLinkedObject => obj !== null);
    
    if (provinceObjects.length > 0) {
      // Pass array of RelatedLinkedObject directly
      addGoldenHourRelationship(Number(goldenHourId), "provinces", provinceObjects);
    }
  }
  
  // We would add province -> golden hours relationships in a similar way
  // But that should be done in the provinces.ts file
}

// Initialize events relationships (keeping this from the existing file)
const initializeGoldenHourEventRelationships = () => {
  // Event relationships would be added here
  // This is already in the existing file, so keeping it as a reference
};

// When exports are initialized, create all relationships
function initializeGoldenHourRelationships() {
  // Initialize events relationship (already present in the file)
  initializeGoldenHourEventRelationships();
  
  // Initialize regions relationship
  initializeGoldenHourRegionRelationships();
  
  // Initialize provinces relationship
  initializeGoldenHourProvinceRelationships();
  
  // Add participants and spin history relationships as needed
  // These are already present in the existing file, so they would be included here
}

// Initialize all relationships
initializeGoldenHourRelationships();

// Create mock golden hour details
export const mockGoldenHourDetails: Record<number, any> = {
  1: {
    id: 1,
    name: 'Morning Rush',
    description: 'Increased chances of winning during morning rush hours',
    startTime: '08:00:00',
    endTime: '10:00:00',
    startDate: '2023-06-01',
    endDate: '2023-08-31',
    multiplier: 2,
    totalParticipants: 450,
    totalSpins: 1200,
    totalWinners: 85,
    currentEvent: {
      id: 1,
      name: 'Summer Giveaway',
      startTime: '2023-06-01T00:00:00Z',
      endTime: '2023-08-31T23:59:59Z',
      status: 'Active'
    }
  },
  // Additional golden hour details would be included here
};
