'use client';

import { useState, useEffect, ReactNode } from 'react';
import { Search, Filter, ChevronDown, ChevronUp, MoreHorizontal, Plus } from 'lucide-react';
import { TableFetchRequest, TableFetchResponse, ObjectType, TabTableRow, FetchStatus, DataObjectKeyValues } from '@/app/lib/api/interfaces';
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

export default function DataTable({
  data: initialData,
  columns,
  actions,
  detailView,
  detailViewMode = 'auto',
  entityType = 'EVENT',
  addItemButton,
  filterOptions,
  urlStatePrefix,
  emptyMessage = "No data found.",
  onPageChange,
  onSortChange,
  onSearchChange,
  onFilterChange,
  fetchData,
  showDetailView = true,
  activeTab,
  statusField = 'status'
}: DataTableProps) {
  // Create a safe version of initialData with default values
  const emptyTableData: TableFetchResponse = {
    totalPage: 0, // Changed from totalPages to totalPage to match interface
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
      // Fix for search property - create a properly typed empty Record
      search: Object.values(ObjectType).reduce((acc, type) => {
        acc[type] = { searchCriteria: {} };
        return acc;
      }, {} as Record<ObjectType, DataObjectKeyValues>),
      objectType: ObjectType[entityType as keyof typeof ObjectType]
    },
    // Fix for statistics property - ensure charts property exists
    statistics: {
      charts: {}
    },
    relatedLinkedObjects: {},
    first: true,
    last: true,
    empty: true,
    numberOfElements: 0
  };

  // Use safe initialData (or empty data structure if initialData is null/undefined)
  const safeInitialData = initialData || emptyTableData;

  // Setup state with safe initial data
  const [data, setData] = useState<TableFetchResponse>(safeInitialData);
  const [expandedRowId, setExpandedRowId] = useState<number | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filters, setFilters] = useState<Record<string, string>>({});
  const [currentPage, setCurrentPage] = useState(0);
  const [sortField, setSortField] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [isLoading, setIsLoading] = useState(false);

  // Update data state when initialData changes
  useEffect(() => {
    if (initialData) {
      setData(initialData);
    }
  }, [initialData]);

  // Auto-create the addItemButton if provided as boolean true
  const actualAddItemButton = typeof addItemButton === 'boolean' && addItemButton === true
    ? {
      label: `Add ${entityType.charAt(0).toUpperCase() + entityType.slice(1).toLowerCase()}`,
      onClick: () => console.log(`Add ${entityType} clicked`)
    }
    : (typeof addItemButton === 'object' ? addItemButton : undefined);

  // Built-in entity detail view renderer
  const defaultDetailView = (rowData: TabTableRow) => {
    // Pass the entire row data object to the EntityDetailTabs component
    // This allows access to both the relatedTables list and other needed properties
    return (
      <EntityDetailTabs
        tableRow={rowData}
        entityType={entityType}
      />
    );
  };

  // Handle row detail view rendering based on provided mode
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

  // Create a function to fetch data from the API
  const fetchTableData = async (request: TableFetchRequest): Promise<TableFetchResponse> => {
    // If a custom fetchData function is provided, use it
    if (fetchData) {
      return fetchData(request);
    }

    // Add objectType to the request based on entityType
    const requestWithObjectType: TableFetchRequest = {
      ...request,
      objectType: ObjectType[entityType as keyof typeof ObjectType]
    };

    try {
      // Use the tableService to fetch data without needing to specify the endpoint
      const response = await import('@/app/lib/api/tableService')
        .then(module => module.fetchTableData(requestWithObjectType));
      return response;
    } catch (error) {
      console.error(`Error fetching ${entityType} data:`, error);
      return emptyTableData;
    }
  };

  // Handle row click
  const handleRowClick = (rowId: number) => {
    if (showDetailView && (detailView || detailViewMode !== 'custom')) {
      setExpandedRowId(expandedRowId === rowId ? null : rowId);
    }
  };

  // Check if there's any data to display
  const hasData = data?.rows && data.rows.length > 0;

  return (
    <div className="w-full">
      {/* Search and Filter Bar */}
      <div className="mb-4 flex flex-wrap justify-between items-center gap-3">
        <div className="flex flex-wrap gap-2">
          {/* Search Input */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-500" />
            <input
              type="text"
              placeholder="Search..."
              className="pl-9 py-2 pr-4 bg-[#3c3c3c] rounded focus:outline-none focus:ring-1 focus:ring-[#007acc]"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          {/* Filters */}
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

        {/* Add Item Button */}
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

      {/* Table */}
      <div className="bg-[#1e1e1e] border border-[#3c3c3c] rounded-md overflow-hidden">
        {/* Table Header */}
        {columns && columns.length > 0 && (
          <div className="grid grid-cols-12 bg-[#2d2d2d] text-white font-medium p-3">
            {columns.map((column) => (
              <div
                key={column.key}
                className="col-span-1 flex items-center cursor-pointer"
                onClick={() => setSortField(column.key)}
              >
                {column.header}
                {sortField === column.key && (
                  sortDirection === 'asc' ?
                    <ChevronUp className="ml-1 h-4 w-4" /> :
                    <ChevronDown className="ml-1 h-4 w-4" />
                )}
              </div>
            ))}

            {/* Actions Column Header */}
            {actions && actions.length > 0 && (
              <div className="col-span-2 text-right">Actions</div>
            )}
          </div>
        )}

        {/* Table Body */}
        <div className="divide-y divide-[#3c3c3c]">
          {isLoading ? (
            <div className="p-8 text-center">
              <div className="animate-spin h-8 w-8 border-4 border-[#007acc] border-t-transparent rounded-full mx-auto mb-2"></div>
              <p className="text-gray-400">Loading data...</p>
            </div>
          ) : hasData ? (
            data.rows.map((row) => (
              <div key={row.data?.id || Math.random()}>
                {/* Regular Row */}
                <div
                  className={`grid grid-cols-12 p-3 text-[#cccccc] ${showDetailView ? 'cursor-pointer hover:bg-[#2a2d2e]' : ''} ${expandedRowId === row.data?.id ? 'bg-[#2a2d2e]' : ''}`}
                  onClick={() => row.data?.id && handleRowClick(row.data.id)}
                >
                  {columns && columns.map((column) => (
                    <div key={column.key} className="col-span-1 break-words">
                      {column.render ? column.render(row.data[column.key], row.data) : row.data[column.key]}
                    </div>
                  ))}

                  {/* Actions */}
                  {actions && actions.length > 0 && (
                    <div className="col-span-2 flex justify-end space-x-2">
                      {actions.map((action) => (
                        <button
                          key={action.label}
                          className={`flex items-center ${action.color}`}
                          onClick={() => action.onClick(row.data)}
                        >
                          {action.iconLeft}
                          <span>{action.label}</span>
                          {action.iconRight}
                        </button>
                      ))}
                    </div>
                  )}
                </div>

                {/* Expanded Detail View */}
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
