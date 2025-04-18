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
import { Search, Filter, ChevronDown, ChevronUp, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight, Plus, X, ChevronRight as ExpandIcon, ChevronDown as CollapseIcon, Pencil, Trash2 } from 'lucide-react';
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
  editable?: boolean; // New property to control editability
  hidden?: boolean;   // New property to hide fields from detail view
}

// Action definition interface
export interface ActionDef {
  label: string;
  onClick: (row: any) => void;
  color?: 'blue' | 'red' | 'green' | 'yellow' | 'gray';
  iconLeft?: ReactNode;
  iconRight?: ReactNode;
  showCondition?: (row: any) => boolean;
  showDetail?: boolean; // Add this property to control detail view behavior
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

    // Check if the column is explicitly marked as unsortable
    const isUnsortable = columnInfo.sortType === SortType.UNSORTABLE;

    // Check for audit fields - these should always be sortable unless explicitly marked as unsortable
    const isAuditField = ['createdBy', 'updatedBy', 'createdDate', 'lastModifiedDate'].includes(key);

    // Determine which fields should be hidden from detail view
    const shouldHide = key === 'currentServerTime'; // Hide Current Server Time field

    // Determine if field should be editable in the detail view
    const isEditable = !['id', 'version', 'createdBy', 'updatedBy', 'createdDate', 'lastModifiedDate'].includes(key);

