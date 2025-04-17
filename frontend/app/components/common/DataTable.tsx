'use client';

/**
 * Enhanced DataTable Component
 * 
 * Features:
 * - Dynamic column sorting and filtering based on fieldNameMap metadata
 * - Type-specific filtering for text, number, date, and boolean fields
 * - Advanced pagination with configurable page sizes
 * - Alternating row colors for better readability
 * - Expandable rows with tabbed detail view using EntityDetailTabs
 * - Lazy loading of tab content
 */

import { useState, useEffect, ReactNode, useRef, useCallback, useMemo, Fragment } from 'react';
import { Search, Filter, ChevronDown, ChevronUp, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight, Plus, X, ChevronRight as ExpandIcon, ChevronDown as CollapseIcon } from 'lucide-react';
import { TableFetchRequest, TableFetchResponse, ObjectType, TabTableRow, FetchStatus, DataObject, SortType, FilterType, ColumnInfo, TableRow } from '@/app/lib/api/interfaces';
import EntityDetailTabs from './EntityDetailTabs';
import { fetchTableData } from '@/app/lib/api/tableService';

// Page size options
const PAGE_SIZE_OPTIONS = [10, 20, 30, 50, 100, 500, 1000];

// Define filter operator enums
enum TextFilterOperator {
  EQUALS = 'equals',
  CONTAINS = 'contains',
  STARTS_WITH = 'startsWith',
  ENDS_WITH = 'endsWith'
}

enum NumericFilterOperator {
  EQUALS = 'equals',
  BETWEEN = 'between',
  GREATER_THAN = 'greaterThan',
  LESS_THAN = 'lessThan'
}

interface FilterState {
  field: string;
  operator: TextFilterOperator | NumericFilterOperator | string;
  value: any;
  secondValue?: any; // For BETWEEN operator
  active: boolean;
}

interface PaginationState {
  pageIndex: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
}

// Filter component for text fields
const TextFilter: React.FC<{
  column: ColumnDef;
  filterState: FilterState;
  onChange: (filterState: FilterState) => void;
  onClose: () => void;
}> = ({ column, filterState, onChange, onClose }) => {
  return (
    <div className="p-2 bg-[#2d2d2d] border border-[#3c3c3c] rounded shadow-lg absolute z-10 w-64 mt-1">
      <div className="flex justify-between items-center mb-2">
        <h3 className="text-sm font-medium">Filter {column.header}</h3>
        <button onClick={onClose} className="text-gray-400 hover:text-white">
          <X size={16} />
        </button>
      </div>

      <select
        className="mb-2 w-full bg-[#3c3c3c] text-white p-1 rounded"
        value={filterState.operator}
        onChange={(e) => onChange({ ...filterState, operator: e.target.value as TextFilterOperator })}
      >
        <option value={TextFilterOperator.EQUALS}>Equals</option>
        <option value={TextFilterOperator.CONTAINS}>Contains</option>
        <option value={TextFilterOperator.STARTS_WITH}>Starts with</option>
        <option value={TextFilterOperator.ENDS_WITH}>Ends with</option>
      </select>

      <input
        type="text"
        className="w-full bg-[#3c3c3c] text-white p-1 rounded mb-2"
        value={filterState.value || ''}
        onChange={(e) => onChange({ ...filterState, value: e.target.value })}
        placeholder="Enter value..."
      />

      <div className="flex justify-between">
        <button
          className="bg-red-700 text-white px-2 py-1 rounded text-xs"
          onClick={() => onChange({ ...filterState, active: false, value: '' })}
        >
          Clear
        </button>

        <button
          className="bg-[#007acc] text-white px-2 py-1 rounded text-xs"
          onClick={() => {
            onChange({ ...filterState, active: true });
            onClose();
          }}
        >
          Apply
        </button>
      </div>
    </div>
  );
};

