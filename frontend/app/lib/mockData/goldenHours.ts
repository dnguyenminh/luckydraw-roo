import { TableFetchResponse, FieldType, SortType } from './interfaces';
import { mockRegionTable } from './regions';
import { mockProvinceTable } from './provinces';

// Mock golden hour table data
export const mockGoldenHourTable: TableFetchResponse = {
  totalPages: 2,
  currentPage: 0,
  pageSize: 10,
  totalElements: 12,
  tableName: "golden_hours",
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
      fieldName: "startTime", 
      fieldType: FieldType.TIME, 
      sortType: SortType.ASCENDING,
      displayName: "Start Time",
      filterable: true
    },
    { 
      fieldName: "endTime", 
      fieldType: FieldType.TIME, 
      sortType: SortType.ASCENDING,
      displayName: "End Time",
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
      fieldName: "multiplier", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "Multiplier",
      filterable: true
    },
    { 
      fieldName: "status", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Status",
      filterable: true
    }
  ],
  rows: [
    {
      data: { 
        id: 1, 
        name: "Evening Rush", 
        startTime: "18:00", 
        endTime: "19:00", 
        startDate: "2023-09-01",
        endDate: "2023-11-30",
        multiplier: 2,
        status: "Active"
      }
    },
    {
      data: { 
        id: 2, 
        name: "Lunch Break", 
        startTime: "12:00", 
        endTime: "13:00", 
        startDate: "2023-09-01",
        endDate: "2023-11-30",
        multiplier: 1.5,
        status: "Active"
      }
    },
    {
      data: { 
        id: 3, 
        name: "Morning Coffee", 
        startTime: "09:00", 
        endTime: "10:00", 
        startDate: "2023-09-01",
        endDate: "2023-11-30",
        multiplier: 1.5,
        status: "Active"
      }
    },
    {
      data: { 
        id: 4, 
        name: "Weekend Special", 
        startTime: "14:00", 
        endTime: "18:00", 
        startDate: "2023-09-02",
        endDate: "2023-11-26",
        multiplier: 3,
        status: "Scheduled"
      }
    },
    {
      data: { 
        id: 5, 
        name: "Happy Hour", 
        startTime: "17:00", 
        endTime: "20:00", 
        startDate: "2023-09-15",
        endDate: "2023-10-15",
        multiplier: 2.5,
        status: "Scheduled"
      }
    }
  ],
  originalRequest: {
    page: 0,
    size: 10,
    sorts: [{ field: "startTime", order: "asc" }],
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

// Function to add relationships to golden hours
export function addGoldenHourRelationship(
  goldenHourId: number,
  relationName: string,
  relationData: any
) {
  if (!mockGoldenHourTable.relatedTables) {
    mockGoldenHourTable.relatedTables = {};
  }
  
  if (!mockGoldenHourTable.relatedTables[relationName]) {
    mockGoldenHourTable.relatedTables[relationName] = {};
  }
  
  mockGoldenHourTable.relatedTables[relationName][goldenHourId] = relationData;
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
  
  // Set up golden hour -> regions relationships
  for (const [goldenHourId, regionIds] of Object.entries(goldenHourRegionsMap)) {
    const regionRows = regionIds.map(regionId => {
      return mockRegionTable.rows.find(row => row.data.id === regionId);
    }).filter(Boolean); // Filter out undefined
    
    if (regionRows.length > 0) {
      addGoldenHourRelationship(Number(goldenHourId), "regions", {
        totalPages: 1,
        currentPage: 0,
        pageSize: 10,
        totalElements: regionRows.length,
        tableName: "golden_hour_regions",
        rows: regionRows,
        first: true,
        last: true,
        empty: false,
        numberOfElements: regionRows.length
      });
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
    const provinceRows = provinceIds.map(provinceId => {
      return mockProvinceTable.rows.find(row => row.data.id === provinceId);
    }).filter(Boolean); // Filter out undefined
    
    if (provinceRows.length > 0) {
      addGoldenHourRelationship(Number(goldenHourId), "provinces", {
        totalPages: 1,
        currentPage: 0,
        pageSize: 10,
        totalElements: provinceRows.length,
        tableName: "golden_hour_provinces",
        rows: provinceRows,
        first: true,
        last: true,
        empty: false,
        numberOfElements: provinceRows.length
      });
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

// Mock golden hour details
export const mockGoldenHourDetails: Record<number, any> = {
  1: {
    id: 1,
    name: "Evening Rush",
    description: "Increased rewards during evening rush hour",
    startTime: "18:00",
    endTime: "19:00",
    startDate: "2023-09-01",
    endDate: "2023-11-30",
    multiplier: 2,
    totalParticipants: 3250,
    totalSpins: 8720,
    totalWinners: 2180,
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
    name: "Lunch Break",
    description: "Special promotion during lunch hours",
    startTime: "12:00",
    endTime: "13:00",
    startDate: "2023-09-01",
    endDate: "2023-11-30",
    multiplier: 1.5,
    totalParticipants: 2780,
    totalSpins: 6450,
    totalWinners: 1425,
    currentEvent: {
      id: 1,
      name: "Summer Giveaway",
      startTime: "2023-06-01",
      endTime: "2023-08-31",
      status: "Active"
    }
  }
};