    return {
      key,
      header: headerText,
      fieldType: columnInfo.fieldType,
      sortable: !isUnsortable,
      filterable: true,
      render: renderer,
      editable: isEditable && (columnInfo.editable !== false),
      hidden: shouldHide
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
  showSearchBox?: boolean; // New prop to control search box visibility
  onEdit?: (row: any) => void;
  onDelete?: (row: any) => void;
  showDefaultActions?: boolean;
  onSave?: (row: any, editedData: any) => Promise<boolean>; // New prop for save handler
  onAdd?: () => void; // Add this new prop for handling add operation
}

export default function DataTable({
  data: initialData,
  columns,
  actions: customActions,
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
  search,
  showSearchBox = false,
  onEdit,
  onDelete,
  showDefaultActions = true,
  onSave,
  onAdd // Add new prop here
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

  // Generate a stable instance ID to track component instances
  const instanceId = useRef(`datable-${Math.random().toString(36).substring(2, 9)}`);

  // Use refs for state that shouldn't trigger re-renders
  const dataRef = useRef<TableFetchResponse>(safeInitialData);
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

  const [editingRowId, setEditingRowId] = useState<number | null>(null);
  const [isEditConfirmOpen, setIsEditConfirmOpen] = useState(false);
  const [isSaveConfirmOpen, setIsSaveConfirmOpen] = useState(false);
  const [editedData, setEditedData] = useState<any>(null);
  const [editAction, setEditAction] = useState<'save' | 'cancel' | null>(null);

  // Add state for new row (for add functionality)
  const [isAddingNewRow, setIsAddingNewRow] = useState(false);
  const [newRowData, setNewRowData] = useState<any>(null);

  // Update the showBlockingOverlay logic to reflect when we're in edit mode
  const showBlockingOverlay = editingRowId !== null || isAddingNewRow;

  // Use refs for sort state to prevent multiple renders during initialization
  const sortFieldRef = useRef<string | null>(null);
  const sortDirectionRef = useRef<'asc' | 'desc'>('asc');
  const [sortField, setSortField] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [isLoading, setIsLoading] = useState(false);

  // Track if component is mounted
  const isMountedRef = useRef(false);

  // Track if initial data fetch has happened
  const initialFetchDoneRef = useRef(false);

  // Create ref to track the detail view container for proper focus management
  const detailContainerRef = useRef<HTMLDivElement>(null);

  // Create a ref to store all tabbable elements outside the editing container
  const nonEditableTabbableElementsRef = useRef<HTMLElement[]>([]);

  // Enhance the useEffect for keyboard navigation
  useEffect(() => {
    // Function to make all interactive elements outside the edit area untabbable
    const makeOutsideElementsUntabbable = () => {
      if (editingRowId !== null || isAddingNewRow) {
        const detailContainer = detailContainerRef.current;
        if (!detailContainer) return;

        // Store original tabindex values and set to -1 for elements outside our edit container
        const allTabbableElements = Array.from(
          document.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])')
        ) as HTMLElement[];

        nonEditableTabbableElementsRef.current = allTabbableElements.filter(el => {
          // Skip elements inside our edit container
          if (detailContainer.contains(el)) return false;

          // Remember and update tabindex
          el.dataset.originalTabIndex = el.getAttribute('tabindex') || '';
          el.setAttribute('tabindex', '-1');
          return true;
        });
      }
    };

    // Function to restore original tabindex values when exiting edit mode
    const restoreTabindexValues = () => {
      nonEditableTabbableElementsRef.current.forEach(el => {
        if (el.dataset.originalTabIndex) {
          el.setAttribute('tabindex', el.dataset.originalTabIndex);
        } else {
          el.removeAttribute('tabindex');
        }
      });
      nonEditableTabbableElementsRef.current = [];
    };

    // Handle tab navigation within the form
    const handleTabNavigation = (e: KeyboardEvent) => {
      // Only intercept tab navigation when in edit mode
      if (editingRowId !== null || isAddingNewRow) {
        const detailContainer = detailContainerRef.current;
        if (!detailContainer) return;

        // Find all focusable elements inside the detail container
        const focusableElements = Array.from(detailContainer.querySelectorAll(
          'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        )) as HTMLElement[];

        if (focusableElements.length === 0) return;

        const firstElement = focusableElements[0];
        const lastElement = focusableElements[focusableElements.length - 1];

        // Handle Tab and Shift+Tab to create a focus trap
        if (e.key === 'Tab') {
          if (e.shiftKey && document.activeElement === firstElement) {
            e.preventDefault();
            lastElement.focus();
          } else if (!e.shiftKey && document.activeElement === lastElement) {
            e.preventDefault();
            firstElement.focus();
          }
        }
      }
    };

    // Apply changes when entering edit mode
    if (editingRowId !== null || isAddingNewRow) {
      makeOutsideElementsUntabbable();

      // Focus the first interactive element after making others untabbable
      setTimeout(() => {
        if (detailContainerRef.current) {
          const firstInput = detailContainerRef.current.querySelector(
            'input, select, textarea, button:not([disabled])'
          ) as HTMLElement;

          if (firstInput) {
            firstInput.focus();
          }
        }
      }, 100);
    } else {
      restoreTabindexValues();
    }

    // Add event listener for keyboard navigation
    document.addEventListener('keydown', handleTabNavigation);

    return () => {
      document.removeEventListener('keydown', handleTabNavigation);
      restoreTabindexValues();
    };
  }, [editingRowId, isAddingNewRow]);

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
  const pendingRequestRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const columnsWithActiveFilters = useMemo(() => {
    return Object.entries(activeFilters).filter(([_, filter]) => filter.active).map(([key]) => key);
  }, [activeFilters]);

  const effectiveColumns = useMemo(() => {
    return columns || (data?.fieldNameMap ?
      generateColumnsFromFieldMap(data.fieldNameMap) : []);
  }, [columns, data?.fieldNameMap]);

  const loadData = useCallback(async (options: {
    pageIndex?: number;
    pageSize?: number;
    sortField?: string | null;
    sortDirection?: 'asc' | 'desc' | null;
    filters?: Record<string, FilterState>;
    searchTerm?: string;
    isInitialLoad?: boolean;
    isForceReload?: boolean;
    isSortChange?: boolean;  // New flag for sort changes
  } = {}) => {
    // Cancel any pending requests
    if (pendingRequestRef.current) {
      clearTimeout(pendingRequestRef.current);
      pendingRequestRef.current = null;
    }

    const {
      pageIndex = pagination.pageIndex,
      pageSize = pagination.pageSize,
      sortField: requestSortField = sortFieldRef.current || sortField,
      sortDirection: requestSortDirection = sortDirectionRef.current || sortDirection,
      filters = activeFilters,
      searchTerm: requestSearchTerm = searchTerm,
      isInitialLoad = false,
      isForceReload = false,
      isSortChange = false  // Initialize new flag
    } = options;

    // Modified condition: Also allow API calls for sort changes regardless of initialData
    // Skip if we have initialData and this isn't an explicit reload or sort change
    if (initialData && !isForceReload && !isInitialLoad && !isSortChange) {
      console.log("Using provided initialData, skipping fetch");
      return;
    }

    // Skip if component is not mounted
    if (!isMountedRef.current) {
      console.log("Component not mounted, skipping fetch");
      return;
    }

    // Don't allow duplicate requests
    const requestSignature = JSON.stringify({
      pageIndex,
      pageSize,
      sortField: requestSortField,
      sortDirection: requestSortDirection,
      filters,
      searchTerm: requestSearchTerm
    });

    if (requestSignature === currentRequestRef.current && !isForceReload) {
      console.log(`[${instanceId.current}] Skipping duplicate request:`, requestSignature);
      return;
    }

    // Ensure we only fetch once during initial mount
    if (isInitialLoad && initialFetchDoneRef.current) {
      console.log(`[${instanceId.current}] Initial fetch already done, skipping`);
      return;
    }

    console.log(`[${instanceId.current}] Request will be executed:`, requestSignature);

    // Set request state before async operation to prevent race conditions
    currentRequestRef.current = requestSignature;

    // Don't show loading state for quick refreshes
    if (!isLoading) setIsLoading(true);

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
      if (!isMountedRef.current) {
        console.log(`[${instanceId.current}] Component unmounted during fetch, discarding results`);
        return;
      }

      const response = await effectiveFetchData(request);

      dataRef.current = response;
      setData(response);

      setPagination({
        pageIndex: response.currentPage,
        pageSize: response.pageSize,
        totalItems: response.totalElements,
        totalPages: Math.max(1, response.totalPage)
      });

      if (isInitialLoad) {
        initialFetchDoneRef.current = true;
      }
    } catch (error) {
      console.error(`[${instanceId.current}] Error fetching data for ${entityType}:`, error);
    } finally {
      setIsLoading(false);
    }
  }, [
    pagination, sortField, sortDirection, activeFilters, searchTerm,
    initialData, effectiveFetchData, entityType, search, isLoading
  ]);

  // Handle component mount and unmount
  useEffect(() => {
    isMountedRef.current = true;

    // Initial data load
    if (!initialData) {
      console.log(`[${instanceId.current}] Component mounted, scheduling initial load`);
      // Delay initial fetch slightly to let things settle
      const timeoutId = setTimeout(() => {
        loadData({ isInitialLoad: true });
      }, 50);
      pendingRequestRef.current = timeoutId;
    }

    return () => {
      console.log(`[${instanceId.current}] Component unmounting`);
      isMountedRef.current = false;

      // Clean up any pending requests
      if (pendingRequestRef.current) {
        clearTimeout(pendingRequestRef.current);
      }
    };
  }, [entityType, initialData, loadData]);

  // Handle loading on parameter changes (with debounce)
  useEffect(() => {
    // Skip if we're using provided data
    if (initialData) return;

    // Skip the initial render - we handle that separately
    if (!initialFetchDoneRef.current) return;

    console.log(`[${instanceId.current}] Parameters changed, debouncing reload`);

    // Clear any existing debounce timer
    if (pendingRequestRef.current) {
      clearTimeout(pendingRequestRef.current);
    }

    // Set new debounce timer
    const timeoutId = setTimeout(() => {
      // Update ref values first to ensure consistent state
      sortFieldRef.current = sortField;
      sortDirectionRef.current = sortDirection;
      loadData();
    }, 300);

    pendingRequestRef.current = timeoutId;

    return () => {
      if (pendingRequestRef.current) {
        clearTimeout(pendingRequestRef.current);
      }
    };
  }, [pagination.pageIndex, pagination.pageSize, sortField, sortDirection, activeFilters, searchTerm, loadData, initialData]);

  const handleFilterChange = (columnKey: string, filterState: FilterState) => {
    console.log(`[${instanceId.current}] Filter changed for ${columnKey}:`, filterState);

    // Create updated filters with the new filter state
    const updatedFilters = {
      ...activeFilters,
      [columnKey]: filterState
    };

    // Update the state
    setActiveFilters(updatedFilters);

    // If this is an apply or clear action, immediately reload the data
    if (filterState.active !== activeFilters[columnKey]?.active) {
      console.log(`[${instanceId.current}] Filter ${filterState.active ? 'applied' : 'removed'}, reloading data`);

      // Cancel any pending requests
      if (pendingRequestRef.current) {
        clearTimeout(pendingRequestRef.current);
        pendingRequestRef.current = null;
      }

      // Trigger immediate data reload and force API call for filter changes too
      loadData({
        filters: updatedFilters,
        isSortChange: true // Use the same flag to force API call
      });
    }
  };

  // Update page change handler to trigger immediate data reload
  const handlePageChange = (pageIndex: number) => {
    console.log(`[${instanceId.current}] Page changed to ${pageIndex}`);

    // Update pagination state
    setPagination(current => ({
      ...current,
      pageIndex
    }));

    // Immediately reload data with new page and force API call
    loadData({
      pageIndex,
      isSortChange: true  // Use the same flag to force API call for consistency
    });
  };

  // Update page size change handler to trigger immediate data reload
  const handlePageSizeChange = (pageSize: number) => {
    console.log(`[${instanceId.current}] Page size changed to ${pageSize}`);

    // Update pagination state
    setPagination(current => ({
      ...current,
      pageSize,
      pageIndex: 0
    }));

    // Immediately reload data with new page size and force API call
    loadData({
      pageSize,
      pageIndex: 0,
      isSortChange: true  // Use the same flag to force API call
    });
  };

  // Update search handler to debounce and trigger data reload
  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setSearchTerm(value);

    // Notify parent if callback provided
    if (onSearchChange) {
      onSearchChange(value);
    }

    // Cancel any pending requests to implement debouncing
    if (pendingRequestRef.current) {
      clearTimeout(pendingRequestRef.current);
      pendingRequestRef.current = null;
    }

    // Set new timer for reload with debounce
    const timeoutId = setTimeout(() => {
      console.log(`[${instanceId.current}] Search changed to "${value}", reloading data`);
      loadData({ searchTerm: value });
    }, 500); // Longer debounce for typing

    pendingRequestRef.current = timeoutId;
  };

