'use client';

import { useState, useEffect, ReactNode } from 'react';
import { Search, Filter, ChevronDown, ChevronUp, MoreHorizontal, Plus } from 'lucide-react';
import { TableFetchRequest, TableFetchResponse, ObjectType } from '@/app/lib/mockData';
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

// Entity configurations for reusability
interface EntityConfig {
  columns: ColumnDef[];
  filterOptions?: FilterOption[];
  defaultActions?: ActionDef[];
  emptyMessage?: string;
}

// Predefined entity configurations for common entity types
const entityConfigs: Record<string, EntityConfig> = {
  event: {
    columns: [
      { key: 'name', header: 'Name' },
      { key: 'startDate', header: 'Start Date' },
      { key: 'endDate', header: 'End Date' },
      { key: 'status', header: 'Status' },
      { key: 'participantCount', header: 'Participants' },
      { key: 'winnerCount', header: 'Winners' }
    ],
    filterOptions: [
      {
        key: 'status',
        label: 'Status',
        options: [
          { value: 'all', label: 'All Status' },
          { value: 'Active', label: 'Active' },
          { value: 'Upcoming', label: 'Upcoming' },
          { value: 'Completed', label: 'Completed' }
        ]
      }
    ],
    defaultActions: [
      { 
        label: 'Edit', 
        onClick: (row) => console.log('Edit event', row),
        color: 'blue'
      },
      { 
        label: 'Delete', 
        onClick: (row) => console.log('Delete event', row),
        color: 'red'
      }
    ],
    emptyMessage: "No events found. Click 'Add Event' to create one."
  },
  participant: {
    columns: [
      { key: 'name', header: 'Name' },
      { key: 'email', header: 'Email' },
      { key: 'province', header: 'Province' },
      { key: 'status', header: 'Status' },
      { key: 'spins', header: 'Spins' },
      { key: 'wins', header: 'Wins' }
    ],
    filterOptions: [
      {
        key: 'status',
        label: 'Status',
        options: [
          { value: 'all', label: 'All Status' },
          { value: 'Active', label: 'Active' },
          { value: 'Inactive', label: 'Inactive' }
        ]
      },
      {
        key: 'province',
        label: 'Province',
        options: [
          { value: 'all', label: 'All Provinces' },
          { value: 'Western Province', label: 'Western Province' },
          { value: 'Eastern Province', label: 'Eastern Province' },
          { value: 'Northern Province', label: 'Northern Province' },
          { value: 'Southern Province', label: 'Southern Province' },
          { value: 'Central Province', label: 'Central Province' }
        ]
      }
    ],
    defaultActions: [
      { 
        label: 'Edit', 
        onClick: (row) => console.log('Edit participant', row),
        color: 'blue'
      },
      { 
        label: 'Delete', 
        onClick: (row) => console.log('Delete participant', row),
        color: 'red'
      }
    ],
    emptyMessage: "No participants found. Click 'Add Participant' to create one."
  },
  goldenHour: {
    columns: [
      { key: 'name', header: 'Name' },
      { key: 'startTime', header: 'Start Time' },
      { key: 'endTime', header: 'End Time' },
      { key: 'startDate', header: 'Start Date' },
      { key: 'endDate', header: 'End Date' },
      { 
        key: 'multiplier', 
        header: 'Multiplier',
        render: (value) => `${value}x` 
      },
      { key: 'status', header: 'Status' }
    ],
    filterOptions: [
      {
        key: 'status',
        label: 'Status',
        options: [
          { value: 'all', label: 'All Status' },
          { value: 'Active', label: 'Active' },
          { value: 'Scheduled', label: 'Scheduled' },
          { value: 'Completed', label: 'Completed' }
        ]
      }
    ],
    defaultActions: [
      { 
        label: 'Edit', 
        onClick: (row) => console.log('Edit golden hour', row),
        color: 'blue'
      },
      { 
        label: 'Delete', 
        onClick: (row) => console.log('Delete golden hour', row),
        color: 'red'
      }
    ],
    emptyMessage: "No golden hours found. Click 'Add Golden Hour' to create one."
  },
  // More entity configs would go here...
};

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
  entityType?: keyof typeof ObjectType; // Updated type to be keyof typeof ObjectType
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
  activeTab?: string; // New property for active tab
  statusField?: string; // New property for status field name (default: 'status')
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
  emptyMessage,
  onPageChange,
  onSortChange, 
  onSearchChange,
  onFilterChange,
  fetchData,
  showDetailView = true,
  activeTab,
  statusField = 'status'
}: DataTableProps) {
  // Get entity configuration if entityType is provided
  const entityConfig = entityType ? entityConfigs[entityType] : undefined;
  
  // Create a safe version of initialData with default values
  const emptyTableData: TableFetchResponse = {
    totalPages: 0,
    currentPage: 0,
    pageSize: 10,
    totalElements: 0,
    tableName: entityType || "empty",
    rows: [],
    originalRequest: {
      page: 0,
      size: 10,
      sorts: [],
      filters: [],
      search: {}
    },
    statistics: {},
    relatedTables: {}
  };
  
  // Use safe initialData (or empty data structure if initialData is null/undefined)
  const safeInitialData = initialData || emptyTableData;

  // Apply entity configuration where props are not provided
  const tableColumns = columns || (entityConfig?.columns || []);
  const tableActions = actions || (entityConfig?.defaultActions || []);
  const tableFilterOptions = filterOptions || (entityConfig?.filterOptions || []);
  const tableEmptyMessage = emptyMessage || (entityConfig?.emptyMessage || "No data found.");

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
        label: `Add ${entityType.charAt(0).toUpperCase() + entityType.slice(1)}`,
        onClick: () => console.log(`Add ${entityType} clicked`) 
      } 
    : (typeof addItemButton === 'object' ? addItemButton : undefined);

  // Built-in entity detail view renderer
  const defaultDetailView = (rowData: any) => {
    return (
      <EntityDetailTabs 
        entityId={rowData.id} 
        entityTable={data}
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

  // Create a TableFetchRequest from current state
  const createFetchRequest = (): TableFetchRequest => {
    // Prepare sorts
    const sorts = sortField 
      ? [{ field: sortField, order: sortDirection }] 
      : [];
    
    // Prepare filters from filter selections
    let requestFilters = Object.entries(filters)
      .filter(([_, value]) => value !== 'all')
      .map(([field, value]) => ({ 
        field, 
        operator: 'eq', 
        value 
      }));
    
    // Add filter from activeTab if provided
    if (activeTab && activeTab !== 'all') {
      // For most entities, the tab name (like 'active', 'completed') should be capitalized
      const statusValue = activeTab.charAt(0).toUpperCase() + activeTab.slice(1);
      
      // Don't add duplicate filters
      if (!requestFilters.some(f => f.field === statusField)) {
        requestFilters.push({
          field: statusField,
          operator: 'eq',
          value: statusValue
        });
      }
    }
    
    // Prepare search
    const search: Record<string, string> = {};
    if (searchTerm) {
      search.global = searchTerm;
    }
    
    return {
      page: currentPage,
      size: data.pageSize || 10,
      sorts,
      filters: requestFilters,
      search
    };
  };
  
  // Load data based on current filters, sort, page, etc.
  const loadData = async () => {
    setIsLoading(true);
    try {
      const request = createFetchRequest();
      const response = await fetchTableData(request);
      setData(response);
    } catch (error) {
      console.error('Error fetching data:', error);
      // Set default empty data on error
      setData(emptyTableData);
    } finally {
      setIsLoading(false);
    }
  };
  
  // Effect to load data when dependencies change
  useEffect(() => {
    loadData();
  }, [currentPage, sortField, sortDirection, searchTerm, filters, activeTab]);
  
  // Handle row click
  const handleRowClick = (rowId: number) => {
    if (showDetailView && (detailView || detailViewMode !== 'custom')) {
      setExpandedRowId(expandedRowId === rowId ? null : rowId);
    }
  };
  
  // Handle search
  const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setSearchTerm(value);
    setCurrentPage(0); // Reset to first page
    
    if (onSearchChange) {
      onSearchChange(value);
    }
  };
  
  // Handle filter change
  const handleFilterChange = (filterKey: string, value: string) => {
    setFilters(prev => ({
      ...prev,
      [filterKey]: value
    }));
    setCurrentPage(0); // Reset to first page
    
    if (onFilterChange) {
      onFilterChange(filterKey, value);
    }
  };
  
  // Handle sort change
  const handleSortChange = (column: string) => {
    if (sortField === column) {
      // Toggle direction if same column
      const newDirection = sortDirection === 'asc' ? 'desc' : 'asc';
      setSortDirection(newDirection);
      
      if (onSortChange) {
        onSortChange(column, newDirection);
      }
    } else {
      // New column, default to ascending
      setSortField(column);
      setSortDirection('asc');
      
      if (onSortChange) {
        onSortChange(column, 'asc');
      }
    }
    
    setCurrentPage(0); // Reset to first page
  };
  
  // Handle page change
  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    
    if (onPageChange) {
      onPageChange(page);
    }
  };
  
  // URL state management integration
  useEffect(() => {
    if (urlStatePrefix) {
      const params = new URLSearchParams(window.location.search);
      
      // Get search term from URL
      const urlSearch = params.get(`${urlStatePrefix}_search`);
      if (urlSearch) setSearchTerm(urlSearch);
      
      // Get sort from URL
      const urlSort = params.get(`${urlStatePrefix}_sort`);
      const urlDir = params.get(`${urlStatePrefix}_dir`);
      if (urlSort) setSortField(urlSort);
      if (urlDir && (urlDir === 'asc' || urlDir === 'desc')) setSortDirection(urlDir);
      
      // Get page from URL
      const urlPage = params.get(`${urlStatePrefix}_page`);
      if (urlPage) setCurrentPage(parseInt(urlPage, 10) || 0);
      
      // Get filters from URL
      const newFilters: Record<string, string> = {};
      tableFilterOptions.forEach(option => {
        const value = params.get(`${urlStatePrefix}_filter_${option.key}`);
        if (value) newFilters[option.key] = value;
      });
      
      if (Object.keys(newFilters).length > 0) {
        setFilters(newFilters);
      }
    }
  }, [urlStatePrefix, tableFilterOptions]);
  
  // Effect to serialize current state to URL
  useEffect(() => {
    if (urlStatePrefix) {
      const params = new URLSearchParams(window.location.search);
      
      // Set search term
      if (searchTerm) {
        params.set(`${urlStatePrefix}_search`, searchTerm);
      } else {
        params.delete(`${urlStatePrefix}_search`);
      }
      
      // Set sort
      if (sortField) {
        params.set(`${urlStatePrefix}_sort`, sortField);
        params.set(`${urlStatePrefix}_dir`, sortDirection);
      } else {
        params.delete(`${urlStatePrefix}_sort`);
        params.delete(`${urlStatePrefix}_dir`);
      }
      
      // Set page
      if (currentPage > 0) {
        params.set(`${urlStatePrefix}_page`, currentPage.toString());
      } else {
        params.delete(`${urlStatePrefix}_page`);
      }
      
      // Set filters
      Object.entries(filters).forEach(([key, value]) => {
        if (value && value !== 'all') {
          params.set(`${urlStatePrefix}_filter_${key}`, value);
        } else {
          params.delete(`${urlStatePrefix}_filter_${key}`);
        }
      });
      
      // Update URL without reloading page
      const newUrl = `${window.location.pathname}?${params.toString()}`;
      window.history.replaceState({}, '', newUrl);
    }
  }, [searchTerm, sortField, sortDirection, currentPage, filters, urlStatePrefix]);

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
              onChange={handleSearch}
            />
          </div>
          
          {/* Filters */}
          {tableFilterOptions.map((filter) => (
            <select
              key={filter.key}
              className="bg-[#3c3c3c] text-white px-3 py-2 rounded focus:outline-none focus:ring-1 focus:ring-[#007acc]"
              value={filters[filter.key] || 'all'}
              onChange={(e) => handleFilterChange(filter.key, e.target.value)}
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
        {tableColumns.length > 0 && (
          <div className="grid grid-cols-12 bg-[#2d2d2d] text-white font-medium p-3">
            {tableColumns.map((column, index) => {
              // Calculate column width based on the number of columns and actions
              const colSpan = Math.max(1, Math.floor((12 - (tableActions.length > 0 ? 2 : 0)) / tableColumns.length));
              
              return (
                <div
                  key={column.key}
                  className={`col-span-${colSpan} flex items-center cursor-pointer`}
                  onClick={() => handleSortChange(column.key)}
                >
                  {column.header}
                  {sortField === column.key && (
                    sortDirection === 'asc' ? 
                      <ChevronUp className="ml-1 h-4 w-4" /> : 
                      <ChevronDown className="ml-1 h-4 w-4" />
                  )}
                </div>
              );
            })}
            
            {/* Actions Column Header */}
            {tableActions.length > 0 && (
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
                {/* Regular Row - add null checks for row.data */}
                <div 
                  className={`grid grid-cols-12 p-3 text-[#cccccc] ${
                    showDetailView ? 'cursor-pointer hover:bg-[#2a2d2e]' : ''
                  } ${expandedRowId === row.data?.id ? 'bg-[#2a2d2e]' : ''}`}
                  onClick={() => row.data?.id && handleRowClick(row.data.id)}
                >
                  {/* Data Columns - ensure row.data exists */}
                  {row.data && tableColumns.map((column, index) => {
                    const colSpan = Math.max(1, Math.floor((12 - (tableActions.length > 0 ? 2 : 0)) / tableColumns.length));
                    const value = row.data[column.key];
                    
                    return (
                      <div key={`${row.data.id || index}-${column.key}`} className={`col-span-${colSpan} break-words`}>
                        {column.render ? column.render(value, row.data) : value}
                      </div>
                    );
                  })}
                  
                  {/* Actions Column - ensure row.data exists */}
                  {row.data && tableActions.length > 0 && (
                    <div 
                      className="col-span-2 flex justify-end space-x-2" 
                      onClick={(e) => e.stopPropagation()}
                    >
                      {tableActions.map((action, actionIndex) => {
                        // Determine action button color
                        const colorClasses = {
                          blue: 'text-blue-400 hover:text-blue-300',
                          red: 'text-red-400 hover:text-red-300',
                          green: 'text-green-400 hover:text-green-300',
                          yellow: 'text-yellow-400 hover:text-yellow-300'
                        };
                        const color = action.color ? colorClasses[action.color] : 'text-gray-400 hover:text-white';
                        
                        return (
                          <button 
                            key={`${row.data.id || actionIndex}-action-${actionIndex}`}
                            className={`flex items-center ${color}`}
                            onClick={() => action.onClick(row.data)}
                          >
                            {action.iconLeft}
                            <span>{action.label}</span>
                            {action.iconRight}
                          </button>
                        );
                      })}
                    </div>
                  )}
                </div>
                
                {/* Expanded Detail View - ensure row.data exists and has id property */}
                {row.data?.id && expandedRowId === row.data.id && (
                  <div className="p-4 bg-[#252525] border-t border-[#3c3c3c]">
                    {renderRowDetail(row.data)}
                  </div>
                )}
              </div>
            ))
          ) : (
            <div className="p-8 text-center text-gray-400">
              {tableEmptyMessage}
            </div>
          )}
        </div>
      </div>
      
      {/* Pagination - ensure data has required properties */}
      {hasData && data.totalPages > 1 && (
        <div className="mt-4 flex justify-between items-center">
          <div className="text-sm text-gray-400">
            Showing {data.currentPage * data.pageSize + 1} to {Math.min((data.currentPage + 1) * data.pageSize, data.totalElements)} of {data.totalElements} entries
          </div>
          
          <div className="flex space-x-2">
            {/* First Page Button */}
            <button
              className={`px-3 py-1 rounded ${data.currentPage === 0 ? 'bg-[#2d2d2d] text-gray-500 cursor-not-allowed' : 'bg-[#2d2d2d] text-white hover:bg-[#3c3c3c]'}`}
              onClick={() => handlePageChange(0)}
              disabled={data.currentPage === 0}
            >
              First
            </button>
            
            {/* Previous Page Button */}
            <button
              className={`px-3 py-1 rounded ${data.currentPage === 0 ? 'bg-[#2d2d2d] text-gray-500 cursor-not-allowed' : 'bg-[#2d2d2d] text-white hover:bg-[#3c3c3c]'}`}
              onClick={() => handlePageChange(data.currentPage - 1)}
              disabled={data.currentPage === 0}
            >
              Previous
            </button>
            
            {/* Page Numbers */}
            {Array.from({ length: data.totalPages }).map((_, index) => {
              // Only show a few pages around the current page
              if (
                index === 0 ||
                index === data.totalPages - 1 ||
                (index >= data.currentPage - 1 && index <= data.currentPage + 1)
              ) {
                return (
                  <button
                    key={index}
                    className={`px-3 py-1 rounded ${data.currentPage === index ? 'bg-[#007acc] text-white' : 'bg-[#2d2d2d] text-white hover:bg-[#3c3c3c]'}`}
                    onClick={() => handlePageChange(index)}
                  >
                    {index + 1}
                  </button>
                );
              } else if (
                (index === data.currentPage - 2 && data.currentPage > 2) ||
                (index === data.currentPage + 2 && data.currentPage < data.totalPages - 3)
              ) {
                // Show ellipsis
                return <span key={index} className="px-2">...</span>;
              }
              
              return null;
            })}
            
            {/* Next Page Button */}
            <button
              className={`px-3 py-1 rounded ${data.currentPage === data.totalPages - 1 ? 'bg-[#2d2d2d] text-gray-500 cursor-not-allowed' : 'bg-[#2d2d2d] text-white hover:bg-[#3c3c3c]'}`}
              onClick={() => handlePageChange(data.currentPage + 1)}
              disabled={data.currentPage === data.totalPages - 1}
            >
              Next
            </button>
            
            {/* Last Page Button */}
            <button
              className={`px-3 py-1 rounded ${data.currentPage === data.totalPages - 1 ? 'bg-[#2d2d2d] text-gray-500 cursor-not-allowed' : 'bg-[#2d2d2d] text-white hover:bg-[#3c3c3c]'}`}
              onClick={() => handlePageChange(data.totalPages - 1)}
              disabled={data.currentPage === data.totalPages - 1}
            >
              Last
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
