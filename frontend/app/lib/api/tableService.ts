import {
  TableFetchRequest,
  TableFetchResponse,
  ObjectType,
  SortType,
  FetchStatus,
  StatisticsInfo,
  FilterType,
  DataObject
} from './interfaces';
import { stringToObjectType } from '../mockData/utils';
import { mockFetchTableData } from './mockData'; // Import the correct function

import {
  mockEventTable,
  mockParticipantTable,
  mockRegionTable,
  mockProvinceTable,
  mockRewardTable,
  mockGoldenHourTable,
  mockAuditLogTable,
  mockSpinHistoryTable,
  mockUserTable,
  mockRoleTable
} from '../mockData/index';

// Configuration for API endpoints
export const apiConfig = {
  // Base URL for API requests - ensure it matches the provided endpoint
  baseUrl: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api',

  // Set to false to use real API by default
  useMockData: process.env.NEXT_PUBLIC_USE_MOCK_DATA === 'true' || false,

  // Request timeout in milliseconds
  timeout: 30000,

  // Additional headers to include with all requests
  headers: {
    'Content-Type': 'application/json'
  }
};

// Map of table names to their mock data
const mockTables: Record<string, TableFetchResponse> = {
  'events': mockEventTable,
  'participants': mockParticipantTable,
  'regions': mockRegionTable,
  'provinces': mockProvinceTable,
  'rewards': mockRewardTable,
  'golden_hours': mockGoldenHourTable,
  'audit_log': mockAuditLogTable,
  'spin_history': mockSpinHistoryTable,
  'users': mockUserTable,
  'roles': mockRoleTable
};

// Centralized entity API endpoint mapping - single source of truth
export const entityApiEndpoints: Record<string, string> = {
  event: 'events',
  participant: 'participants',
  region: 'regions',
  province: 'provinces',
  reward: 'rewards',
  goldenHour: 'golden_hours',
  auditLog: 'audit_log',
  spinHistory: 'spin_history',
  user: 'users',
  role: 'roles'
};

// Mapping from entity types to API endpoints
export const objectTypeToEndpoint: Record<ObjectType, string> = {
  [ObjectType.Event]: 'events',
  [ObjectType.Region]: 'regions',
  [ObjectType.Province]: 'provinces',
  [ObjectType.Reward]: 'rewards',
  [ObjectType.GoldenHour]: 'golden_hours',
  [ObjectType.Participant]: 'participants',
  [ObjectType.SpinHistory]: 'spin_history',
  [ObjectType.AuditLog]: 'audit_log',
  [ObjectType.Statistics]: 'statistics',
  [ObjectType.User]: 'users',
  [ObjectType.Role]: 'roles',
  [ObjectType.Permission]: 'permissions',
  [ObjectType.Configuration]: 'configurations',
  [ObjectType.BlacklistedToken]: 'blacklisted_tokens',
  [ObjectType.EventLocation]: 'event_locations',
  [ObjectType.ParticipantEvent]: 'participant_events'
};

// Helper function to construct full API URLs
export function getApiUrl(endpoint: string): string {
  // Remove trailing slash from base URL if present
  const baseUrl = apiConfig.baseUrl.endsWith('/')
    ? apiConfig.baseUrl.slice(0, -1)
    : apiConfig.baseUrl;

  // Add leading slash to endpoint if missing
  const formattedEndpoint = endpoint.startsWith('/')
    ? endpoint
    : `/${endpoint}`;

  return `${baseUrl}${formattedEndpoint}`;
}

// Function to filter rows based on search term
function filterRowsBySearch(rows: any[], searchTerm: string): any[] {
  if (!searchTerm) return rows;

  const searchLower = searchTerm.toLowerCase();
  return rows.filter(row => {
    // Check each field in the row data
    return Object.values(row.data).some(value =>
      value && String(value).toLowerCase().includes(searchLower)
    );
  });
}

// Function to filter rows based on filters
function filterRowsByFilters(rows: any[], filters: any[]): any[] {
  if (!filters || filters.length === 0) return rows;

  return rows.filter(row => {
    // All filters must pass for a row to be included
    return filters.every(filter => {
      const fieldValue = row.data[filter.field];

      // Skip if the field doesn't exist
      if (fieldValue === undefined) return true;

      // Handle different operators
      switch (filter.operator) {
        case 'eq': // equals
          return String(fieldValue).toLowerCase() === String(filter.value).toLowerCase();
        case 'neq': // not equals
          return String(fieldValue).toLowerCase() !== String(filter.value).toLowerCase();
        case 'gt': // greater than
          return Number(fieldValue) > Number(filter.value);
        case 'gte': // greater than or equal
          return Number(fieldValue) >= Number(filter.value);
        case 'lt': // less than
          return Number(fieldValue) < Number(filter.value);
        case 'lte': // less than or equal
          return Number(fieldValue) <= Number(filter.value);
        case 'contains': // contains
          return String(fieldValue).toLowerCase().includes(String(filter.value).toLowerCase());
        default:
          return true;
      }
    });
  });
}

