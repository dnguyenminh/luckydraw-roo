import React from 'react';
import { FilterState, ColumnDef } from '../../types';
import TextFilter from './TextFilter';
import NumericFilter from './NumericFilter';
import BooleanFilter from './BooleanFilter';

interface ColumnFilterProps {
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
 * ColumnFilter component that selects the appropriate filter based on column type
 */
export const ColumnFilter: React.FC<ColumnFilterProps> = ({
  column,
  filterState,
  onChange,
  onClose
}) => {
  // Select filter component based on column type
  if (column.fieldType === 'BOOLEAN') {
    return (
      <BooleanFilter 
        column={column} 
        filterState={filterState} 
        onChange={onChange} 
        onClose={onClose} 
      />
    );
  }

  if (['NUMBER', 'DATE', 'DATETIME', 'TIME'].includes(column.fieldType)) {
    return (
      <NumericFilter 
        column={column} 
        filterState={filterState} 
        onChange={onChange} 
        onClose={onClose} 
      />
    );
  }

  // Default to text filter for string and other types
  return (
    <TextFilter 
      column={column} 
      filterState={filterState} 
      onChange={onChange} 
      onClose={onClose} 
    />
  );
};

export default ColumnFilter;
