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
import { 
  addRecord, 
  updateRecord, 
  deleteRecord, 
  exportTableData 
} from '@/app/lib/api/tableActionService';

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
    <div className="p-2 bg-[#2d2d2d] border border-[#3c3c3d] rounded shadow-lg absolute z-[200] w-64 mt-1">
      <div className="flex justify-between items-center mb-2">
        <h3 className="text-sm font-medium">Filter {column.header}</h3>
        <button onClick={onClose} className="text-gray-400 hover:text-white" aria-label="Close filter">
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
    <div className="p-2 bg-[#2d2d2d] border border-[#3c3c3c] rounded shadow-lg absolute z-[200] w-64 mt-1">
      <div className="flex justify-between items-center mb-2">
        <h3 className="text-sm font-medium">Filter {column.header}</h3>
        <button onClick={onClose} className="text-gray-400 hover:text-white" aria-label="Close filter">
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
    <div className="p-2 bg-[#2d2d2d] border border-[#3c3c3c] rounded shadow-lg absolute z-[200] w-64 mt-1">
      <div className="flex justify-between items-center mb-2">
        <h3 className="text-sm font-medium">Filter {column.header}</h3>
        <button onClick={onClose} className="text-gray-400 hover:text-white" aria-label="Close filter">
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
          aria-label="Items per page"
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
          aria-label="Go to first page"
        >
          <ChevronsLeft size={18} />
        </button>

        <button
          onClick={() => onPageChange(pageIndex - 1)}
          disabled={pageIndex === 0}
          className={`p-1 rounded ${pageIndex === 0 ? 'text-gray-600' : 'text-gray-400 hover:bg-[#3c3c3c]'}`}
          aria-label="Go to previous page"
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
            aria-label="Current page"
          />
          <span className="mx-1 text-sm text-gray-400">of {totalPages}</span>
        </div>

        <button
          onClick={() => onPageChange(pageIndex + 1)}
          disabled={pageIndex >= totalPages - 1}
          className={`p-1 rounded ${pageIndex >= totalPages - 1 ? 'text-gray-600' : 'text-gray-400 hover:bg-[#3c3c3c]'}`}
          aria-label="Go to next page"
        >
          <ChevronRight size={18} />
        </button>

        <button
          onClick={() => onPageChange(totalPages - 1)}
          disabled={pageIndex >= totalPages - 1}
          className={`p-1 rounded ${pageIndex >= totalPages - 1 ? 'text-gray-600' : 'text-gray-400 hover:bg-[#3c3c3c]'}`}
          aria-label="Go to last page"
        >
          <ChevronsRight size={18} />
        </button>
      </div>
    </div>
  );
};

// Enhanced column definitions - update to use full TableRow instead of just its data property
export interface ColumnDef {
  key: string;
  header: string;
  fieldType: string;
  sortable: boolean;
  filterable: boolean;
  render?: (value: any, row: TableRow) => ReactNode;
  editable?: boolean;
  hidden?: boolean;
}

// Action definition interface - update to use full TableRow
export interface ActionDef {
  label: string;
  onClick: (row: TableRow) => void;
  color?: 'blue' | 'red' | 'green' | 'yellow' | 'gray';
  iconLeft?: ReactNode;
  iconRight?: ReactNode;
  showCondition?: (row: TableRow) => boolean;
  showDetail?: boolean;
  isTableAction?: boolean;
}