// Function to sort rows
function sortRows(rows: any[], sortField: string, sortOrder: string): any[] {
  if (!sortField) return rows;

  return [...rows].sort((a, b) => {
    const aValue = a.data[sortField];
    const bValue = b.data[sortField];

    // Handle undefined values
    if (aValue === undefined && bValue === undefined) return 0;
    if (aValue === undefined) return 1;
    if (bValue === undefined) return -1;

    // Compare based on type
    if (typeof aValue === 'number' && typeof bValue === 'number') {
      return sortOrder === 'asc' ? aValue - bValue : bValue - aValue;
    }

    // Default string comparison
    const aString = String(aValue).toLowerCase();
    const bString = String(bValue).toLowerCase();
    return sortOrder === 'asc'
      ? aString.localeCompare(bString)
      : bString.localeCompare(aString);
  });
}

// Mock implementation for now - you would replace this with real API calls
export async function fetchTableData(request: TableFetchRequest): Promise<TableFetchResponse> {
  try {
    // Validate the objectType before sending request
    if (request.objectType === undefined) {
      console.error('Missing objectType in request:', request);
      throw new Error('objectType is required in the request');
    }

    // // Convert the request to what the API expects
    // const apiRequest = {
    //   ...request,
    //   objectType: typeof request.objectType === 'string' ? 
    //     request.objectType : 
    //     ObjectType[request.objectType]
    // };
    
    // Log what we're sending to the API
    console.log('Sending table data request:', request);

    // Create a proper URL for the API endpoint
    const url = `${apiConfig.baseUrl}/table-data/fetch/${request.objectType.toLowerCase()}`;
    
    // Log the request for debugging
    console.log(`Fetching data from: ${url}`, request);
    
    try {
      // // Simplify the search structure to avoid sending unnecessary data
      // // Only include the relevant entity type in the search object
      // const simplifiedSearch: Record<ObjectType, DataObject> = {} as Record<ObjectType, DataObject>;
      
      // // Only add the current entity type to the search
      // const entityType = request.objectType;
      
      // // Check if there's actual search data for this entity
      // let hasSearchData = false;
      // if (request.search && 
      //     request.search[entityType] && 
      //     request.search[entityType].data?.data?._search) {
      //   hasSearchData = true;
      // }
      
      // // Only include the current entity in search, and only if there's actual search data
      // if (hasSearchData) {
      //   simplifiedSearch[entityType] = {
      //     objectType: entityType,
      //     key: { keys: [] },
      //     fieldNameMap: {},
      //     description: '',
      //     data: {
      //       data: { 
      //         _search: request.search[entityType].data.data._search || "" 
      //       }
      //     },
      //     order: 0
      //   };
      // }

      // // Send the request with a simplified search structure
      // const sanitizedRequest = {
      //   page: request.page,
      //   size: request.size || 10,
      //   sorts: request.sorts || [],
      //   filters: request.filters || [],
      //   // If there's no search data, send an empty object instead of complex structure
      //   search: hasSearchData ? simplifiedSearch : {},
      //   objectType: request.objectType,
      //   // Add the required entityName property using the mapping
      //   entityName: request.objectType
      // };

      // console.log('Sending simplified request:', sanitizedRequest);

      const response = await fetch(url, {
        method: 'POST',
        headers: {
          ...apiConfig.headers
        },
        body: JSON.stringify(request)
      });

      // Add more detailed error handling
      if (!response.ok) {
        const errorText = await response.text();
        console.error(`API request failed with status ${response.status}:`, errorText);
        throw new Error(`API error: ${response.status} ${errorText ? '- ' + errorText : ''}`);
      }

      const data = await response.json();
      
      // If using mock data is enabled and no real data is available, use mock data as fallback
      if (apiConfig.useMockData && (!data || !data.rows || data.rows.length === 0)) {
        console.log(`No data returned from API for ${request.objectType}, using mock data instead.`);
        return mockFetchTableData(request); // Use the imported function
      }
      
      return data;
    } catch (error) {
      console.error('Error fetching table data:', error);
      
      // If using mock data is enabled, use it as a fallback on errors
      if (apiConfig.useMockData) {
        console.log(`API request failed, using mock data as fallback for ${request.objectType}`);
        return mockFetchTableData(request); // Use the imported function
      }
      
      // Otherwise propagate the error
      throw error;
    }
  } catch (error) {
    console.error('Error in fetchTableData:', error);
    throw error;
  }
}

// Updated fetchEntityData to remove redundant parameter
export async function fetchEntityData(
  request: TableFetchRequest
): Promise<TableFetchResponse> {
  // Validate that objectType is provided in the request
  if (!request.objectType) {
    throw new Error('objectType is required in the request');
  }

  // Use the fetchTableData function directly since objectType is already in the request
  return fetchTableData(request);
}

// Convert entity type string to ObjectType enum
function getObjectTypeForEntityName(entityName: string): ObjectType | undefined {
  // Convert string like 'event' to ObjectType.EVENT
  const upperCaseEntityName = entityName.toUpperCase();
  return ObjectType[upperCaseEntityName as keyof typeof ObjectType];
}

// Function to fetch related table data from the API
async function fetchRelatedTableData(
  sourceTableName: string,
  entityId: number,
  relatedTableName: string,
  request: any
): Promise<TableFetchResponse> {
  // Create a request with proper filters to get related data
  const tableRequest = {
    ...request,
    objectType: relatedTableName.toUpperCase() as unknown as ObjectType,
    filters: [
      ...request.filters || [],
      {
        field: `${sourceTableName.toLowerCase()}Id`,
        filterType: 'EQUALS',
        minValue: entityId.toString(),
        maxValue: entityId.toString()
      }
    ]
  };
  
  // Use the fetchTableData service with the correct API endpoint
  return await fetchTableData(tableRequest);
}