// Filter component for numeric and date fields
const NumericFilter: React.FC<{
  column: ColumnDef;
  filterState: FilterState;
  onChange: (filterState: FilterState) => void;
  onClose: () => void;
}> = ({ column, filterState, onChange, onClose }) => {
  const isDateField = column.fieldType === 'DATE' || column.fieldType === 'DATETIME' || column.fieldType === 'TIME';
  const inputType = isDateField ? (column.fieldType === 'TIME' ? 'time' : 'datetime-local') : 'number';

  return (
    <div className="p-2 bg-[#2d2d2d] border border-[#3c3c3c] rounded shadow-lg absolute z-10 w-64 mt-1">
      <div className="flex justify-between items-center mb-2">
        <h3 className="text-sm font-medium">Filter {column.header}</h3>
        <button onClick={onClose} className="text-gray-400 hover:text-white">
          <X size={16} />
        </button>
      </div>

      <select
        className="mb-2 w-full bg-[#3c3c3c] text-white p-1 rounded"
        value={filterState.operator}
        onChange={(e) => onChange({ ...filterState, operator: e.target.value as NumericFilterOperator })}
      >
        <option value={NumericFilterOperator.EQUALS}>Equals</option>
        <option value={NumericFilterOperator.GREATER_THAN}>Greater than</option>
        <option value={NumericFilterOperator.LESS_THAN}>Less than</option>
        <option value={NumericFilterOperator.BETWEEN}>Between</option>
      </select>

      {filterState.operator === NumericFilterOperator.BETWEEN ? (
        <>
          <div className="flex items-center gap-2 mb-2">
            <input
              type={inputType}
              className="flex-1 bg-[#3c3c3c] text-white p-1 rounded"
              value={filterState.value || ''}
              onChange={(e) => onChange({ ...filterState, value: e.target.value })}
              placeholder="Min value"
            />
            <span>to</span>
            <input
              type={inputType}
              className="flex-1 bg-[#3c3c3c] text-white p-1 rounded"
              value={filterState.secondValue || ''}
              onChange={(e) => onChange({ ...filterState, secondValue: e.target.value })}
              placeholder="Max value"
            />
          </div>
        </>
      ) : (
        <input
          type={inputType}
          className="w-full bg-[#3c3c3c] text-white p-1 rounded mb-2"
          value={filterState.value || ''}
          onChange={(e) => onChange({ ...filterState, value: e.target.value })}
          placeholder={isDateField ? "Select date/time..." : "Enter value..."}
        />
      )}

      <div className="flex justify-between">
        <button
          className="bg-red-700 text-white px-2 py-1 rounded text-xs"
          onClick={() => onChange({ ...filterState, active: false, value: '', secondValue: '' })}
        >
          Clear
        </button>

        <button
          className="bg-[#007acc] text-white px-2 py-1 rounded text-xs"
          onClick={() => {
            onChange({ ...filterState, active: true });
            onClose();
          }}
        >
          Apply
        </button>
      </div>
    </div>
  );
};

// Filter component for boolean fields
const BooleanFilter: React.FC<{
  column: ColumnDef;
  filterState: FilterState;
  onChange: (filterState: FilterState) => void;
  onClose: () => void;
}> = ({ column, filterState, onChange, onClose }) => {
  return (
    <div className="p-2 bg-[#2d2d2d] border border-[#3c3c3c] rounded shadow-lg absolute z-10 w-64 mt-1">
      <div className="flex justify-between items-center mb-2">
        <h3 className="text-sm font-medium">Filter {column.header}</h3>
        <button onClick={onClose} className="text-gray-400 hover:text-white">
          <X size={16} />
        </button>
      </div>

      <div className="flex gap-4 mb-2">
        <label className="flex items-center">
          <input
            type="radio"
            name={`bool-filter-${column.key}`}
            checked={filterState.value === true}
            onChange={() => onChange({ ...filterState, value: true })}
            className="mr-1"
          />
          True
        </label>

        <label className="flex items-center">
          <input
            type="radio"
            name={`bool-filter-${column.key}`}
            checked={filterState.value === false}
            onChange={() => onChange({ ...filterState, value: false })}
            className="mr-1"
          />
          False
        </label>

        <label className="flex items-center">
          <input
            type="radio"
            name={`bool-filter-${column.key}`}
            checked={filterState.value === null || filterState.value === undefined}
            onChange={() => onChange({ ...filterState, value: null })}
            className="mr-1"
          />
          All
        </label>
      </div>

      <div className="flex justify-between">
        <button
          className="bg-red-700 text-white px-2 py-1 rounded text-xs"
          onClick={() => onChange({ ...filterState, active: false, value: null })}
        >
          Clear
        </button>

        <button
          className="bg-[#007acc] text-white px-2 py-1 rounded text-xs"
          onClick={() => {
            onChange({ ...filterState, active: filterState.value !== null });
            onClose();
          }}
        >
          Apply
        </button>
      </div>
    </div>
  );
};

