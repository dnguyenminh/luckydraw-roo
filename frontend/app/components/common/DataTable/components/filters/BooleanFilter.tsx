import React from 'react';
import { X } from 'lucide-react';
import { FilterState, ColumnDef } from '../../types';

interface BooleanFilterProps {
  /**
   * Column definition for this filter
   */
  column: ColumnDef;
  
  /**
   * Current filter state
   */
  filterState: FilterState;
  
  /**
   * Callback when filter changes
   */
  onChange: (filterState: FilterState) => void;
  
  /**
   * Callback to close filter popup
   */
  onClose: () => void;
}

/**
 * BooleanFilter component provides filtering UI for boolean fields
 */
export const BooleanFilter: React.FC<BooleanFilterProps> = ({
  column,
  filterState,
  onChange,
  onClose
}) => {
  return (
    <div className="p-2 bg-[#2d2d2d] border border-[#3c3c3c] rounded shadow-lg absolute z-[200] w-64 mt-1">
      <div className="flex justify-between items-center mb-2">
        <h3 className="text-sm font-medium">Filter {column.header}</h3>
        <button 
          onClick={onClose} 
          className="text-gray-400 hover:text-white" 
          aria-label="Close filter"
        >
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
            aria-label="Filter for true values"
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
            aria-label="Filter for false values"
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
            aria-label="Show all values"
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

export default BooleanFilter;
