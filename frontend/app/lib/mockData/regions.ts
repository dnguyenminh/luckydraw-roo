import { 
  TableFetchResponse, 
  Column, 
  FieldType, 
  SortType, 
  ObjectType,
  RelatedLinkedObjectsMap,
  RelatedLinkedObject
} from './interfaces';
import { mockEventTable, addEventRelationship } from './events';

// Define columns for the regions table
const regionColumns: Column[] = [
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
    fieldName: 'code', 
    fieldType: FieldType.STRING, 
    sortType: SortType.NONE, 
    displayName: 'Code', 
    filterable: true 
  },
  { 
    fieldName: 'provinceCount', 
    fieldType: FieldType.NUMBER, 
    sortType: SortType.NONE, 
    displayName: 'Provinces', 
    filterable: false 
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

// Create related linked objects for regions
const regionRelatedObjects: RelatedLinkedObjectsMap = {
  // Provinces related to regions
  provinces: {
    '1': [
      { objectType: ObjectType.PROVINCE, id: 1, name: 'Province A', population: 500000 },
      { objectType: ObjectType.PROVINCE, id: 2, name: 'Province B', population: 750000 }
    ],
    '2': [
      { objectType: ObjectType.PROVINCE, id: 3, name: 'Province C', population: 600000 },
      { objectType: ObjectType.PROVINCE, id: 4, name: 'Province D', population: 450000 }
    ],
    '3': [
      { objectType: ObjectType.PROVINCE, id: 5, name: 'Province E', population: 850000 },
      { objectType: ObjectType.PROVINCE, id: 6, name: 'Province F', population: 550000 }
    ],
    '4': [
      { objectType: ObjectType.PROVINCE, id: 7, name: 'Province G', population: 720000 },
      { objectType: ObjectType.PROVINCE, id: 8, name: 'Province H', population: 480000 }
    ]
  },
  // Events related to regions
  events: {
    '1': [
      { objectType: ObjectType.EVENT, id: 1, name: 'Summer Giveaway' },
      { objectType: ObjectType.EVENT, id: 3, name: 'Spring Festival' }
    ],
    '2': [
      { objectType: ObjectType.EVENT, id: 1, name: 'Summer Giveaway' }
    ],
    '3': [
      { objectType: ObjectType.EVENT, id: 2, name: 'Winter Wonderland' }
    ],
    '4': [
      { objectType: ObjectType.EVENT, id: 2, name: 'Winter Wonderland' },
      { objectType: ObjectType.EVENT, id: 3, name: 'Spring Festival' }
    ]
  }
};

// Create mock region data
export const mockRegionTable: TableFetchResponse = {
  totalPages: 1,
  currentPage: 0,
  pageSize: 10,
  totalElements: 4,
  tableName: 'regions',
  columns: regionColumns,
  rows: [
    {
      data: {
        id: 1,
        name: 'North',
        code: 'N',
        provinceCount: 2,
        participantCount: 950,
        status: 'Active'
      }
    },
    {
      data: {
        id: 2,
        name: 'South',
        code: 'S',
        provinceCount: 2,
        participantCount: 780,
        status: 'Active'
      }
    },
    {
      data: {
        id: 3,
        name: 'East',
        code: 'E',
        provinceCount: 2,
        participantCount: 820,
        status: 'Active'
      }
    },
    {
      data: {
        id: 4,
        name: 'West',
        code: 'W',
        provinceCount: 2,
        participantCount: 750,
        status: 'Active'
      }
    }
  ],
  relatedLinkedObjects: regionRelatedObjects,
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
    objectType: ObjectType.REGION
  },
  statistics: {
    totalRegions: 4,
    activeRegions: 4,
    totalProvinces: 8,
    activeProvinces: 8
  }
};

