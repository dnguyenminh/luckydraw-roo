import { 
  TableFetchRequest, 
  TableFetchResponse,
  ObjectType,
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
} from '@/app/lib/mockData';

// Configuration for API endpoints
export const apiConfig = {
  // Base URL for API requests - can be updated based on environment
  baseUrl: process.env.NEXT_PUBLIC_API_BASE_URL || 'https://api.example.com/v1',
  
  // Whether to use mock data instead of making real API requests
  useMockData: process.env.NEXT_PUBLIC_USE_MOCK_DATA === 'true' || true,
  
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
  [ObjectType.EVENT]: 'events',
  [ObjectType.REGION]: 'regions',
  [ObjectType.PROVINCE]: 'provinces',
  [ObjectType.REWARD]: 'rewards', 
  [ObjectType.GOLDEN_HOUR]: 'golden_hours',
  [ObjectType.PARTICIPANT]: 'participants',
  [ObjectType.SPIN_HISTORY]: 'spin_history',
  [ObjectType.AUDIT_LOG]: 'audit_log',
  [ObjectType.STATIS]: 'statistics',
  [ObjectType.USER]: 'users',
  [ObjectType.ROLE]: 'roles'
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

// Fetch table data based on request params - REFACTORED to use objectType
export async function fetchTableData(
  request: TableFetchRequest
): Promise<TableFetchResponse> {
  // Use mock data if configured
  if (apiConfig.useMockData) {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 500));
    
    // Get the endpoint from objectType in the request
    if (!request.objectType) {
      throw new Error('objectType is required in the request');
    }
    
    // Get the API endpoint from objectType
    const endpoint = objectTypeToEndpoint[request.objectType];
    if (!endpoint) {
      throw new Error(`No API endpoint mapping found for objectType: ${request.objectType}`);
    }
    
    // Get the base table data
    const baseTable = mockTables[endpoint];
    if (!baseTable) {
      throw new Error(`No mock data found for endpoint: ${endpoint}`);
    }
    
    // Clone the table to avoid modifying the original
    const result = JSON.parse(JSON.stringify(baseTable)) as TableFetchResponse;
    
    // Store the original request
    result.originalRequest = request;
    
    // Filter rows by search term (if any)
    let filteredRows = baseTable.rows;
    if (request.search && Object.keys(request.search).length > 0) {
      // Handle global search
      if (request.search.global) {
        filteredRows = filterRowsBySearch(filteredRows, request.search.global);
      }
      
      // Handle field-specific searches
      for (const [field, term] of Object.entries(request.search)) {
        if (field !== 'global') {
          filteredRows = filteredRows.filter(row => 
            row.data[field] && 
            String(row.data[field]).toLowerCase().includes(term.toLowerCase())
          );
        }
      }
    }
    
    // Filter rows by filters (if any)
    if (request.filters && request.filters.length > 0) {
      filteredRows = filterRowsByFilters(filteredRows, request.filters);
    }
    
    // Sort rows (if requested)
    if (request.sorts && request.sorts.length > 0) {
      // Apply primary sort
      const primarySort = request.sorts[0];
      filteredRows = sortRows(filteredRows, primarySort.field, primarySort.order);
    }
    
    // Update total elements count
    result.totalElements = filteredRows.length;
    
    // Calculate total pages
    result.totalPages = Math.max(1, Math.ceil(filteredRows.length / request.size));
    
    // Set current page (ensure it's valid)
    result.currentPage = Math.min(
      Math.max(0, request.page), 
      result.totalPages - 1
    );
    
    // Paginate the rows
    const startIndex = result.currentPage * request.size;
    const endIndex = startIndex + request.size;
    result.rows = filteredRows.slice(startIndex, endIndex);
    
    // Set page metadata
    result.first = result.currentPage === 0;
    result.last = result.currentPage === result.totalPages - 1;
    result.empty = result.rows.length === 0;
    result.numberOfElements = result.rows.length;
    
    return result;
  } else {
    // For real API requests
    try {
      // Get the endpoint from objectType in the request
      if (!request.objectType) {
        throw new Error('objectType is required in the request');
      }
      
      // Get the API endpoint from objectType
      const endpointPath = objectTypeToEndpoint[request.objectType];
      if (!endpointPath) {
        throw new Error(`No API endpoint mapping found for objectType: ${request.objectType}`);
      }
      
      // Construct the full API URL
      const apiUrl = getApiUrl(endpointPath);
      
      // Make the actual API request
      const response = await fetch(apiUrl, {
        method: 'POST',
        headers: {
          ...apiConfig.headers,
          // Add any auth tokens if needed
          ...(localStorage.getItem('authToken') ? {
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`
          } : {})
        },
        body: JSON.stringify(request),
        signal: AbortSignal.timeout(apiConfig.timeout)
      });
      
      if (!response.ok) {
        throw new Error(`API error: ${response.status} ${response.statusText}`);
      }
      
      return await response.json() as TableFetchResponse;
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }
}

// Generic fetch function that handles any entity type
export async function fetchEntityData(
  entityType: keyof typeof ObjectType,
  request: TableFetchRequest
): Promise<TableFetchResponse> {
  // Add objectType to the request
  request.objectType = ObjectType[entityType as keyof typeof ObjectType];
  
  // Use the refactored fetchTableData function that doesn't need a table name
  return fetchTableData(request);
}

// Convert entity type string to ObjectType enum
function getObjectTypeForEntityName(entityName: string): ObjectType | undefined {
  // Convert string like 'event' to ObjectType.EVENT
  const upperCaseEntityName = entityName.toUpperCase();
  return Object.values(ObjectType).find(
    obj => obj === upperCaseEntityName || obj === entityName
  );
}

// Helper function for related tables
export async function fetchRelatedTableData(
  sourceEntityType: string,
  parentId: number,
  relationName: string,
  request?: TableFetchRequest
): Promise<TableFetchResponse> {
  // Use default request if not provided
  const fetchRequest = request || {
    page: 0,
    size: 10,
    sorts: [],
    filters: [],
    search: {}
  };
  
  // Get the source entity's API endpoint
  const sourceEndpoint = entityApiEndpoints[sourceEntityType.toLowerCase()];
  if (!sourceEndpoint) {
    throw new Error(`No API endpoint mapping found for entity: ${sourceEntityType}`);
  }
  
  // Get parent table
  const parentTable = mockTables[sourceEndpoint];
  if (!parentTable || !parentTable.relatedTables) {
    throw new Error(`No mock data found for entity: ${sourceEntityType} or no related tables`);
  }
  
  // Get the related table for this parent ID
  const relatedTablesForType = parentTable.relatedTables[relationName];
  if (!relatedTablesForType) {
    throw new Error(`No related tables of type ${relationName} found for ${sourceEntityType}`);
  }
  
  const relatedTable = relatedTablesForType[parentId];
  if (!relatedTable) {
    // Return empty result
    return {
      totalPages: 0,
      currentPage: 0,
      pageSize: fetchRequest.size,
      totalElements: 0,
      tableName: `${sourceEndpoint}_${relationName}`,
      rows: [],
      originalRequest: fetchRequest,
      statistics: {},
      first: true,
      last: true,
      empty: true,
      numberOfElements: 0
    };
  }
  
  // Clone to avoid modifications to the original
  const result = JSON.parse(JSON.stringify(relatedTable)) as TableFetchResponse;
  
  // Set the original request
  result.originalRequest = fetchRequest;
  
  return result;
}
