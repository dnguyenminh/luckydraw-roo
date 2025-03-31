import { TableFetchResponse, FieldType, SortType } from './interfaces';
import { mockRegionTable } from './regions';

// Mock province table data
export const mockProvinceTable: TableFetchResponse = {
  totalPages: 5,
  currentPage: 0,
  pageSize: 10,
  totalElements: 48,
  tableName: "provinces",
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
      displayName: "Province Name",
      filterable: true
    },
    { 
      fieldName: "regionName", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Region",
      filterable: true
    },
    { 
      fieldName: "population", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "Population",
      filterable: false
    },
    { 
      fieldName: "participantCount", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "Participants",
      filterable: false
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
      data: { id: 1, name: 'Western Province', regionName: 'West Region', regionId: 4, population: 3500000, participantCount: 2150, status: 'Active' }
    },
    {
      data: { id: 2, name: 'Eastern Province', regionName: 'East Region', regionId: 3, population: 2800000, participantCount: 1870, status: 'Active' }
    },
    {
      data: { id: 3, name: 'Northern Province', regionName: 'North Region', regionId: 1, population: 2100000, participantCount: 1620, status: 'Active' }
    },
    {
      data: { id: 4, name: 'Southern Province', regionName: 'South Region', regionId: 2, population: 2400000, participantCount: 1950, status: 'Active' }
    },
    {
      data: { id: 5, name: 'Central Province', regionName: 'Central Region', regionId: 5, population: 1900000, participantCount: 1450, status: 'Active' }
    },
    {
      data: { id: 6, name: 'Northwestern Province', regionName: 'North Region', regionId: 1, population: 1750000, participantCount: 1350, status: 'Active' }
    },
    {
      data: { id: 7, name: 'Northeastern Province', regionName: 'East Region', regionId: 3, population: 1600000, participantCount: 1280, status: 'Active' }
    },
    {
      data: { id: 8, name: 'Southwestern Province', regionName: 'South Region', regionId: 2, population: 1450000, participantCount: 1150, status: 'Active' }
    },
    {
      data: { id: 9, name: 'Southeastern Province', regionName: 'East Region', regionId: 3, population: 1350000, participantCount: 920, status: 'Active' }
    },
    {
      data: { id: 10, name: 'Coastal Province', regionName: 'West Region', regionId: 4, population: 1250000, participantCount: 850, status: 'Active' }
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
  relatedTables: {},
  first: true,
  last: false,
  empty: false,
  numberOfElements: 10
};

// Mock province details
export const mockProvinceDetails: Record<number, any> = {
  1: {
    id: 1,
    name: 'Western Province',
    regionName: 'West Region',
    regionId: 4,
    description: 'The western coastal province',
    created: '2023-01-10',
    lastModified: '2023-03-05',
    population: 3500000,
    totalParticipants: 2150,
    activeParticipants: 1850
  },
  2: {
    id: 2,
    name: 'Eastern Province',
    regionName: 'East Region',
    regionId: 3,
    description: 'The eastern coastal province',
    created: '2023-01-10',
    lastModified: '2023-02-28',
    population: 2800000,
    totalParticipants: 1870,
    activeParticipants: 1640
  }
};

// Function to add a province relationship
export function addProvinceRelationship(
  provinceId: number, 
  relationName: string, 
  relationData: any
) {
  if (!mockProvinceTable.relatedTables) {
    mockProvinceTable.relatedTables = {};
  }
  
  if (!mockProvinceTable.relatedTables[relationName]) {
    mockProvinceTable.relatedTables[relationName] = {};
  }
  
  mockProvinceTable.relatedTables[relationName][provinceId] = relationData;
}

// Create province-region relationships
function initializeProvinceRegionRelationships() {
  // Map regions to their provinces
  const regionProvinceMap: Record<string, number[]> = {
    "1": [3, 6],        // North Region has provinces 3, 6
    "2": [4, 8],        // South Region has provinces 4, 8
    "3": [2, 7, 9],     // East Region has provinces 2, 7, 9
    "4": [1, 10],       // West Region has provinces 1, 10
    "5": [5]            // Central Region has province 5
  };

  // Setup province -> region relationships
  for (const province of mockProvinceTable.rows) {
    const provinceData = province.data;
    const regionId = provinceData.regionId;
    
    // Find the region in mockRegionTable
    const regionData = mockRegionTable.rows.find(r => r.data.id === regionId);
    if (regionData) {
      // Add region as a related entry to this province
      if (!mockProvinceTable.relatedTables) mockProvinceTable.relatedTables = {};
      if (!mockProvinceTable.relatedTables.region) mockProvinceTable.relatedTables.region = {};
      
      mockProvinceTable.relatedTables.region[provinceData.id] = {
        totalPages: 1,
        currentPage: 0,
        pageSize: 1,
        totalElements: 1,
        tableName: "province_region",
        rows: [regionData],
        first: true,
        last: true,
        empty: false,
        numberOfElements: 1
      };
    }
  }
  
  // Setup region -> provinces relationships in region table
  if (mockRegionTable.relatedTables) {
    for (const [regionId, provinceIds] of Object.entries(regionProvinceMap)) {
      // Get all provinces for this region
      const provinceRows = provinceIds.map(id => 
        mockProvinceTable.rows.find(row => row.data.id === id)
      ).filter(Boolean); // Filter out undefined entries
      
      if (provinceRows.length > 0) {
        // Add these provinces to the region's related tables
        if (!mockRegionTable.relatedTables.provinces) {
          mockRegionTable.relatedTables.provinces = {};
        }
        
        mockRegionTable.relatedTables.provinces[regionId] = {
          totalPages: 1,
          currentPage: 0,
          pageSize: 10,
          totalElements: provinceRows.length,
          tableName: "region_provinces",
          rows: provinceRows,
          first: true,
          last: true,
          empty: false,
          numberOfElements: provinceRows.length
        };
      }
    }
  }
}

// Initialize the bidirectional relationships
initializeProvinceRegionRelationships();
