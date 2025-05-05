import React from 'react';
import { X } from 'lucide-react';
import { FilterState, TextFilterOperator, ColumnDef } from '../../types';

interface TextFilterProps {
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
 * TextFilter component provides filtering UI for text fields
 */
export const TextFilter: React.FC<TextFilterProps> = ({
  column,
  filterState,
  onChange,
  onClose
}) => {
  return (
    <div className="p-2 bg-[#2d2d2d] border border-[#3c3c3d] rounded shadow-lg absolute z-[200] w-64 mt-1">
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
        aria-label={`Filter value for ${column.header}`}
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

export default TextFilter;
