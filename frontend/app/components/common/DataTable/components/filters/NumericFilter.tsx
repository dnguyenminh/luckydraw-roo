import React from 'react';
import { X } from 'lucide-react';
import { FilterState, NumericFilterOperator, ColumnDef } from '../../types';

interface NumericFilterProps {
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
 * NumericFilter component provides filtering UI for numeric and date fields
 */
export const NumericFilter: React.FC<NumericFilterProps> = ({
  column,
  filterState,
  onChange,
  onClose
}) => {
  const isDateField = column.fieldType === 'DATE' || column.fieldType === 'DATETIME' || column.fieldType === 'TIME';
  const inputType = isDateField ? (column.fieldType === 'TIME' ? 'time' : 'datetime-local') : 'number';

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
              aria-label="Minimum value"
            />
            <span>to</span>
            <input
              type={inputType}
              className="flex-1 bg-[#3c3c3c] text-white p-1 rounded"
              value={filterState.secondValue || ''}
              onChange={(e) => onChange({ ...filterState, secondValue: e.target.value })}
              placeholder="Max value"
              aria-label="Maximum value"
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
          aria-label={`Filter value for ${column.header}`}
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

export default NumericFilter;