// Helper function to generate column definitions
const generateColumnsFromFieldMap = (fieldNameMap: Record<string, ColumnInfo>): ColumnDef[] => {
  if (!fieldNameMap) return [];

  return Object.entries(fieldNameMap).map(([key, columnInfo]) => {
    let renderer: ((value: any, row: TableRow) => ReactNode) | undefined;

    const headerText = columnInfo.fieldName
      .charAt(0).toUpperCase() +
      columnInfo.fieldName.slice(1).replace(/([A-Z])/g, ' $1');

    if (columnInfo.fieldType === 'BOOLEAN') {
      renderer = (value) =>
        value === true ? '✓' :
          value === false ? '✗' :
            '-';
    }

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

    const isIdField = key === 'id';
    const isViewIdField = key === 'viewId';
    const isUnsortable = columnInfo.sortType === SortType.UNSORTABLE && !isViewIdField;
    const isAuditField = ['createdBy', 'updatedBy', 'createdDate', 'lastModifiedDate'].includes(key);
    const shouldHide = isIdField || key === 'currentServerTime';
    const isEditable = !['id', 'viewId', 'version', 'createdBy', 'updatedBy', 'createdDate', 'lastModifiedDate'].includes(key);

    return {
      key,
      header: key === 'viewId' ? 'ID' : headerText,
      fieldType: columnInfo.fieldType,
      sortable: isViewIdField ? true : !isUnsortable,
      filterable: true,
      render: renderer,
      editable: isEditable && (columnInfo.editable !== false),
      hidden: shouldHide
    };
  }).sort((a, b) => {
    if (a.key === 'viewId') return -1;
    if (b.key === 'viewId') return 1;
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
    if (value instanceof Date) {
      return value.toLocaleString();
    }

    if (typeof value === 'string' && value.match(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/)) {
      return new Date(value).toLocaleString();
    }

    if (value?.id && value?.name) {
      return value.name;
    }

    if (value?.token && value?.tokenType) {
      return `${value.tokenType}: ${value.token.substring(0, 10)}...`;
    }

    return '{Object}';
  }

  if (typeof value === 'boolean') {
    return value ? '✓' : '✗';
  }

  return String(value);
};

// Detail view modes
type DetailViewMode = 'custom' | 'auto' | 'tabs';

// Entity API endpoints
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
  detailView?: (rowData: TabTableRow) => ReactNode;
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
  showSearchBox?: boolean;
  onEdit?: (row: TableRow) => void;
  onDelete?: (row: TableRow) => void;
  showDefaultActions?: boolean;
  onSave?: (row: TableRow | null, editedData: TableRow) => Promise<boolean>;
  onAdd?: () => void;
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
  onAdd
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

  const instanceId = useRef(`datatable-${Math.random().toString(36).substring(2, 9)}`);

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
  const [isDeleteConfirmOpen, setIsDeleteConfirmOpen] = useState(false);
  const [rowToDelete, setRowToDelete] = useState<TableRow | null>(null);
  const [editedData, setEditedData] = useState<TableRow | null>(null);
  const [editAction, setEditAction] = useState<'save' | 'cancel' | 'delete' | null>(null);

  const [isAddingNewRow, setIsAddingNewRow] = useState(false);
  const [newRowData, setNewRowData] = useState<TableRow | null>(null);

  const showBlockingOverlay = editingRowId !== null || isAddingNewRow;

  const sortFieldRef = useRef<string | null>(null);
  const sortDirectionRef = useRef<'asc' | 'desc'>('asc');
  const [sortField, setSortField] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [isLoading, setIsLoading] = useState(false);

  const isMountedRef = useRef(false);
  const initialFetchDoneRef = useRef(false);
  const detailContainerRef = useRef<HTMLDivElement>(null);
  const nonEditableTabbableElementsRef = useRef<HTMLElement[]>([]);

  useEffect(() => {
    const makeOutsideElementsUntabbable = () => {
      if (showBlockingOverlay) {
        const detailContainer = detailContainerRef.current;
        if (!detailContainer) return;

        const allTabbableElements = Array.from(
          document.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])')
        ) as HTMLElement[];

        nonEditableTabbableElementsRef.current = allTabbableElements.filter(el => {
          if (detailContainer.contains(el)) return false;
          el.dataset.originalTabIndex = el.getAttribute('tabindex') || '';
          el.setAttribute('tabindex', '-1');
          return true;
        });
      }
    };

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

    const handleTabNavigation = (e: KeyboardEvent) => {
      if (showBlockingOverlay) {
        const detailContainer = detailContainerRef.current;
        if (!detailContainer) return;

        const focusableElements = Array.from(detailContainer.querySelectorAll(
          'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        )) as HTMLElement[];

        if (focusableElements.length === 0) return;

        const firstElement = focusableElements[0];
        const lastElement = focusableElements[focusableElements.length - 1];

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

    if (showBlockingOverlay) {
      makeOutsideElementsUntabbable();
      setTimeout(() => {
        if (detailContainerRef.current) {
          const firstInput = detailContainerRef.current.querySelector(
            'input, select, textarea, button:not([disabled])'
          ) as HTMLElement;
          if (firstInput) firstInput.focus();
        }
      }, 100);
    } else {
      restoreTabindexValues();
    }

    document.addEventListener('keydown', handleTabNavigation);

    return () => {
      document.removeEventListener('keydown', handleTabNavigation);
      restoreTabindexValues();
    };
  }, [showBlockingOverlay]);

  const defaultFetchData = async (request: TableFetchRequest): Promise<TableFetchResponse> => {
    try {
      const effectiveObjectType = entityType &&
        (typeof entityType === 'string'
          ? ObjectType[entityType.toUpperCase() as keyof typeof ObjectType]
          : entityType);

      const requestWithObjectType: TableFetchRequest = {
        ...request,
        objectType: request.objectType || effectiveObjectType
      };

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
    const cols = columns || (data?.fieldNameMap ? 
      generateColumnsFromFieldMap(data.fieldNameMap) : []);
    return cols.filter(col => !col.hidden);
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
    isSortChange?: boolean;
  } = {}) => {
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
      isSortChange = false
    } = options;

    if (initialData && !isForceReload && !isInitialLoad && !isSortChange) {
      return;
    }

    if (!isMountedRef.current) {
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

    if (requestSignature === currentRequestRef.current && !isForceReload) {
      return;
    }

    if (isInitialLoad && initialFetchDoneRef.current) {
      return;
    }

    currentRequestRef.current = requestSignature;

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
      console.error(`Error fetching data for ${entityType}:`, error);
    } finally {
      setIsLoading(false);
    }
  }, [
    pagination, sortField, sortDirection, activeFilters, searchTerm,
    initialData, effectiveFetchData, entityType, search, isLoading
  ]);

  useEffect(() => {
    isMountedRef.current = true;

    if (!initialData) {
      const timeoutId = setTimeout(() => {
        loadData({ isInitialLoad: true });
      }, 50);
      pendingRequestRef.current = timeoutId;
    }

    return () => {
      isMountedRef.current = false;
      if (pendingRequestRef.current) {
        clearTimeout(pendingRequestRef.current);
      }
    };
  }, [entityType, initialData, loadData]);

  useEffect(() => {
    if (initialData || !initialFetchDoneRef.current) return;

    if (pendingRequestRef.current) {
      clearTimeout(pendingRequestRef.current);
    }

    const timeoutId = setTimeout(() => {
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
    const updatedFilters = {
      ...activeFilters,
      [columnKey]: filterState
    };

    setActiveFilters(updatedFilters);

    // Check if active state changed OR filter value changed
    const currentFilter = activeFilters[columnKey];
    const valueChanged = 
      currentFilter?.value !== filterState.value || 
      currentFilter?.secondValue !== filterState.secondValue;
    const activeStateChanged = filterState.active !== currentFilter?.active;
    
    if (activeStateChanged || (filterState.active && valueChanged)) {
      if (pendingRequestRef.current) {
        clearTimeout(pendingRequestRef.current);
        pendingRequestRef.current = null;
      }

      loadData({
        filters: updatedFilters,
        isSortChange: true
      });
    }
  };

  const handlePageChange = (pageIndex: number) => {
    setPagination(current => ({
      ...current,
      pageIndex
    }));

    loadData({
      pageIndex,
      isSortChange: true
    });
  };

  const handlePageSizeChange = (pageSize: number) => {
    setPagination(current => ({
      ...current,
      pageSize,
      pageIndex: 0
    }));

    loadData({
      pageSize,
      pageIndex: 0,
      isSortChange: true
    });
  };

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setSearchTerm(value);

    if (onSearchChange) {
      onSearchChange(value);
    }

    if (pendingRequestRef.current) {
      clearTimeout(pendingRequestRef.current);
      pendingRequestRef.current = null;
    }

    const timeoutId = setTimeout(() => {
      loadData({ searchTerm: value });
    }, 500);

    pendingRequestRef.current = timeoutId;
  };

  const handleSortClick = (field: string) => {
    let newDirection: 'asc' | 'desc';

    if (sortField === field) {
      newDirection = sortDirection === 'asc' ? 'desc' : 'asc';
      sortDirectionRef.current = newDirection;
      setSortDirection(newDirection);
    } else {
      newDirection = 'asc';
      sortFieldRef.current = field;
      setSortField(field);
      sortDirectionRef.current = newDirection;
      setSortDirection(newDirection);
    }

    if (onSortChange) {
      onSortChange(field, newDirection);
    }

    if (pendingRequestRef.current) {
      clearTimeout(pendingRequestRef.current);
      pendingRequestRef.current = null;
    }

    loadData({
      sortField: field,
      sortDirection: newDirection,
      isSortChange: true
    });
  };

  const handleEdit = useCallback((row: TableRow) => {
    if (row.data?.viewId && expandedRowId !== row.data?.viewId) {
      setExpandedRowId(row.data?.viewId);
    }

    setEditingRowId(row.data?.viewId || null);

    if (onEdit) {
      onEdit(row);
    }
  }, [expandedRowId, onEdit]);

  const handleCancelEdit = useCallback(() => {
    setEditAction('cancel');
    setIsEditConfirmOpen(true);
  }, []);

  const handleSaveEdit = useCallback((rowData: TableRow, data: TableRow) => {
    setEditedData(data);
    setEditAction('save');
    setTimeout(() => {
      setIsSaveConfirmOpen(true);
    }, 0);
  }, []);

  const handleConfirmation = useCallback(async (confirmed: boolean) => {
    if (editAction === 'cancel') {
      setIsEditConfirmOpen(false);
      if (confirmed) {
        setEditingRowId(null);
        setIsAddingNewRow(false);
      }
    } else if (editAction === 'save') {
      setIsSaveConfirmOpen(false);

      if (confirmed && editedData) {
        try {
          const isNewRecord = isAddingNewRow || (editedData.data?.id === undefined || editedData.data?.id < 0);

          if (onSave) {
            // const rowData = editingRowId !== null
            //   ? data.rows.find(row => row.data?.viewId === editingRowId)?.data || null
            //   : null;

            const saveResult = await onSave(
              null,
              editedData
            );

            if (saveResult) {
              setEditingRowId(null);
              setIsAddingNewRow(false);
              loadData({ isForceReload: true });
            } else {
              console.error('Save operation returned false');
            }
          } else {
            const tableRowData: TableRow = { data: editedData };
            const result = isNewRecord
              ? await addRecord(entityType as ObjectType, tableRowData, data)
              : await updateRecord(entityType as ObjectType, tableRowData, data);

            if (result.success) {
              setEditingRowId(null);
              setIsAddingNewRow(false);
              loadData({ isForceReload: true });
            } else {
              console.error('Error saving record:', result.message);
            }
          }
        } catch (error) {
          console.error('Error during save operation:', error);
        }
      }
    } else if (editAction === 'delete') {
      setIsDeleteConfirmOpen(false);

      if (confirmed && rowToDelete) {
        try {
          if (onDelete) {
            await onDelete(rowToDelete);
          } else {
            // rowToDelete is already a TableRow object
            const result = await deleteRecord(entityType as ObjectType, rowToDelete, data);

            if (result.success) {
              loadData({ isForceReload: true });
            } else {
              console.error('Error deleting record:', result.message);
            }
          }
        } catch (error) {
          console.error('Error during delete operation:', error);
        }
      }
    }
  }, [editAction, editedData, editingRowId, isAddingNewRow, loadData, onSave, entityType, data, rowToDelete, onDelete]);

  const handleAddNewRow = useCallback(() => {
    const emptyData = {} as TableRow['data'];
    effectiveColumns.forEach(col => {
      emptyData[col.key] = null;
    });

    const tempNewRowData: TableRow = {'data':{ id: -1, ...emptyData }};
    setNewRowData(tempNewRowData);
    setIsAddingNewRow(true);

    if (onAdd) {
      onAdd();
    }
  }, [effectiveColumns, onAdd]);

  const handleDeleteClick = useCallback((row: TableRow['data']) => {
    setRowToDelete({ data: row }); // Create a TableRow object
    setEditAction('delete');
    setIsDeleteConfirmOpen(true);
  }, []);

  const handleRowClick = useCallback((rowViewId: number | undefined, isActionClick: boolean = false) => {
    if (!rowViewId || editingRowId === rowViewId || isAddingNewRow) {
      return;
    }

    if (!showDetailView || (!detailView && detailViewMode === 'custom')) {
      return;
    }

    setExpandedRowId(expandedRowId === rowViewId ? null : rowViewId);
  }, [expandedRowId, editingRowId, isAddingNewRow, showDetailView, detailView, detailViewMode]);

  const isDefinedNumber = (value: number | undefined | null): value is number => {
    return value !== undefined && value !== null;
  };

  const actualAddItemButton = typeof addItemButton === 'boolean' && addItemButton === true
    ? {
        label: `Add ${entityType.charAt(0).toUpperCase() + entityType.slice(1).toLowerCase()}`,
        onClick: handleAddNewRow
      }
    : (typeof addItemButton === 'object' ? addItemButton : undefined);

  const defaultActions: ActionDef[] = useMemo(() => {
    if (!showDefaultActions) return [];

    const actions: ActionDef[] = [];

    actions.push({
      label: "Edit",
      onClick: handleEdit,
      color: "blue",
      iconLeft: <Pencil size={14} />,
      showDetail: true,
    });

    actions.push({
      label: "Delete",
      onClick: handleDeleteClick,
      color: "red",
      iconLeft: <Trash2 size={14} />,
    });

    return actions;
  }, [handleEdit, handleDeleteClick, showDefaultActions]);

  const effectiveActions = useMemo(() => {
    return (customActions || []).filter(action => !action.isTableAction)
      .concat(defaultActions);
  }, [customActions, defaultActions]);

  const tableActions = useMemo(() => {
    return (customActions || []).filter(action => action.isTableAction);
  }, [customActions]);

  const handleExportTable = useCallback(async () => {
    try {
      setIsLoading(true);
      const exportData = { ...data, noReload: true };
      await exportTableData(entityType as ObjectType, exportData);
    } catch (error) {
      console.error('Export error:', error);
    } finally {
      setIsLoading(false);
    }
  }, [entityType, data]);

  const executeTableAction = useCallback((action: ActionDef) => {
    if (action.label === "Export") {
      handleExportTable();
    } else {
      action.onClick(null as any); // Type safety to be improved later
    }
  }, [handleExportTable]);

  const hasData = data.rows && data.rows.length > 0;

  const renderRowDetail = useCallback((row: TabTableRow) => {
    if (editingRowId === row.data?.viewId) {
      return (
        <EntityDetailTabs
          tableRow={row}
          entityType={entityType}
          tableInfo={data}
          search={search}
          isEditing={true}
          onCancelEdit={handleCancelEdit}
          onSaveEdit={(editedData) => handleSaveEdit(row, editedData)}
          columns={effectiveColumns}
        />
      );
    }

    if (detailView && detailViewMode === 'custom') {
      return detailView(row);
    }

    return (
      <EntityDetailTabs
        tableRow={row}
        entityType={entityType}
        tableInfo={data}
        search={search}
        isEditing={false}
        columns={effectiveColumns}
      />
    );
  }, [
    data, detailView, detailViewMode, editingRowId, effectiveColumns, entityType, handleCancelEdit, handleSaveEdit, search
  ]);

  return (
    <div className="w-full relative flex flex-col">
      <div className="mb-4 flex flex-wrap justify-between items-center gap-3 w-full">
        <div className="flex flex-wrap gap-2">
          {showSearchBox && (
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-500" />
              <input
                type="text"
                placeholder="Search..."
                className="pl-9 py-2 pr-4 bg-[#3c3c3c] rounded focus:outline-none focus:ring-1 focus:ring-[#007acc]"
                value={searchTerm}
                onChange={handleSearchChange}
                aria-label="Search table"
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
                      aria-label={`Remove filter for ${column.header}`}
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
                aria-label="Clear all filters"
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
              aria-label={`Filter by ${filter.label}`}
            >
              {filter.options.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          ))}
        </div>

        <div className="flex items-center gap-2">
          {tableActions.length > 0 && (
            <div className="flex gap-2">
              {tableActions.map((action) => (
                <button
                  key={action.label}
                  className={`flex items-center text-xs px-3 py-2 rounded ${
                    action.color === 'blue' ? 'bg-blue-700 text-blue-100' :
                    action.color === 'red' ? 'bg-red-700 text-red-100' :
                    action.color === 'green' ? 'bg-green-700 text-green-100' :
                    action.color === 'yellow' ? 'bg-yellow-700 text-yellow-100' :
                    'bg-[#3c3c3c] text-white'
                  } hover:opacity-90`}
                  onClick={() => executeTableAction(action)}
                  disabled={isLoading}
                  aria-label={action.label}
                >
                  {action.iconLeft}
                  <span className="mx-1">{action.label}</span>
                  {action.iconRight}
                </button>
              ))}
            </div>
          )}

          {actualAddItemButton && (
            <button
              className="bg-[#007acc] text-white px-3 py-2 rounded hover:bg-[#0069ac] flex items-center"
              onClick={actualAddItemButton.onClick}
              aria-label={actualAddItemButton.label}
            >
              <Plus className="h-4 w-4 mr-2" />
              {actualAddItemButton.label}
            </button>
          )}
        </div>
      </div>

      <div className="bg-[#1e1e1e] border border-[#3c3c3c] rounded-md overflow-hidden relative w-full flex-grow flex flex-col">
        <div className="overflow-x-auto w-full flex-1">
          <table
            className="w-full relative table-auto"
            aria-busy={isLoading}
            role="grid"
          >
            {effectiveColumns && effectiveColumns.length > 0 && (
              <thead className="bg-[#2d2d2d] text-white font-medium sticky top-0 z-[10]">
                <tr>
                  {showDetailView && (
                    <th className="w-10 p-3 whitespace-nowrap" scope="col"></th>
                  )}
                  {effectiveColumns.map((column) => (
                    <th
                      key={column.key}
                      className="text-left p-3 relative"
                      scope="col"
                      style={{ width: column.key === 'viewId' ? '80px' : 'auto' }}
                    >
                      <div className="flex items-start">
                        <button
                          className={`flex-grow flex flex-wrap items-center mr-5 ${column.sortable ? 'cursor-pointer hover:text-[#007acc]' : ''} ${sortField === column.key ? 'text-[#007acc]' : ''}`}
                          onClick={() => column.sortable && handleSortClick(column.key)}
                          disabled={!column.sortable}
                          aria-sort={sortField === column.key ? (sortDirection === 'asc' ? 'ascending' : 'descending') : 'none'}
                          aria-label={`Sort by ${column.header}`}
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
                            aria-label={`Filter by ${column.header}`}
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
                  {showDefaultActions && (
                    <th className="text-right p-3 whitespace-nowrap" scope="col" style={{ width: '150px' }}>
                      <div className="flex items-center justify-between">
                        <span>Actions</span>
                        <button
                          onClick={handleAddNewRow}
                          className="flex items-center justify-center p-1 bg-green-700 text-white rounded hover:bg-green-800"
                          title="Add new row"
                          disabled={isAddingNewRow || editingRowId !== null}
                          aria-label="Add new row"
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
              {isAddingNewRow && newRowData && (
                <tr className="bg-[#2a2d2e] relative z-[200]">
                  {showDetailView && (
                    <td className="w-10 p-3">
                      <CollapseIcon className="h-4 w-4 text-[#007acc]" />
                    </td>
                  )}
                  {effectiveColumns.map((column) => (
                    <td key={column.key} className="p-3 break-words">
                      {column.render
                        ? column.render(newRowData.data?.column.key, newRowData)
                        : safeRenderValue(newRowData.data?.column.key)}
                    </td>
                  ))}
                  {showDefaultActions && (
                    <td className="p-3 text-right">
                      <div className="flex justify-end space-x-2">
                        <button
                          className="flex items-center text-xs px-2 py-1 rounded bg-red-800 text-red-100"
                          onClick={() => setIsAddingNewRow(false)}
                          aria-label="Cancel adding new row"
                        >
                          <X size={14} className="mr-1" />
                          <span>Cancel</span>
                        </button>
                      </div>
                    </td>
                  )}
                </tr>
              )}

              {isAddingNewRow && newRowData && (
                <tr className="relative z-[200]">
                  <td
                    colSpan={effectiveColumns.length + (showDetailView ? 1 : 0) + (showDefaultActions ? 1 : 0)}
                    className="p-0 bg-[#252525] border-t border-[#3c3c3c]"
                  >
                    <div className="p-4 relative" ref={detailContainerRef}>
                      <EntityDetailTabs
                        tableRow={newRowData as TabTableRow}
                        entityType={entityType}
                        tableInfo={data}
                        search={search}
                        isEditing={true}
                        isNewRow={true}
                        onCancelEdit={() => setIsAddingNewRow(false)}
                        onSaveEdit={(editedData) => {
                          if (onSave) {
                            onSave(null, editedData).then(result => {
                              if (result) {
                                setIsAddingNewRow(false);
                                loadData({ isForceReload: true });
                              }
                            });
                          }
                        }}
                        columns={effectiveColumns}
                        excludedStatusOptions={['DELETE']}
                      />
                    </div>
                  </td>
                </tr>
              )}

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
                  <Fragment key={row.data?.viewId || `row_${idx}`}>
                    <tr
                      className={`${idx % 2 === 0 ? 'bg-[#1e1e1e]' : 'bg-[#252525]'} 
                        ${showDetailView ? 'cursor-pointer hover:bg-[#2a2d2e]' : ''} 
                        ${expandedRowId === row.data?.viewId ? 'bg-[#2a2d2e]' : ''}
                        ${editingRowId === row.data?.viewId ? 'relative z-[200]' : ''}`}
                      onClick={() => handleRowClick(row.data?.viewId)}
                      role="row"
                    >
                      {editingRowId === row.data?.viewId && (
                        <td
                          className="absolute inset-0 bg-transparent z-[150]"
                          colSpan={1}
                          onClick={(e) => {
                            e.preventDefault();
                            e.stopPropagation();
                          }}
                          style={{ pointerEvents: 'all' }}
                        />
                      )}

                      {showDetailView && (
                        <td className="w-10 p-3">
                          {expandedRowId === row.data?.viewId ? (
                            <CollapseIcon className="h-4 w-4 text-[#007acc]" />
                          ) : (
                            <ExpandIcon className="h-4 w-4 text-gray-400" />
                          )}
                        </td>
                      )}
                      {effectiveColumns.map((column) => (
                        <td key={column.key} className="p-3 break-words" role="cell">
                          {column.render
                            ? column.render(row.data[column.key], row)
                            : safeRenderValue(row.data[column.key])}
                        </td>
                      ))}
                      {showDefaultActions && (
                        <td className="p-3 text-right" role="cell">
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
                                  e.stopPropagation();
                                  if (editingRowId !== null || isAddingNewRow) {
                                    return;
                                  }
                                  action.onClick(row);
                                  if (action.showDetail) {
                                    handleRowClick(row.data?.viewId, true);
                                  }
                                }}
                                disabled={editingRowId !== null || isAddingNewRow}
                                aria-label={`${action.label} row ${row.data?.viewId}`}
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

                    {isDefinedNumber(row.data?.viewId) && expandedRowId === row.data.viewId && (
                      <tr className={editingRowId === row.data?.viewId ? 'relative z-[200]' : ''}>
                        <td
                          colSpan={effectiveColumns.length + (showDetailView ? 1 : 0) + (showDefaultActions ? 1 : 0)}
                          className="p-0 bg-[#252525] border-t border-[#3c3c3c]"
                          role="row"
                        >
                          <div
                            className="p-4 relative"
                            ref={editingRowId === row.data?.viewId ? detailContainerRef : null}
                            style={{ zIndex: 200 }}
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
            <div className="border-t border-[#3c3c3c] w-full">
              <Pagination
                pagination={pagination}
                onPageChange={handlePageChange}
                onPageSizeChange={handlePageSizeChange}
              />
            </div>
          )}
        </div>
      </div>

      {showBlockingOverlay && (
        <>
          <div
            className="fixed inset-0 bg-black bg-opacity-40 z-[100]"
            role="presentation"
            aria-hidden="true"
            onClick={(e) => e.stopPropagation()}
          />
          <div
            className="sr-only"
            tabIndex={0}
            aria-hidden="true"
            onFocus={() => {
              if (detailContainerRef.current) {
                const firstInput = detailContainerRef.current.querySelector(
                  'input, select, textarea, button:not([disabled])'
                ) as HTMLElement;
                if (firstInput) firstInput.focus();
              }
            }}
          />
        </>
      )}

      {isEditConfirmOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-[300] flex items-center justify-center" role="dialog" aria-modal="true" aria-labelledby="cancel-confirm-title">
          <div className="bg-[#2d2d2d] p-6 rounded-lg shadow-lg max-w-md w-full">
            <h3 id="cancel-confirm-title" className="text-lg font-medium mb-4">Confirm Cancel</h3>
            <p className="mb-6">Are you sure you want to cancel? All unsaved changes will be lost.</p>
            <div className="flex justify-end space-x-3">
              <button
                className="px-4 py-2 bg-[#3c3c3c] text-white rounded hover:bg-[#4c4c4c]"
                onClick={() => handleConfirmation(false)}
                aria-label="Continue editing"
              >
                No, continue editing
              </button>
              <button
                className="px-4 py-2 bg-red-700 text-white rounded hover:bg-red-800"
                onClick={() => handleConfirmation(true)}
                aria-label="Discard changes"
              >
                Yes, discard changes
              </button>
            </div>
          </div>
        </div>
      )}

      {isSaveConfirmOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-[300] flex items-center justify-center" role="dialog" aria-modal="true" aria-labelledby="save-confirm-title">
          <div className="bg-[#2d2d2d] p-6 rounded-lg shadow-lg max-w-md w-full">
            <h3 id="save-confirm-title" className="text-lg font-medium mb-4">Confirm Save</h3>
            <p className="mb-6">Are you sure you want to save these changes?</p>
            <div className="flex justify-end space-x-3">
              <button
                className="px-4 py-2 bg-[#3c3c3c] text-white rounded hover:bg-[#4c4c4c]"
                onClick={() => handleConfirmation(false)}
                aria-label="Continue editing"
              >
                No, continue editing
              </button>
              <button
                className="px-4 py-2 bg-green-700 text-white rounded hover:bg-green-800"
                onClick={() => handleConfirmation(true)}
                aria-label="Save changes"
              >
                Yes, save changes
              </button>
            </div>
          </div>
        </div>
      )}

      {isDeleteConfirmOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-[300] flex items-center justify-center" role="dialog" aria-modal="true" aria-labelledby="delete-confirm-title">
          <div className="bg-[#2d2d2d] p-6 rounded-lg shadow-lg max-w-md w-full">
            <h3 id="delete-confirm-title" className="text-lg font-medium mb-4">Confirm Delete</h3>
            <p className="mb-6">Are you sure you want to delete this record? This action cannot be undone.</p>
            <div className="flex justify-end space-x-3">
              <button
                className="px-4 py-2 bg-[#3c3c3c] text-white rounded hover:bg-[#4c4c4c]"
                onClick={() => handleConfirmation(false)}
                aria-label="Cancel delete"
              >
                No, cancel
              </button>
              <button
                className="px-4 py-2 bg-red-700 text-white rounded hover:bg-red-800"
                onClick={() => handleConfirmation(true)}
                aria-label="Confirm delete"
              >
                Yes, delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}