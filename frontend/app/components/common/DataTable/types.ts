import { ReactNode } from 'react';
import { TableFetchRequest, TableFetchResponse, ObjectType, TabTableRow, DataObject, TableRow, SortType, FilterType } from '@/app/lib/api/interfaces';

// Filter operator enums
export enum TextFilterOperator {
  EQUALS = 'equals',
  CONTAINS = 'contains',
  STARTS_WITH = 'startsWith',
  ENDS_WITH = 'endsWith'
}

export enum NumericFilterOperator {
  EQUALS = 'equals',
  BETWEEN = 'between',
  GREATER_THAN = 'greaterThan',
  LESS_THAN = 'lessThan'
}

export interface FilterState {
  field: string;
  operator: TextFilterOperator | NumericFilterOperator | string;
  value: any;
  secondValue?: any; // For BETWEEN operator
  active: boolean;
}

export interface PaginationState {
  pageIndex: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
}

// Enhanced column definitions
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

// Action definition interface
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

// Detail view modes
export type DetailViewMode = 'custom' | 'auto' | 'tabs';

export interface FilterOption {
  key: string;
  label: string;
  options: { value: string; label: string }[];
}

export interface DataTableProps {
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

export type ReloadFunction = (options?: {
  pageIndex?: number;
  pageSize?: number;
  sortField?: string | null;
  sortDirection?: 'asc' | 'desc' | null;
  filters?: Record<string, FilterState>;
  searchTerm?: string;
  isInitialLoad?: boolean;
  isForceReload?: boolean;
  isSortChange?: boolean;
}) => Promise<void>;