// Column filter component that renders the appropriate filter based on column type
const ColumnFilter: React.FC<{
  column: ColumnDef;
  filterState: FilterState;
  onChange: (filterState: FilterState) => void;
  onClose: () => void;
}> = ({ column, filterState, onChange, onClose }) => {
  if (column.fieldType === 'BOOLEAN') {
    return <BooleanFilter column={column} filterState={filterState} onChange={onChange} onClose={onClose} />;
  }

  if (['NUMBER', 'DATE', 'DATETIME', 'TIME'].includes(column.fieldType)) {
    return <NumericFilter column={column} filterState={filterState} onChange={onChange} onClose={onClose} />;
  }

  return <TextFilter column={column} filterState={filterState} onChange={onChange} onClose={onClose} />;
};

// Pagination component
const Pagination: React.FC<{
  pagination: PaginationState;
  onPageChange: (page: number) => void;
  onPageSizeChange: (pageSize: number) => void;
}> = ({ pagination, onPageChange, onPageSizeChange }) => {
  const { pageIndex, pageSize, totalItems, totalPages } = pagination;

  return (
    <div className="flex flex-wrap items-center justify-between gap-2 px-2 py-1 border-t border-[#3c3c3c]">
      <div className="flex items-center gap-2">
        <span className="text-sm text-gray-400">
          {totalItems > 0 ?
            `Showing ${pageIndex * pageSize + 1}-${Math.min((pageIndex + 1) * pageSize, totalItems)} of ${totalItems}` :
            'No results'
          }
        </span>

        <select
          value={pageSize}
          onChange={(e) => onPageSizeChange(Number(e.target.value))}
          className="bg-[#3c3c3c] text-white text-sm px-2 py-1 rounded"
        >
          {PAGE_SIZE_OPTIONS.map(size => (
            <option key={size} value={size}>
              {size} per page
            </option>
          ))}
        </select>
      </div>

      <div className="flex items-center gap-1">
        <button
          onClick={() => onPageChange(0)}
          disabled={pageIndex === 0}
          className={`p-1 rounded ${pageIndex === 0 ? 'text-gray-600' : 'text-gray-400 hover:bg-[#3c3c3c]'}`}
        >
          <ChevronsLeft size={18} />
        </button>

        <button
          onClick={() => onPageChange(pageIndex - 1)}
          disabled={pageIndex === 0}
          className={`p-1 rounded ${pageIndex === 0 ? 'text-gray-600' : 'text-gray-400 hover:bg-[#3c3c3c]'}`}
        >
          <ChevronLeft size={18} />
        </button>

        <div className="flex items-center">
          <input
            type="number"
            min={1}
            max={totalPages}
            value={pageIndex + 1}
            onChange={(e) => {
              const page = parseInt(e.target.value) - 1;
              if (page >= 0 && page < totalPages) {
                onPageChange(page);
              }
            }}
            className="w-12 bg-[#3c3c3c] text-white text-center py-1 rounded"
          />
          <span className="mx-1 text-sm text-gray-400">of {totalPages}</span>
        </div>

        <button
          onClick={() => onPageChange(pageIndex + 1)}
          disabled={pageIndex >= totalPages - 1}
          className={`p-1 rounded ${pageIndex >= totalPages - 1 ? 'text-gray-600' : 'text-gray-400 hover:bg-[#3c3c3c]'}`}
        >
          <ChevronRight size={18} />
        </button>

        <button
          onClick={() => onPageChange(totalPages - 1)}
          disabled={pageIndex >= totalPages - 1}
          className={`p-1 rounded ${pageIndex >= totalPages - 1 ? 'text-gray-600' : 'text-gray-400 hover:bg-[#3c3c3c]'}`}
        >
          <ChevronsRight size={18} />
        </button>
      </div>
    </div>
  );
};

// Enhanced column definitions
export interface ColumnDef {
  key: string;
  header: string;
  fieldType: string;
  sortable: boolean;
  filterable: boolean;
  render?: (value: any, row: any) => ReactNode;
}