// Create mock region details
export const mockRegionDetails: Record<number, any> = {
  1: {
    id: 1,
    name: 'North',
    code: 'N',
    description: 'Northern region of the country with 2 major provinces',
    created: '2023-01-10T08:00:00Z',
    lastModified: '2023-02-15T14:30:00Z',
    totalProvinces: 2,
    activeProvinces: 2,
    totalParticipants: 950,
    activeParticipants: 920,
    totalEvents: 2
  },
  2: {
    id: 2,
    name: 'South',
    code: 'S',
    description: 'Southern region of the country with 2 major provinces',
    created: '2023-01-10T09:15:00Z',
    lastModified: '2023-02-15T15:45:00Z',
    totalProvinces: 2,
    activeProvinces: 2,
    totalParticipants: 780,
    activeParticipants: 765,
    totalEvents: 1
  },
  3: {
    id: 3,
    name: 'East',
    code: 'E',
    description: 'Eastern region of the country with 2 major provinces',
    created: '2023-01-10T10:30:00Z',
    lastModified: '2023-02-15T16:20:00Z',
    totalProvinces: 2,
    activeProvinces: 2,
    totalParticipants: 820,
    activeParticipants: 800,
    totalEvents: 1
  },
  4: {
    id: 4,
    name: 'West',
    code: 'W',
    description: 'Western region of the country with 2 major provinces',
    created: '2023-01-10T11:45:00Z',
    lastModified: '2023-02-15T17:10:00Z',
    totalProvinces: 2,
    activeProvinces: 2,
    totalParticipants: 750,
    activeParticipants: 730,
    totalEvents: 2
  }
};

// Create event relationships
// Link regions to events
export function addRegionRelationship(
  regionId: number,
  relationName: string,
  relationObjects: RelatedLinkedObject[]
) {
  if (!mockRegionTable.relatedLinkedObjects) {
    mockRegionTable.relatedLinkedObjects = {};
  }
  
  if (!mockRegionTable.relatedLinkedObjects[relationName]) {
    mockRegionTable.relatedLinkedObjects[relationName] = {};
  }
  
  mockRegionTable.relatedLinkedObjects[relationName][regionId] = relationObjects;
}

// Modified: Fix the initializeRegionEventRelationships function to use relatedLinkedObjects
function initializeRegionEventRelationships() {
  // Create region-event relationship for Summer Giveaway (Event ID 1)
  const regionEventsMap = {
    "1": [1, 2], // North Region relates to events 1 (Summer) and 2 (Fall)
    "2": [1],    // South Region relates to event 1 (Summer)
    "3": [2, 3], // East Region relates to events 2 (Fall) and 3 (Winter)
    "4": [2, 4], // West Region relates to events 2 (Fall) and 4 (Spring)
  };

  // Set up region->events relationships
  for (const [regionId, eventIds] of Object.entries(regionEventsMap)) {
    const regionEventObjects = eventIds.map(eventId => {
      const eventRow = mockEventTable.rows.find(row => row.data.id === eventId);
      return {
        objectType: ObjectType.EVENT,
        id: eventId,
        name: eventRow?.data.name || `Unknown Event ${eventId}`
      };
    });
    
    addRegionRelationship(Number(regionId), "events", regionEventObjects);
  }
  
  // Set up event->regions relationships
  const eventRegionsMap = {
    "1": [1, 2],  // Summer Giveaway relates to North and South Regions
    "2": [1, 3, 4], // Fall Promotion relates to North, East, and West Regions
    "3": [3],     // Winter Special relates to East Region
    "4": [4]      // Spring Festival relates to West Region
  };
  
  for (const [eventId, regionIds] of Object.entries(eventRegionsMap)) {
    // FIX: Convert to proper RelatedLinkedObject[] format
    const regionObjects = regionIds.map(regionId => {
      const regionData = mockRegionTable.rows.find(row => row.data.id === regionId);
      return {
        objectType: ObjectType.REGION,
        id: regionId,
        name: regionData?.data.name || `Unknown Region ${regionId}`
      };
    });
    
    // Pass array of RelatedLinkedObject directly
    addEventRelationship(Number(eventId), "regions", regionObjects);
  }
}

// Initialize relationships
initializeRegionEventRelationships();
