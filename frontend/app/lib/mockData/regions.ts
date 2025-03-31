import { TableFetchResponse, FieldType, SortType } from './interfaces';
import { mockEventTable, addEventRelationship } from './events';

// Mock region table data
export const mockRegionTable: TableFetchResponse = {
  totalPages: 2,
  currentPage: 0,
  pageSize: 10,
  totalElements: 12,
  tableName: "regions",
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
      displayName: "Region Name",
      filterable: true
    },
    { 
      fieldName: "code", 
      fieldType: FieldType.STRING, 
      sortType: SortType.ASCENDING,
      displayName: "Code",
      filterable: true
    },
    { 
      fieldName: "provinceCount", 
      fieldType: FieldType.NUMBER, 
      sortType: SortType.ASCENDING,
      displayName: "Provinces",
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
      data: { id: 1, name: 'North Region', code: 'NR', provinceCount: 8, participantCount: 8450, status: 'Active' }
    },
    {
      data: { id: 2, name: 'South Region', code: 'SR', provinceCount: 6, participantCount: 7320, status: 'Active' }
    },
    {
      data: { id: 3, name: 'East Region', code: 'ER', provinceCount: 5, participantCount: 5680, status: 'Active' }
    },
    {
      data: { id: 4, name: 'West Region', code: 'WR', provinceCount: 7, participantCount: 4120, status: 'Active' }
    },
    {
      data: { id: 5, name: 'Central Region', code: 'CR', provinceCount: 4, participantCount: 3780, status: 'Inactive' }
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
  numberOfElements: 5
};

// Mock region details
export const mockRegionDetails: Record<number, any> = {
  1: {
    id: 1,
    name: 'North Region',
    code: 'NR',
    description: 'The northern region of the country',
    created: '2022-12-01',
    lastModified: '2023-02-15',
    totalProvinces: 8,
    activeProvinces: 7,
    totalParticipants: 8450,
    activeParticipants: 6230,
    totalEvents: 2
  },
  2: {
    id: 2,
    name: 'South Region',
    code: 'SR',
    description: 'The southern region of the country',
    created: '2022-12-01',
    lastModified: '2023-01-20',
    totalProvinces: 6,
    activeProvinces: 6,
    totalParticipants: 7320,
    activeParticipants: 5840,
    totalEvents: 1
  },
};

// Create event relationships
// Link regions to events
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
    const regionEventRows = eventIds.map(eventId => {
      const eventData = mockEventTable.rows.find(row => row.data.id === eventId);
      return eventData || { data: { id: eventId, name: `Unknown Event ${eventId}` } };
    });
    
    if (!mockRegionTable.relatedTables) mockRegionTable.relatedTables = {};
    if (!mockRegionTable.relatedTables.events) mockRegionTable.relatedTables.events = {};
    
    mockRegionTable.relatedTables.events[regionId] = {
      totalPages: 1,
      currentPage: 0,
      pageSize: 10,
      totalElements: regionEventRows.length,
      tableName: "region_events",
      rows: regionEventRows,
      first: true,
      last: true,
      empty: regionEventRows.length === 0,
      numberOfElements: regionEventRows.length
    };
  }
  
  // Set up event->regions relationships
  const eventRegionsMap = {
    "1": [1, 2],  // Summer Giveaway relates to North and South Regions
    "2": [1, 3, 4], // Fall Promotion relates to North, East, and West Regions
    "3": [3],     // Winter Special relates to East Region
    "4": [4]      // Spring Festival relates to West Region
  };
  
  for (const [eventId, regionIds] of Object.entries(eventRegionsMap)) {
    const eventRegionRows = regionIds.map(regionId => {
      const regionData = mockRegionTable.rows.find(row => row.data.id === regionId);
      return regionData || { data: { id: regionId, name: `Unknown Region ${regionId}` } };
    });
    
    addEventRelationship(Number(eventId), "regions", {
      totalPages: 1,
      currentPage: 0,
      pageSize: 10,
      totalElements: eventRegionRows.length,
      tableName: "event_regions",
      rows: eventRegionRows,
      first: true,
      last: true,
      empty: eventRegionRows.length === 0,
      numberOfElements: eventRegionRows.length
    });
  }
}

// Initialize relationships
initializeRegionEventRelationships();