// Action definition interface
export interface ActionDef {
  label: string;
  onClick: (row: any) => void;
  color?: 'blue' | 'red' | 'green' | 'yellow' | 'gray';
  iconLeft?: ReactNode;
  iconRight?: ReactNode;
  showCondition?: (row: any) => boolean;
}

// Updated helper function to generate column definitions with more metadata
const generateColumnsFromFieldMap = (fieldNameMap: Record<string, ColumnInfo>): ColumnDef[] => {
  // Add a check to handle null or undefined fieldNameMap
  if (!fieldNameMap) return [];

  return Object.entries(fieldNameMap).map(([key, columnInfo]) => {
    let renderer: ((value: any, row: any) => ReactNode) | undefined;

    // Format header text with spaces before uppercase letters (except the first letter)
    const headerText = columnInfo.fieldName
      .charAt(0).toUpperCase() + 
      columnInfo.fieldName.slice(1).replace(/([A-Z])/g, ' $1');

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
        <span className={`px-2 py-1 rounded-full text-xs ${value === 'ACTIVE' ? 'bg-green-800 text-green-200' :
          value === 'INACTIVE' ? 'bg-red-800 text-red-200' :
            'bg-gray-800 text-gray-200'
          }`}>
          {value}
        </span>
      );
    }

    return {
      key,
      header: headerText,
      fieldType: columnInfo.fieldType,
      sortable: columnInfo.sortType !== SortType.NONE,
      filterable: true, // Most fields should be filterable
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

interface FilterOption {
  key: string;
  label: string;
  options: { value: string; label: string }[];
}

interface DataTableProps {
  data?: TableFetchResponse | null;
  columns?: ColumnDef[];
  actions?: ActionDef[];
  detailView?: (rowData: TabTableRow) => ReactNode; // Updated type from any to TabTableRow
  detailViewMode?: DetailViewMode;
  entityType: ObjectType;
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
  search?: Record<ObjectType, DataObject>;
}

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
  statusField = 'status',
  search
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
  const [activeFilters, setActiveFilters] = useState<Record<string, FilterState>>({});
  const [openFilterColumn, setOpenFilterColumn] = useState<string | null>(null);
  const [pagination, setPagination] = useState<PaginationState>({
    pageIndex: 0,
    pageSize: 10,
    totalItems: safeInitialData.totalElements,
    totalPages: Math.max(1, safeInitialData.totalPage)
  });
  const [sortField, setSortField] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [isLoading, setIsLoading] = useState(false);

  const defaultFetchData = async (request: TableFetchRequest): Promise<TableFetchResponse> => {
    try {
      // Ensure objectType is always set correctly
      const effectiveObjectType = entityType &&
        (typeof entityType === 'string'
          ? ObjectType[entityType.toUpperCase() as keyof typeof ObjectType]
          : entityType);

      const requestWithObjectType: TableFetchRequest = {
        ...request,
        objectType: request.objectType || effectiveObjectType
      };

      console.log(`Using default fetchTableData for ${entityType} with objectType:`, requestWithObjectType.objectType);

      if (!requestWithObjectType.objectType) {
        console.error("No objectType provided for request:", requestWithObjectType);
        return emptyTableData;
      }

      const response = await fetchTableData(requestWithObjectType);
      return response;
    } catch (error) {
      console.error(`Error fetching ${entityType} data:`, error);
      return emptyTableData;
    }
  };

  const effectiveFetchData = providedFetchData || defaultFetchData;

  const currentRequestRef = useRef<string | null>(null);

  const columnsWithActiveFilters = useMemo(() => {
    return Object.entries(activeFilters).filter(([_, filter]) => filter.active).map(([key]) => key);
  }, [activeFilters]);

  const loadData = useCallback(async (options: {
    pageIndex?: number;
    pageSize?: number;
    sortField?: string | null;
    sortDirection?: 'asc' | 'desc' | null;
    filters?: Record<string, FilterState>;
    searchTerm?: string;
    isInitialLoad?: boolean;
  } = {}) => {
    const {
      pageIndex = pagination.pageIndex,
      pageSize = pagination.pageSize,
      sortField: requestSortField = sortField,
      sortDirection: requestSortDirection = sortDirection,
      filters = activeFilters,
      searchTerm: requestSearchTerm = searchTerm,
      isInitialLoad = false
    } = options;

    if (initialData && !isInitialLoad) {
      return;
    }

    const requestSignature = JSON.stringify({
      pageIndex,
      pageSize,
      sortField: requestSortField,
      sortDirection: requestSortDirection,
      filters,
      searchTerm: requestSearchTerm
    });

    if (requestSignature === currentRequestRef.current) {
      return;
    }

    currentRequestRef.current = requestSignature;
    setIsLoading(true);

    try {
      const apiFilters = Object.entries(filters)
        .filter(([_, filterState]) => filterState.active)
        .map(([field, filterState]) => {
          let filterType: FilterType;
          let minValue: string = '';
          let maxValue: string = '';

          switch (filterState.operator) {
            case TextFilterOperator.EQUALS:
              filterType = FilterType.EQUALS;
              minValue = String(filterState.value || '');
              break;
            case TextFilterOperator.CONTAINS:
              filterType = FilterType.CONTAINS;
              minValue = String(filterState.value || '');
              break;
            case TextFilterOperator.STARTS_WITH:
              filterType = FilterType.STARTS_WITH;
              minValue = String(filterState.value || '');
              break;
            case TextFilterOperator.ENDS_WITH:
              filterType = FilterType.ENDS_WITH;
              minValue = String(filterState.value || '');
              break;
            case NumericFilterOperator.EQUALS:
              filterType = FilterType.EQUALS;
              minValue = String(filterState.value || '');
              break;
            case NumericFilterOperator.GREATER_THAN:
              filterType = FilterType.GREATER_THAN;
              minValue = String(filterState.value || '');
              break;
            case NumericFilterOperator.LESS_THAN:
              filterType = FilterType.LESS_THAN;
              minValue = String(filterState.value || '');
              break;
            case NumericFilterOperator.BETWEEN:
              filterType = FilterType.BETWEEN;
              minValue = String(filterState.value || '');
              maxValue = String(filterState.secondValue || '');
              break;
            default:
              filterType = FilterType.EQUALS;
              minValue = String(filterState.value || '');
          }

          return {
            field,
            filterType,
            minValue,
            maxValue
          };
        });

      
      const request: TableFetchRequest = {
        page: pageIndex,
        size: pageSize,
        sorts: requestSortField ? [
          {
            field: requestSortField,
            sortType: requestSortDirection === 'asc' ? SortType.ASCENDING : SortType.DESCENDING
          }
        ] : [],
        filters: apiFilters,
        search: search || {} as Record<ObjectType, DataObject>,
        objectType: entityType,
        entityName: entityType
      };

      // if (requestSearchTerm) {
      //   const entityObjectType = ObjectType[entityType as keyof typeof ObjectType];

      //   if (request.search[entityObjectType]) {
      //     request.search[entityObjectType] = {
      //       ...request.search[entityObjectType],
      //       data: {
      //         data: {
      //           ...request.search[entityObjectType].data.data,
      //           _search: requestSearchTerm
      //         }
      //       }
      //     };
      //   }
      // }

      const response = await effectiveFetchData(request);

      setData(response);

      setPagination({
        pageIndex: response.currentPage,
        pageSize: response.pageSize,
        totalItems: response.totalElements,
        totalPages: Math.max(1, response.totalPage)
      });
    } catch (error) {
      console.error(`Error fetching data for ${entityType}:`, error);
    } finally {
      setIsLoading(false);
    }
  }, [
    pagination, sortField, sortDirection, activeFilters, searchTerm,
    initialData, effectiveFetchData, entityType, search
  ]);

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
  }, [pagination.pageIndex, pagination.pageSize, sortField, sortDirection, activeFilters, searchTerm, loadData, initialData]);

  const handleFilterChange = (columnKey: string, filterState: FilterState) => {
    setActiveFilters(current => ({
      ...current,
      [columnKey]: filterState
    }));
  };

  const handlePageChange = (pageIndex: number) => {
    setPagination(current => ({
      ...current,
      pageIndex
    }));
  };

  const handlePageSizeChange = (pageSize: number) => {
    setPagination(current => ({
      ...current,
      pageSize,
      pageIndex: 0
    }));
  };

  const actualAddItemButton = typeof addItemButton === 'boolean' && addItemButton === true
    ? {
      label: `Add ${entityType.charAt(0).toUpperCase() + entityType.slice(1).toLowerCase()}`,
      onClick: () => console.log(`Add ${entityType} clicked`)
    }
    : (typeof addItemButton === 'object' ? addItemButton : undefined);

  const defaultDetailView = (rowData: TabTableRow) => {
    const inferredRelatedTables: string[] = [];

    if (data && data.relatedLinkedObjects) {
      Object.keys(data.relatedLinkedObjects).forEach(key => {
        if (key in ObjectType) {
          inferredRelatedTables.push(key);
        }
      });
    }

    // Pass tableInfo from the current data if available
    const tableInfo = data;

    return (
      <EntityDetailTabs
        tableRow={rowData}
        entityType={entityType}
        tableInfo={tableInfo}
        search={search}
      />
    );
  };

  const renderRowDetail = (rowData: TabTableRow) => {
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

          {columnsWithActiveFilters.length > 0 && (
            <div className="flex flex-wrap gap-1 items-center">
              <span className="text-sm text-gray-400">Filters:</span>
              {columnsWithActiveFilters.map(columnKey => {
                const column = effectiveColumns.find(col => col.key === columnKey);
                if (!column) return null;

                return (
                  <div
                    key={columnKey}
                    className="bg-[#3c3c3c] text-white text-xs px-2 py-1 rounded flex items-center gap-1"
                  >
                    <span>{column.header}</span>
                    <button
                      onClick={() => handleFilterChange(columnKey, { ...activeFilters[columnKey], active: false })}
                      className="text-gray-400 hover:text-white"
                    >
                      <X size={12} />
                    </button>
                  </div>
                );
              })}

              <button
                onClick={() => {
                  const updatedFilters = { ...activeFilters };
                  Object.keys(updatedFilters).forEach(key => {
                    updatedFilters[key] = { ...updatedFilters[key], active: false };
                  });
                  setActiveFilters(updatedFilters);
                }}
                className="text-xs text-[#007acc] hover:underline"
              >
                Clear all
              </button>
            </div>
          )}

          {filterOptions && filterOptions.map((filter) => (
            <select
              key={filter.key}
              className="bg-[#3c3c3c] text-white px-3 py-2 rounded focus:outline-none focus:ring-1 focus:ring-[#007acc]"
              value={activeFilters[filter.key]?.value || 'all'}
              onChange={(e) => handleFilterChange(filter.key, {
                field: filter.key,
                operator: TextFilterOperator.EQUALS,
                value: e.target.value,
                active: e.target.value !== 'all'
              })}
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
        <div className="overflow-x-auto">
          <table className="w-full table-fixed min-w-[800px]">
            {effectiveColumns && effectiveColumns.length > 0 && (
              <thead className="bg-[#2d2d2d] text-white font-medium sticky top-0 z-10">
                <tr>
                  {showDetailView && (
                    <th className="w-10 p-3"></th>
                  )}
                  {effectiveColumns.map((column) => (
                    <th
                      key={column.key}
                      className="text-left p-3 relative"
                    >
                      <div className="flex items-start">
                        <button
                          className={`flex-grow flex flex-wrap items-center mr-5 ${column.sortable ? 'cursor-pointer hover:text-[#007acc]' : ''} ${sortField === column.key ? 'text-[#007acc]' : ''}`}
                          onClick={() => column.sortable && handleSortClick(column.key)}
                          disabled={!column.sortable}
                        >
                          <span className="break-words hyphens-auto pr-1">{column.header}</span>
                          {sortField === column.key && (
                            <span className="inline-flex items-center">
                              {sortDirection === 'asc' ?
                                <ChevronUp className="h-4 w-4" /> :
                                <ChevronDown className="h-4 w-4" />
                              }
                            </span>
                          )}
                        </button>
                        {column.filterable && (
                          <button
                            className={`absolute right-3 top-3 p-1 rounded-full hover:bg-[#3c3c3c] ${activeFilters[column.key]?.active ? 'text-[#007acc]' : 'text-gray-400'}`}
                            onClick={(e) => {
                              e.stopPropagation();
                              setOpenFilterColumn(openFilterColumn === column.key ? null : column.key);
                            }}
                          >
                            <Filter size={14} />
                          </button>
                        )}
                      </div>
                      {openFilterColumn === column.key && (
                        <ColumnFilter
                          column={column}
                          filterState={activeFilters[column.key] || {
                            field: column.key,
                            operator: column.fieldType === 'BOOLEAN' ? 'equals' :
                              ['NUMBER', 'DATE', 'DATETIME', 'TIME'].includes(column.fieldType) ?
                                NumericFilterOperator.EQUALS : TextFilterOperator.CONTAINS,
                            value: null,
                            active: false
                          }}
                          onChange={(filterState) => handleFilterChange(column.key, filterState)}
                          onClose={() => setOpenFilterColumn(null)}
                        />
                      )}
                    </th>
                  ))}
                  {actions && actions.length > 0 && (
                    <th className="text-right p-3 whitespace-nowrap">Actions</th>
                  )}
                </tr>
              </thead>
            )}
            <tbody className="divide-y divide-[#3c3c3c]">
              {isLoading ? (
                <tr>
                  <td
                    colSpan={effectiveColumns.length + (showDetailView ? 1 : 0) + (actions ? 1 : 0)}
                    className="p-8 text-center"
                  >
                    <div className="animate-spin h-8 w-8 border-4 border-[#007acc] border-t-transparent rounded-full mx-auto mb-2"></div>
                    <p className="text-gray-400">Loading data...</p>
                  </td>
                </tr>
              ) : hasData ? (
                data.rows.map((row, idx) => (
                  <Fragment key={row.data?.id || Math.random()}>
                    <tr
                      className={`${idx % 2 === 0 ? 'bg-[#1e1e1e]' : 'bg-[#252525]'} ${showDetailView ? 'cursor-pointer hover:bg-[#2a2d2e]' : ''} ${expandedRowId === row.data?.id ? 'bg-[#2a2d2e]' : ''}`}
                      onClick={() => row.data?.id && handleRowClick(row.data.id)}
                    >
                      {showDetailView && (
                        <td className="w-10 p-3">
                          {expandedRowId === row.data?.id ? (
                            <CollapseIcon className="h-4 w-4 text-[#007acc]" />
                          ) : (
                            <ExpandIcon className="h-4 w-4 text-gray-400" />
                          )}
                        </td>
                      )}
                      {effectiveColumns.map((column) => (
                        <td key={column.key} className="p-3 break-words">
                          {column.render
                            ? column.render(row.data[column.key], row.data)
                            : safeRenderValue(row.data[column.key])}
                        </td>
                      ))}
                      {actions && actions.length > 0 && (
                        <td className="p-3 text-right">
                          <div className="flex justify-end space-x-2">
                            {actions.map((action) => (
                              <button
                                key={action.label}
                                className={`flex items-center text-xs px-2 py-1 rounded ${action.color === 'blue' ? 'bg-blue-800 text-blue-100' :
                                  action.color === 'red' ? 'bg-red-800 text-red-100' :
                                    action.color === 'green' ? 'bg-green-800 text-green-100' :
                                      action.color === 'yellow' ? 'bg-yellow-800 text-yellow-100' :
                                        'bg-[#3c3c3c] text-white'
                                  }`}
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
                        </td>
                      )}
                    </tr>
                    {row.data?.id && expandedRowId === row.data.id && (
                      <tr>
                        <td
                          colSpan={effectiveColumns.length + (showDetailView ? 1 : 0) + (actions ? 1 : 0)}
                          className="p-0 bg-[#252525] border-t border-[#3c3c3c]"
                        >
                          <div className="p-4">
                            {renderRowDetail(row as TabTableRow)}
                          </div>
                        </td>
                      </tr>
                    )}
                  </Fragment>
                ))
              ) : (
                <tr>
                  <td
                    colSpan={effectiveColumns.length + (showDetailView ? 1 : 0) + (actions ? 1 : 0)}
                    className="p-8 text-center text-gray-400"
                  >
                    {emptyMessage}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
          {hasData && (
            <div className="border-t border-[#3c3c3c]">
              <Pagination
                pagination={pagination}
                onPageChange={handlePageChange}
                onPageSizeChange={handlePageSizeChange}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
