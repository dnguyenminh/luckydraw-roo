import React from 'react';
import { Filter, ChevronUp, ChevronDown, Plus } from 'lucide-react';
import { ColumnFilter } from './filters/ColumnFilter';
import { ColumnDef, FilterState, NumericFilterOperator, TextFilterOperator } from '../types';

interface TableHeaderProps {
  /**
   * Column definitions for the table
   */
  columns: ColumnDef[];
  
  /**
   * Current sort field
   */
  sortField: string | null;
  
  /**
   * Current sort direction
   */
  sortDirection: 'asc' | 'desc';
  
  /**
   * Callback when sort changes
   */
  onSort: (field: string) => void;
  
  /**
   * Current filter states
   */
  filters: Record<string, FilterState>;
  
  /**
   * Column key for which filter popup is open
   */
  openFilterColumn: string | null;
  
  /**
   * Callback when filter changes
   */
  onFilterChange: (columnKey: string, filterState: FilterState) => void;
  
  /**
   * Callback to open/close a filter
   */
  onFilterOpen: (columnKey: string | null) => void;
  
  /**
   * Whether to show the expand/collapse column
   */
  showDetailView: boolean;
  
  /**
   * Whether to show actions column
   */
  showActions: boolean;
  
  /**
   * Callback to add a new row
   */
  onAddNew?: () => void;
}

/**
 * TableHeader component renders the table header with sorting and filtering capabilities
 */
export const TableHeader: React.FC<TableHeaderProps> = ({
  columns,
  sortField,
  sortDirection,
  onSort,
  filters,
  openFilterColumn,
  onFilterChange,
  onFilterOpen,
  showDetailView,
  showActions,
  onAddNew
}) => {
  return (
    <thead className="bg-[#2d2d2d] text-white font-medium sticky top-0 z-[10]">
      <tr>
        {showDetailView && (
          <th className="w-10 p-3 whitespace-nowrap" scope="col"></th>
        )}
        
        {columns.map((column) => (
          <th
            key={column.key}
            className="text-left p-3 relative"
            scope="col"
            style={{ width: column.key === 'viewId' ? '80px' : 'auto' }}
          >
            <div className="flex items-start">
              <button
                className={`flex-grow flex flex-wrap items-center mr-5 ${column.sortable ? 'cursor-pointer hover:text-[#007acc]' : ''} ${sortField === column.key ? 'text-[#007acc]' : ''}`}
                onClick={() => column.sortable && onSort(column.key)}
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
                  className={`absolute right-3 top-3 p-1 rounded-full hover:bg-[#3c3c3c] ${filters[column.key]?.active ? 'text-[#007acc]' : 'text-gray-400'}`}
                  onClick={(e) => {
                    e.stopPropagation();
                    onFilterOpen(openFilterColumn === column.key ? null : column.key);
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
                filterState={filters[column.key] || {
                  field: column.key,
                  operator: column.fieldType === 'BOOLEAN' ? 'equals' :
                    ['NUMBER', 'DATE', 'DATETIME', 'TIME'].includes(column.fieldType) ?
                      NumericFilterOperator.EQUALS : TextFilterOperator.CONTAINS,
                  value: null,
                  active: false
                }}
                onChange={(filterState) => onFilterChange(column.key, filterState)}
                onClose={() => onFilterOpen(null)}
              />
            )}
          </th>
        ))}
        
        {showActions && (
          <th className="text-right p-3 whitespace-nowrap" scope="col" style={{ width: '150px' }}>
            <div className="flex items-center justify-between">
              <span>Actions</span>
              {onAddNew && (
                <button
                  onClick={onAddNew}
                  className="flex items-center justify-center p-1 bg-green-700 text-white rounded hover:bg-green-800"
                  title="Add new row"
                  aria-label="Add new row"
                >
                  <Plus size={16} />
                </button>
              )}
            </div>
          </th>
        )}
      </tr>
    </thead>
  );
};

export default TableHeader;
