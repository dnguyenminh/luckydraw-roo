'use client';

import { useState, useEffect, ReactNode, useRef, useCallback } from 'react';
import { Search, Filter, ChevronDown, ChevronUp, MoreHorizontal, Plus } from 'lucide-react';
import { TableFetchRequest, TableFetchResponse, ObjectType, TabTableRow, FetchStatus, DataObjectKeyValues, DataObject, SortType, FilterType, ColumnInfo } from '@/app/lib/api/interfaces';
import EntityDetailTabs from './EntityDetailTabs';
import { fetchTableData } from '@/app/lib/api/tableService';

export interface ColumnDef {
  key: string;
  header: string;
  render?: (value: any, row: any) => ReactNode;
}

export interface ActionDef {
  label: string;
  onClick: (row: any) => void;
  color?: 'blue' | 'red' | 'green' | 'yellow';
  iconLeft?: ReactNode;
  iconRight?: ReactNode;
}

interface FilterOption {
  key: string;
  label: string;
  options: { value: string; label: string }[];
}

// Detail view modes
type DetailViewMode = 'custom' | 'auto' | 'tabs';

// Add a mapping of entity types to their API endpoints
const entityApiEndpoints: Record<string, string> = {
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

interface DataTableProps {
  data?: TableFetchResponse | null;
  columns?: ColumnDef[];
  actions?: ActionDef[];
  detailView?: (rowData: any) => ReactNode;
  detailViewMode?: DetailViewMode;
  entityType?: keyof typeof ObjectType;
  addItemButton?: {
    label: string;
    onClick: () => void;
  } | boolean;
  filterOptions?: FilterOption[];
  urlStatePrefix?: string;
  emptyMessage?: string;
  onPageChange?: (page: number) => void;
  onSortChange?: (property: string, direction: string) => void;
  onSearchChange?: (search: string) => void;
  onFilterChange?: (filter: string, value: string) => void;
  fetchData?: (request: TableFetchRequest) => Promise<TableFetchResponse>;
  showDetailView?: boolean;
  activeTab?: string;
  statusField?: string;
}

// Helper function to generate column definitions from fieldNameMap
const generateColumnsFromFieldMap = (fieldNameMap: Record<string, ColumnInfo>): ColumnDef[] => {
  // Add a check to handle null or undefined fieldNameMap
  if (!fieldNameMap) return [];
  
  return Object.entries(fieldNameMap).map(([key, columnInfo]) => {
    let renderer: ((value: any, row: any) => ReactNode) | undefined;
    
    // Special renderers for different field types
    if (columnInfo.fieldType === 'BOOLEAN') {
      renderer = (value) => 
        value === true ? '✓' : 
        value === false ? '✗' : 
        '-';
    }
    
    // Date field formatting
    if (columnInfo.fieldType === 'DATETIME' || columnInfo.fieldType === 'DATE') {
      renderer = (value) => {
        if (!value) return '-';
        try {
          return new Date(value).toLocaleString();
        } catch (e) {
          return String(value);
        }
      };
    }
    
    // Status tag styling
    if (key === 'status') {
      renderer = (value) => (
        <span className={`px-2 py-1 rounded-full text-xs ${
          value === 'ACTIVE' ? 'bg-green-800 text-green-200' : 
          value === 'INACTIVE' ? 'bg-red-800 text-red-200' : 
          'bg-gray-800 text-gray-200'
        }`}>
          {value}
        </span>
      );
    }
    
    return {
      key,
      header: columnInfo.fieldName.charAt(0).toUpperCase() + columnInfo.fieldName.slice(1),
      render: renderer
    };
  }).sort((a, b) => {
    if (a.key === 'id') return -1;
    if (b.key === 'id') return 1;
    if (a.key === 'status') return -1;
    if (b.key === 'status') return 1;
    return a.key.localeCompare(b.key);
  });
};

// Enhance safeRenderValue to handle more object types
const safeRenderValue = (value: any): React.ReactNode => {
  if (value === null || value === undefined) {
    return '-';
  }
  
  if (typeof value === 'object') {
    // Handle Date objects
    if (value instanceof Date) {
      return value.toLocaleString();
    }
    
    // Handle timestamps that look like ISO dates
    if (typeof value === 'string' && value.match(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/)) {
      return new Date(value).toLocaleString();
    }
    
    // Check if the object has an ID and name (common entity pattern)
    if (value?.id && value?.name) {
      return value.name;
    }
    
    // For BlacklistedToken entity, show token instead
    if (value?.token && value?.tokenType) {
      return `${value.tokenType}: ${value.token.substring(0, 10)}...`;
    }
    
    // For other objects, show a simplified representation
    return '{Object}';
  }
  
  // Handle boolean values
  if (typeof value === 'boolean') {
    return value ? '✓' : '✗';
  }
  
  // Return strings, numbers and other primitives as is
  return String(value);
};

export default function DataTable({
  data: initialData,
  columns,
  actions,
  detailView,
  detailViewMode = 'auto',
  entityType = ObjectType.Event,
  addItemButton,
  filterOptions,
  urlStatePrefix,
  emptyMessage = "No data found.",
  onPageChange,
  onSortChange,
  onSearchChange,
  onFilterChange,
  fetchData: providedFetchData,
  showDetailView = true,
  activeTab,
  statusField = 'status'
}: DataTableProps) {
  const emptyTableData: TableFetchResponse = {
    totalPage: 0,
    currentPage: 0,
    pageSize: 10,
    totalElements: 0,
    tableName: entityType || "empty",
    rows: [],
    status: FetchStatus.NO_DATA,
    message: "No data available",
    fieldNameMap: {},
    originalRequest: {
      page: 0,
      size: 10,
      sorts: [],
      filters: [],
      search: Object.values(ObjectType).reduce((acc, type) => {
        acc[type] = {
          objectType: type,
          key: {
            keys: []
          },
          fieldNameMap: {},
          description: '',
          data: {
            data: {}
          },
          order: 0
        };
        return acc;
      }, {} as Record<ObjectType, DataObject>),
      objectType: ObjectType[entityType as keyof typeof ObjectType]
    },
    statistics: {
      charts: {}
    },
    relatedLinkedObjects: {},
    first: true,
    last: true,
    empty: true,
    numberOfElements: 0
  };

  const safeInitialData = initialData || emptyTableData;

  const [data, setData] = useState<TableFetchResponse>(safeInitialData);
  const [expandedRowId, setExpandedRowId] = useState<number | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filters, setFilters] = useState<Record<string, string>>({});
  const [currentPage, setCurrentPage] = useState(0);
  const [sortField, setSortField] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [isLoading, setIsLoading] = useState(false);

  const defaultFetchData = async (request: TableFetchRequest): Promise<TableFetchResponse> => {
    const requestWithObjectType: TableFetchRequest = {
      ...request,
      objectType: ObjectType[entityType as keyof typeof ObjectType]
    };

    try {
      console.log(`Using default fetchTableData for ${entityType}`);
      const response = await fetchTableData(requestWithObjectType);
      return response;
    } catch (error) {
      console.error(`Error fetching ${entityType} data:`, error);
      return emptyTableData;
    }
  };

  const effectiveFetchData = providedFetchData || defaultFetchData;

  const currentRequestRef = useRef<string | null>(null);

  const loadData = useCallback(async (options: {
    page?: number;
    sortField?: string | null;
    sortDirection?: 'asc' | 'desc';
    filters?: Record<string, string>;
    searchTerm?: string;
    isInitialLoad?: boolean;
  } = {}) => {
    const {
      page = currentPage,
      sortField: requestSortField = sortField,
      sortDirection: requestSortDirection = sortDirection,
      filters: requestFilters = filters,
      searchTerm: requestSearchTerm = searchTerm,
      isInitialLoad = false
    } = options;

    if (initialData && !isInitialLoad) {
      console.debug(`Skipping fetch - using provided data for ${entityType}`);
      return;
    }

    const requestSignature = JSON.stringify({
      page, 
      sortField: requestSortField,
      sortDirection: requestSortDirection, 
      filters: requestFilters, 
      searchTerm: requestSearchTerm
    });
    
    if (requestSignature === currentRequestRef.current) {
      console.debug(`Skipping duplicate request for ${entityType}:`, requestSignature);
      return;
    }
    
    currentRequestRef.current = requestSignature;
    
    setIsLoading(true);

    try {
      const emptySearch = Object.values(ObjectType).reduce((acc, type) => {
        acc[type] = {
          objectType: type,
          key: { keys: [] },
          fieldNameMap: {},
          description: '',
          data: { data: {} },
          order: 0
        };
        return acc;
      }, {} as Record<ObjectType, DataObject>);
      
      const request: TableFetchRequest = {
        page,
        size: data.pageSize || 10,
        sorts: requestSortField ? [
          {
            field: requestSortField,
            sortType: requestSortDirection === 'asc' ? SortType.ASCENDING : SortType.DESCENDING
          }
        ] : [],
        filters: Object.entries(requestFilters)
          .filter(([_, value]) => value && value !== 'all')
          .map(([key, value]) => ({
            field: key,
            filterType: FilterType.CONTAINS,
            minValue: value,
            maxValue: ''
          })),
        search: emptySearch,
        objectType: ObjectType[entityType as keyof typeof ObjectType]
      };

      if (requestSearchTerm) {
        const entityObjectType = ObjectType[entityType as keyof typeof ObjectType];
        
        if (request.search[entityObjectType]) {
          request.search[entityObjectType] = {
            ...request.search[entityObjectType],
            data: {
              data: { _search: requestSearchTerm }
            }
          };
        }
      }

      console.debug(`Fetching data for ${entityType} with options:`, { 
        page, sort: `${requestSortField}:${requestSortDirection}`,
        filtersCount: Object.keys(requestFilters).length,
        hasSearchTerm: !!requestSearchTerm
      });
      
      const response = await effectiveFetchData(request);
      setData(response);
    } catch (error) {
      console.error(`Error fetching data for ${entityType}:`, error);
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, sortField, sortDirection, filters, searchTerm, entityType, effectiveFetchData, data.pageSize, initialData]);

  useEffect(() => {
    if (!initialData) {
      loadData({ isInitialLoad: true });
    }
    
    return () => {
      currentRequestRef.current = null;
    };
  }, [entityType, initialData, loadData]);

  useEffect(() => {
    if (initialData) return;
    
    const debounceTimer = setTimeout(() => {
      loadData();
    }, 300);
    
    return () => clearTimeout(debounceTimer);
  }, [currentPage, sortField, sortDirection, filters, searchTerm, loadData, initialData]);

  const actualAddItemButton = typeof addItemButton === 'boolean' && addItemButton === true
    ? {
      label: `Add ${entityType.charAt(0).toUpperCase() + entityType.slice(1).toLowerCase()}`,
      onClick: () => console.log(`Add ${entityType} clicked`)
    }
    : (typeof addItemButton === 'object' ? addItemButton : undefined);

  const defaultDetailView = (rowData: TabTableRow) => {
    return (
      <EntityDetailTabs
        tableRow={rowData}
        entityType={entityType}
      />
    );
  };

  const renderRowDetail = (rowData: any) => {
    if (!showDetailView) return null;

    if (detailViewMode === 'custom' && detailView) {
      return detailView(rowData);
    }

    if (detailViewMode === 'tabs' || detailViewMode === 'auto') {
      return defaultDetailView(rowData);
    }

    return null;
  };

  const handleRowClick = (rowId: number) => {
    if (showDetailView && (detailView || detailViewMode !== 'custom')) {
      setExpandedRowId(expandedRowId === rowId ? null : rowId);
    }
  };

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setSearchTerm(value);
    if (onSearchChange) {
      onSearchChange(value);
    }
  };

  const handleSortClick = (field: string) => {
    if (sortField === field) {
      const newDirection = sortDirection === 'asc' ? 'desc' : 'asc';
      setSortDirection(newDirection);
      if (onSortChange) {
        onSortChange(field, newDirection);
      }
    } else {
      setSortField(field);
      setSortDirection('asc');
      if (onSortChange) {
        onSortChange(field, 'asc');
      }
    }
  };

  const hasData = data?.rows && data.rows.length > 0;

  const effectiveColumns = columns || (data?.fieldNameMap ? 
    generateColumnsFromFieldMap(data.fieldNameMap) : []);

  return (
    <div className="w-full">
      <div className="mb-4 flex flex-wrap justify-between items-center gap-3">
        <div className="flex flex-wrap gap-2">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-500" />
            <input
              type="text"
              placeholder="Search..."
              className="pl-9 py-2 pr-4 bg-[#3c3c3c] rounded focus:outline-none focus:ring-1 focus:ring-[#007acc]"
              value={searchTerm}
              onChange={handleSearchChange}
            />
          </div>

          {filterOptions && filterOptions.map((filter) => (
            <select
              key={filter.key}
              className="bg-[#3c3c3c] text-white px-3 py-2 rounded focus:outline-none focus:ring-1 focus:ring-[#007acc]"
              value={filters[filter.key] || 'all'}
              onChange={(e) => setFilters(prev => ({ ...prev, [filter.key]: e.target.value }))}
            >
              {filter.options.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          ))}
        </div>

        {actualAddItemButton && (
          <button
            className="bg-[#007acc] text-white px-3 py-2 rounded hover:bg-[#0069ac] flex items-center"
            onClick={actualAddItemButton.onClick}
          >
            <Plus className="h-4 w-4 mr-2" />
            {actualAddItemButton.label}
          </button>
        )}
      </div>

      <div className="bg-[#1e1e1e] border border-[#3c3c3c] rounded-md overflow-hidden">
        {effectiveColumns && effectiveColumns.length > 0 && (
          <div className="grid grid-cols-12 bg-[#2d2d2d] text-white font-medium p-3">
            {effectiveColumns.map((column) => (
              <div
                key={column.key}
                className="col-span-1 flex items-center cursor-pointer"
                onClick={() => handleSortClick(column.key)}
              >
                {column.header}
                {sortField === column.key && (
                  sortDirection === 'asc' ?
                    <ChevronUp className="ml-1 h-4 w-4" /> :
                    <ChevronDown className="ml-1 h-4 w-4" />
                )}
              </div>
            ))}

            {actions && actions.length > 0 && (
              <div className="col-span-2 text-right">Actions</div>
            )}
          </div>
        )}

        <div className="divide-y divide-[#3c3c3c]">
          {isLoading ? (
            <div className="p-8 text-center">
              <div className="animate-spin h-8 w-8 border-4 border-[#007acc] border-t-transparent rounded-full mx-auto mb-2"></div>
              <p className="text-gray-400">Loading data...</p>
            </div>
          ) : hasData ? (
            data.rows.map((row) => (
              <div key={row.data?.id || Math.random()}>
                <div
                  className={`grid grid-cols-12 p-3 text-[#cccccc] ${showDetailView ? 'cursor-pointer hover:bg-[#2a2d2e]' : ''} ${expandedRowId === row.data?.id ? 'bg-[#2a2d2e]' : ''}`}
                  onClick={() => row.data?.id && handleRowClick(row.data.id)}
                >
                  {effectiveColumns.map((column) => (
                    <div key={column.key} className="col-span-1 break-words">
                      {column.render 
                        ? column.render(row.data[column.key], row.data) 
                        : safeRenderValue(row.data[column.key])}
                    </div>
                  ))}

                  {actions && actions.length > 0 && (
                    <div className="col-span-2 flex justify-end space-x-2">
                      {actions.map((action) => (
                        <button
                          key={action.label}
                          className={`flex items-center ${action.color}`}
                          onClick={(e) => {
                            e.stopPropagation();
                            action.onClick(row.data);
                          }}
                        >
                          {action.iconLeft}
                          <span>{action.label}</span>
                          {action.iconRight}
                        </button>
                      ))}
                    </div>
                  )}
                </div>

                {row.data?.id && expandedRowId === row.data.id && (
                  <div className="p-4 bg-[#252525] border-t border-[#3c3c3c]">
                    {renderRowDetail(row.data)}
                  </div>
                )}
              </div>
            ))
          ) : (
            <div className="p-8 text-center text-gray-400">
              {emptyMessage}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
