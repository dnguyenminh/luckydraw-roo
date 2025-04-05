import { 
  TableFetchResponse, 
  Column, 
  FieldType, 
  SortType, 
  ObjectType,
  RelatedLinkedObjectsMap,
  RelatedLinkedObject // Add this import
} from './interfaces';
import { mockRegionTable } from './regions';

// Define columns for the provinces table
const provinceColumns: Column[] = [
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
    fieldName: 'regionName', 
    fieldType: FieldType.STRING, 
    sortType: SortType.NONE, 
    displayName: 'Region', 
    filterable: true 
  },
  { 
    fieldName: 'population', 
    fieldType: FieldType.NUMBER, 
    sortType: SortType.NONE, 
    displayName: 'Population', 
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
    fieldName: 'status', 
    fieldType: FieldType.STRING, 
    sortType: SortType.NONE, 
    displayName: 'Status', 
    filterable: true 
  }
];

// Create related linked objects for provinces
const provinceRelatedObjects: RelatedLinkedObjectsMap = {
  // Participants related to provinces
  participants: {
    '1': [
      { objectType: ObjectType.PARTICIPANT, id: 1, name: 'John Doe' },
      { objectType: ObjectType.PARTICIPANT, id: 2, name: 'Jane Smith' }
    ],
    '2': [
      { objectType: ObjectType.PARTICIPANT, id: 3, name: 'Bob Johnson' },
      { objectType: ObjectType.PARTICIPANT, id: 4, name: 'Alice Brown' }
    ],
    '3': [
      { objectType: ObjectType.PARTICIPANT, id: 5, name: 'Charlie Davis' },
      { objectType: ObjectType.PARTICIPANT, id: 6, name: 'Eva Wilson' }
    ],
    '4': [
      { objectType: ObjectType.PARTICIPANT, id: 7, name: 'Frank Miller' },
      { objectType: ObjectType.PARTICIPANT, id: 8, name: 'Grace Taylor' }
    ],
    '5': [
      { objectType: ObjectType.PARTICIPANT, id: 9, name: 'Henry Clark' },
      { objectType: ObjectType.PARTICIPANT, id: 10, name: 'Ivy Martinez' }
    ],
    '6': [
      { objectType: ObjectType.PARTICIPANT, id: 11, name: 'Jack Robinson' },
      { objectType: ObjectType.PARTICIPANT, id: 12, name: 'Karen Lewis' }
    ],
    '7': [
      { objectType: ObjectType.PARTICIPANT, id: 13, name: 'Leo Walker' },
      { objectType: ObjectType.PARTICIPANT, id: 14, name: 'Mia Allen' }
    ],
    '8': [
      { objectType: ObjectType.PARTICIPANT, id: 15, name: 'Nathan Young' },
      { objectType: ObjectType.PARTICIPANT, id: 16, name: 'Olivia King' }
    ]
  }
};

// Create mock province data
export const mockProvinceTable: TableFetchResponse = {
  totalPages: 1,
  currentPage: 0,
  pageSize: 10,
  totalElements: 8,
  tableName: 'provinces',
  columns: provinceColumns,
  rows: [
    {
      data: {
        id: 1,
        name: 'Province A',
        regionName: 'North',
        population: 500000,
        participantCount: 480,
        status: 'Active'
      }
    },
    {
      data: {
        id: 2,
        name: 'Province B',
        regionName: 'North',
        population: 750000,
        participantCount: 470,
        status: 'Active'
      }
    },
    {
      data: {
        id: 3,
        name: 'Province C',
        regionName: 'South',
        population: 600000,
        participantCount: 390,
        status: 'Active'
      }
    },
    {
      data: {
        id: 4,
        name: 'Province D',
        regionName: 'South',
        population: 450000,
        participantCount: 390,
        status: 'Active'
      }
    },
    {
      data: {
        id: 5,
        name: 'Province E',
        regionName: 'East',
        population: 850000,
        participantCount: 420,
        status: 'Active'
      }
    },
    {
      data: {
        id: 6,
        name: 'Province F',
        regionName: 'East',
        population: 550000,
        participantCount: 400,
        status: 'Active'
      }
    },
    {
      data: {
        id: 7,
        name: 'Province G',
        regionName: 'West',
        population: 720000,
        participantCount: 380,
        status: 'Active'
      }
    },
    {
      data: {
        id: 8,
        name: 'Province H',
        regionName: 'West',
        population: 480000,
        participantCount: 370,
        status: 'Active'
      }
    }
  ],
  relatedLinkedObjects: provinceRelatedObjects,
  first: true,
  last: true,
  empty: false,
  numberOfElements: 8,
  originalRequest: {
    page: 0,
    size: 10,
    sorts: [],
    filters: [],
    search: {},
    objectType: ObjectType.PROVINCE
  },
  statistics: {
    totalProvinces: 8,
    activeProvinces: 8,
    totalParticipants: 3300,
    activeParticipants: 3200
  }
};