  // Update sort handler to use refs and immediately trigger data reload with isSortChange flag
  const handleSortClick = (field: string) => {
    console.log(`[${instanceId.current}] Sort column clicked: ${field}`);

    // Determine the new sort direction
    let newDirection: 'asc' | 'desc';

    if (sortField === field) {
      // Toggle direction if same field
      newDirection = sortDirection === 'asc' ? 'desc' : 'asc';
      sortDirectionRef.current = newDirection;
      setSortDirection(newDirection);
    } else {
      // Default to ascending for new field
      newDirection = 'asc';
      sortFieldRef.current = field;
      setSortField(field);
      sortDirectionRef.current = newDirection;
      setSortDirection(newDirection);
    }

    // Notify parent component if callback provided
    if (onSortChange) {
      onSortChange(field, newDirection);
    }

    // Cancel any pending requests
    if (pendingRequestRef.current) {
      clearTimeout(pendingRequestRef.current);
      pendingRequestRef.current = null;
    }

    // Immediately trigger data reload with new sort parameters and isSortChange flag
    console.log(`[${instanceId.current}] Triggering immediate data reload with sort: ${field} ${newDirection}`);
    loadData({
      sortField: field,
      sortDirection: newDirection,
      isSortChange: true  // Add this flag to force API call
    });
  };

  const handleEdit = useCallback((row: any) => {
    console.log(`Edit row with ID: ${row.id}`);
    
    // Expand the row if not already expanded
    if (expandedRowId !== row.id) {
      setExpandedRowId(row.id);
    }
    
    // Set the row as being edited - this triggers the blocking overlay
    setEditingRowId(row.id);
    
    // If custom edit handler provided, call it
    if (onEdit) {
      onEdit(row);
    }
  }, [expandedRowId, onEdit]);

  const handleCancelEdit = useCallback(() => {
    setEditAction('cancel');
    setIsEditConfirmOpen(true);
  }, []);

  const handleSaveEdit = useCallback((rowData: any, data: any) => {
    setEditedData(data);
    setEditAction('save');
    setIsSaveConfirmOpen(true);
  }, []);

  const handleConfirmation = useCallback(async (confirmed: boolean) => {
    if (editAction === 'cancel') {
      setIsEditConfirmOpen(false);
      if (confirmed) {
        setEditingRowId(null);
      }
    } else if (editAction === 'save') {
      setIsSaveConfirmOpen(false);
      if (confirmed && editedData && onSave) {
        const saveResult = await onSave(data.rows.find(r => r.data?.id === editingRowId)?.data, editedData);
        if (saveResult) {
          setEditingRowId(null);
          // Reload data after successful save
          loadData({ isForceReload: true });
        }
      }
    }
  }, [editAction, editedData, editingRowId, onSave, data, loadData]);