// Create mock province details
export const mockProvinceDetails: Record<number, any> = {
  1: {
    id: 1,
    name: 'Province A',
    regionName: 'North',
    regionId: 1,
    description: 'A northern province with significant industrial development',
    created: '2023-01-15T08:30:00Z',
    lastModified: '2023-02-20T13:45:00Z',
    population: 500000,
    totalParticipants: 480,
    activeParticipants: 470
  },
  2: {
    id: 2,
    name: 'Province B',
    regionName: 'North',
    regionId: 1,
    description: 'A northern province with rich agricultural resources',
    created: '2023-01-15T09:15:00Z',
    lastModified: '2023-02-20T14:30:00Z',
    population: 750000,
    totalParticipants: 470,
    activeParticipants: 460
  },
  3: {
    id: 3,
    name: 'Province C',
    regionName: 'South',
    regionId: 2,
    description: 'A southern coastal province with tourism focus',
    created: '2023-01-15T10:00:00Z',
    lastModified: '2023-02-20T15:15:00Z',
    population: 600000,
    totalParticipants: 390,
    activeParticipants: 380
  },
  4: {
    id: 4,
    name: 'Province D',
    regionName: 'South',
    regionId: 2,
    description: 'A southern province known for its natural reserves',
    created: '2023-01-15T10:45:00Z',
    lastModified: '2023-02-20T16:00:00Z',
    population: 450000,
    totalParticipants: 390,
    activeParticipants: 385
  },
  5: {
    id: 5,
    name: 'Province E',
    regionName: 'East',
    regionId: 3,
    description: 'An eastern province with technological development',
    created: '2023-01-15T11:30:00Z',
    lastModified: '2023-02-20T16:45:00Z',
    population: 850000,
    totalParticipants: 420,
    activeParticipants: 410
  },
  6: {
    id: 6,
    name: 'Province F',
    regionName: 'East',
    regionId: 3,
    description: 'An eastern province with historical significance',
    created: '2023-01-15T12:15:00Z',
    lastModified: '2023-02-20T17:30:00Z',
    population: 550000,
    totalParticipants: 400,
    activeParticipants: 390
  },
  7: {
    id: 7,
    name: 'Province G',
    regionName: 'West',
    regionId: 4,
    description: 'A western province with mountainous terrain',
    created: '2023-01-15T13:00:00Z',
    lastModified: '2023-02-20T18:15:00Z',
    population: 720000,
    totalParticipants: 380,
    activeParticipants: 370
  },
  8: {
    id: 8,
    name: 'Province H',
    regionName: 'West',
    regionId: 4,
    description: 'A western province with diverse cultural heritage',
    created: '2023-01-15T13:45:00Z',
    lastModified: '2023-02-20T19:00:00Z',
    population: 480000,
    totalParticipants: 370,
    activeParticipants: 360
  }
};

// Function to add a province relationship
export function addProvinceRelationship(
  provinceId: number, 
  relationName: string, 
  relationData: any
) {
  if (!mockProvinceTable.relatedLinkedObjects) {
    mockProvinceTable.relatedLinkedObjects = {};
  }
  
  if (!mockProvinceTable.relatedLinkedObjects[relationName]) {
    mockProvinceTable.relatedLinkedObjects[relationName] = {};
  }
  
  mockProvinceTable.relatedLinkedObjects[relationName][provinceId] = relationData;
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
      if (!mockProvinceTable.relatedLinkedObjects) mockProvinceTable.relatedLinkedObjects = {};
      if (!mockProvinceTable.relatedLinkedObjects.region) mockProvinceTable.relatedLinkedObjects.region = {};
      
      // Create proper RelatedLinkedObject for region
      mockProvinceTable.relatedLinkedObjects.region[provinceData.id] = [{
        objectType: ObjectType.REGION,
        id: regionData.data.id,
        name: regionData.data.name || `Region ${regionData.data.id}`
      }];
    }
  }
  
  // Setup region -> provinces relationships in region table
  if (mockRegionTable.relatedLinkedObjects) {
    for (const [regionId, provinceIds] of Object.entries(regionProvinceMap)) {
      // Get all provinces for this region - FIX BOTH TYPE ERRORS
      const provinceObjects = provinceIds
        .map(provinceId => {
          const provinceRow = mockProvinceTable.rows.find(row => row.data.id === provinceId);
          if (provinceRow) {
            // Create a province object that extends RelatedLinkedObject
            const provinceObj: RelatedLinkedObject = {
              objectType: ObjectType.PROVINCE,
              id: provinceId,
              name: provinceRow.data.name || `Province ${provinceId}`
            };
            
            // Add population as an additional property without causing type errors
            (provinceObj as any).population = provinceRow.data.population;
            
            return provinceObj;
          }
          return null;
        })
        .filter((obj): obj is RelatedLinkedObject => obj !== null);
      
      if (provinceObjects.length > 0) {
        // Add these provinces to the region's related objects as an array
        if (!mockRegionTable.relatedLinkedObjects.provinces) {
          mockRegionTable.relatedLinkedObjects.provinces = {};
        }
        
        mockRegionTable.relatedLinkedObjects.provinces[regionId] = provinceObjects;
      }
    }
  }
}

// Initialize the bidirectional relationships
initializeProvinceRegionRelationships();