  const handleAddNewRow = useCallback(() => {
    // Create empty data structure based on columns
    const emptyData = {} as Record<string, any>;
    effectiveColumns.forEach(col => {
      emptyData[col.key] = null;
    });
    
    // Create a temporary negative ID to identify this as a new row
    const tempNewRowData = { id: -1, ...emptyData };
    setNewRowData(tempNewRowData);
    setIsAddingNewRow(true);
    
    // If custom add handler provided, call it
    if (onAdd) {
      onAdd();
    }
  }, [effectiveColumns, onAdd]);

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

    const isEditing = editingRowId === rowData.data?.id;

    if (detailViewMode === 'custom' && detailView) {
      return detailView(rowData);
    }

    if (detailViewMode === 'tabs' || detailViewMode === 'auto') {
      return (
        <EntityDetailTabs
          tableRow={rowData}
          entityType={entityType}
          tableInfo={data}
          search={search}
          isEditing={isEditing}
          onCancelEdit={handleCancelEdit}
          onSaveEdit={(editedData) => handleSaveEdit(rowData.data, editedData)}
          columns={effectiveColumns} // Pass column definitions with edit/hide info
          excludedStatusOptions={['DELETE']} // Exclude DELETE option from status dropdown
        />
      );
    }

    return null;
  };

  const handleRowClick = (rowId: number, isActionClick: boolean = false) => {
    // If this row is currently being edited, prevent any click action
    if (editingRowId === rowId || isAddingNewRow) {
      return;
    }
    
    if (!showDetailView || (!detailView && detailViewMode === 'custom')) {
      return;
    }

    // Regular click handling for non-edit mode
    if (isActionClick) {
      setExpandedRowId(expandedRowId === rowId ? null : rowId);
      return;
    }

    setExpandedRowId(expandedRowId === rowId ? null : rowId);
  };

  const hasData = data?.rows && data.rows.length > 0;

  // Create default actions for edit and delete
  const defaultActions: ActionDef[] = useMemo(() => {
    if (!showDefaultActions) return [];
    
    const actions: ActionDef[] = [];
    
    // Add edit action with default handler
    actions.push({
      label: "Edit",
      onClick: handleEdit, // Use our enhanced edit handler
      color: "blue",
      iconLeft: <Pencil size={14} />,
      showDetail: true, // Always expand the row when editing
    });
    
    // Add delete action with default handler
    actions.push({
      label: "Delete",
      onClick: onDelete || ((row) => console.log(`Delete row with ID: ${row.id}`)),
      color: "red",
      iconLeft: <Trash2 size={14} />,
    });
    
    return actions;
  }, [handleEdit, onDelete, showDefaultActions]);

  // Combine custom actions with default actions
  const effectiveActions = useMemo(() => {
    // If custom actions are provided, merge them with default actions
    if (customActions && customActions.length > 0) {
      return [...customActions, ...defaultActions];
    }
    
    // If no custom actions but we have default actions, use those
    if (defaultActions.length > 0) {
      return defaultActions;
    }
    
    // Return custom actions as fallback (might be undefined/empty)
    return customActions;
  }, [customActions, defaultActions]);

  return (
    <div className="w-full relative">
      <div className="mb-4 flex flex-wrap justify-between items-center gap-3">
        <div className="flex flex-wrap gap-2">
          {/* Conditionally render search box only if showSearchBox is true */}
          {showSearchBox && (
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
          )}

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

      <div className="bg-[#1e1e1e] border border-[#3c3c3c] rounded-md overflow-hidden relative">
        {/* Add a relative position context for z-index stacking */}
        
        {/* Improved table overlay that covers all inactive elements */}
        {showBlockingOverlay && (
          <div 
            className="absolute inset-0 bg-black bg-opacity-10 z-20"
            aria-hidden="true" 
            style={{ pointerEvents: "all" }}
          />
        )}
        
        <div className="overflow-x-auto">
          <table 
            className="w-full table-fixed min-w-full relative"
            aria-busy={isLoading}
          >
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
                  {/* Action column header with add button */}
                  {showDefaultActions && (
                    <th className="text-right p-3 whitespace-nowrap" style={{ width: '150px' }}>
                      <div className="flex items-center justify-between">
                        <span>Actions</span>
                        <button 
                          onClick={handleAddNewRow}
                          className="flex items-center justify-center p-1 bg-green-700 text-white rounded hover:bg-green-800"
                          title="Add new row"
                          disabled={isAddingNewRow || editingRowId !== null}
                        >
                          <Plus size={16} />
                        </button>
                      </div>
                    </th>
                  )}
                </tr>
              </thead>
            )}
            <tbody className="divide-y divide-[#3c3c3c]">
              {/* New row being added - keep at higher z-index */}
              {isAddingNewRow && newRowData && (
                <tr className="bg-[#2a2d2e] relative z-[60]">
                  {showDetailView && (
                    <td className="w-10 p-3">
                      <CollapseIcon className="h-4 w-4 text-[#007acc]" />
                    </td>
                  )}
                  {effectiveColumns.map((column) => (
                    <td key={column.key} className="p-3 break-words">
                      {column.render
                        ? column.render(newRowData[column.key], newRowData)
                        : safeRenderValue(newRowData[column.key])}
                    </td>
                  ))}
                  {showDefaultActions && (
                    <td className="p-3 text-right">
                      <div className="flex justify-end space-x-2">
                        <button
                          className="flex items-center text-xs px-2 py-1 rounded bg-red-800 text-red-100"
                          onClick={() => setIsAddingNewRow(false)}
                        >
                          <X size={14} className="mr-1" />
                          <span>Cancel</span>
                        </button>
                      </div>
                    </td>
                  )}
                </tr>
              )}
              
              {/* Show detail form for new row with higher z-index */}
              {isAddingNewRow && newRowData && (
                <tr className="relative z-[60]">
                  <td
                    colSpan={effectiveColumns.length + (showDetailView ? 1 : 0) + (showDefaultActions ? 1 : 0)}
                    className="p-0 bg-[#252525] border-t border-[#3c3c3c]"
                  >
                    <div className="p-4 relative" ref={detailContainerRef}>
                      <EntityDetailTabs
                        tableRow={{ data: newRowData } as TabTableRow}
                        entityType={entityType}
                        tableInfo={data}
                        search={search}
                        isEditing={true}
                        isNewRow={true}
                        onCancelEdit={() => setIsAddingNewRow(false)}
                        onSaveEdit={(editedData) => {
                          // Handle saving new row
                          if (onSave) {
                            onSave(null, editedData).then(result => {
                              if (result) {
                                setIsAddingNewRow(false);
                                loadData({ isForceReload: true });
                              }
                            });
                          }
                        }}
                        columns={effectiveColumns} // Pass column definitions with edit/hide info
                        excludedStatusOptions={['DELETE']} // Exclude DELETE option from status dropdown
                      />
                    </div>
                  </td>
                </tr>
              )}
              
              {/* Existing rows */}
              {isLoading ? (
                <tr>
                  <td
                    colSpan={effectiveColumns.length + (showDetailView ? 1 : 0) + (showDefaultActions ? 1 : 0)}
                    className="p-8 text-center"
                  >
                    <div className="animate-spin h-8 w-8 border-4 border-[#007acc] border-t-transparent rounded-full mx-auto mb-2"></div>
                    <p className="text-gray-400">Loading data...</p>
                  </td>
                </tr>
              ) : hasData ? (
                data.rows.map((row, idx) => (
                  <Fragment key={row.data?.id || Math.random()}>
                    {/* Parent row with modified interaction handling */}
                    <tr
                      className={`${idx % 2 === 0 ? 'bg-[#1e1e1e]' : 'bg-[#252525]'} 
                        ${showDetailView ? 'cursor-pointer hover:bg-[#2a2d2e]' : ''} 
                        ${expandedRowId === row.data?.id ? 'bg-[#2a2d2e]' : ''}
                        ${editingRowId === row.data?.id ? 'relative z-[60]' : ''}`}
                      onClick={() => row.data?.id && handleRowClick(row.data.id)}
                    >
                      {/* Individual row overlay to prevent interactions when editing */}
                      {editingRowId === row.data?.id && (
                        <td 
                          className="absolute inset-0 bg-transparent z-[55]" 
                          colSpan={1}
                          onClick={(e) => {
                            e.preventDefault();
                            e.stopPropagation();
                          }}
                          style={{ pointerEvents: 'all' }}
                        />
                      )}
                      
                      {/* Row content - unchanged */}
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
                      {/* Always show the Actions cell when showDefaultActions is true */}
                      {showDefaultActions && (
                        <td className="p-3 text-right">
                          <div className="flex justify-end space-x-2">
                            {effectiveActions && effectiveActions.map((action) => (
                              <button
                                key={action.label}
                                className={`flex items-center text-xs px-2 py-1 rounded ${
                                  action.color === 'blue' ? 'bg-blue-800 text-blue-100' :
                                  action.color === 'red' ? 'bg-red-800 text-red-100' :
                                  action.color === 'green' ? 'bg-green-800 text-green-100' :
                                  action.color === 'yellow' ? 'bg-yellow-800 text-yellow-100' :
                                  'bg-[#3c3c3c] text-white'
                                }`}
                                onClick={(e) => {
                                  e.stopPropagation(); // Prevent row click
                                  
                                  // Don't allow action clicks if we're already editing
                                  if (editingRowId !== null || isAddingNewRow) {
                                    return;
                                  }
                                  
                                  action.onClick(row.data);
                                  
                                  if (row.data?.id && action.showDetail) {
                                    handleRowClick(row.data.id, true);
                                  }
                                }}
                                // Disable the button if any row is in edit mode
                                disabled={editingRowId !== null || isAddingNewRow}
                              >
                                {action.iconLeft}
                                <span className="mx-1">{action.label}</span>
                                {action.iconRight}
                              </button>
                            ))}
                          </div>
                        </td>
                      )}
                    </tr>
                    
                    {/* Detail row for expanded items */}
                    {row.data?.id && expandedRowId === row.data.id && (
                      <tr className={editingRowId === row.data?.id ? 'relative z-[60]' : ''}>
                        <td
                          colSpan={effectiveColumns.length + (showDetailView ? 1 : 0) + (showDefaultActions ? 1 : 0)}
                          className="p-0 bg-[#252525] border-t border-[#3c3c3c]"
                        >
                          <div 
                            className="p-4 relative" 
                            ref={editingRowId === row.data?.id ? detailContainerRef : null}
                          >
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
                    colSpan={effectiveColumns.length + (showDetailView ? 1 : 0) + (showDefaultActions ? 1 : 0)}
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

      {/* Improved blocking overlay with better z-indexing */}
      {showBlockingOverlay && (
        <>
          {/* Global overlay */}
          <div 
            className="fixed inset-0 right-[17px] bg-black bg-opacity-40 z-[50]" 
            style={{ pointerEvents: "all" }}
            onClick={(e) => e.stopPropagation()}
            aria-hidden="true"
          />
          
          {/* Focus guard */}
          <div 
            className="sr-only" 
            tabIndex={0}
            aria-hidden="true"
            onFocus={() => {
              if (detailContainerRef.current) {
                const firstInput = detailContainerRef.current.querySelector(
                  'input, select, textarea, button'
                ) as HTMLElement;
                if (firstInput) firstInput.focus();
              }
            }}
          />
        </>
      )}

      {/* Confirmation dialog for canceling edit */}
      {isEditConfirmOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center">
          <div className="bg-[#2d2d2d] p-6 rounded-lg shadow-lg max-w-md w-full">
            <h3 className="text-lg font-medium mb-4">Confirm Cancel</h3>
            <p className="mb-6">Are you sure you want to cancel? All unsaved changes will be lost.</p>
            <div className="flex justify-end space-x-3">
              <button 
                className="px-4 py-2 bg-[#3c3c3c] text-white rounded hover:bg-[#4c4c4c]"
                onClick={() => handleConfirmation(false)}
              >
                No, continue editing
              </button>
              <button 
                className="px-4 py-2 bg-red-700 text-white rounded hover:bg-red-800"
                onClick={() => handleConfirmation(true)}
              >
                Yes, discard changes
              </button>
            </div>
          </div>
        </div>
      )}
      
      {/* Confirmation dialog for saving edit */}
      {isSaveConfirmOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center">
          <div className="bg-[#2d2d2d] p-6 rounded-lg shadow-lg max-w-md w-full">
            <h3 className="text-lg font-medium mb-4">Confirm Save</h3>
            <p className="mb-6">Are you sure you want to save these changes?</p>
            <div className="flex justify-end space-x-3">
              <button 
                className="px-4 py-2 bg-[#3c3c3c] text-white rounded hover:bg-[#4c4c4c]"
                onClick={() => handleConfirmation(false)}
              >
                No, continue editing
              </button>
              <button 
                className="px-4 py-2 bg-green-700 text-white rounded hover:bg-green-800"
                onClick={() => handleConfirmation(true)}
              >
                Yes, save changes
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
