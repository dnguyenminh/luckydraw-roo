import React from 'react';
import { TextFilter } from './TextFilter';
import { NumericFilter } from './NumericFilter';
import { BooleanFilter } from './BooleanFilter';
import { FilterState, ColumnDef, TextFilterOperator, NumericFilterOperator } from '../../types';

interface DataTableColumnFilterProps {
  column: ColumnDef;
  filterState: FilterState;
  onChange: (filterState: FilterState) => void;
  onClose: () => void;
}

export const DataTableColumnFilter: React.FC<DataTableColumnFilterProps> = ({
  column,
  filterState,
  onChange,
  onClose
}) => {
  if (column.fieldType === 'BOOLEAN') {
    return <BooleanFilter column={column} filterState={filterState} onChange={onChange} onClose={onClose} />;
  }

  if (['NUMBER', 'DATE', 'DATETIME', 'TIME'].includes(column.fieldType)) {
    return <NumericFilter column={column} filterState={filterState} onChange={onChange} onClose={onClose} />;
  }

  return <TextFilter column={column} filterState={filterState} onChange={onChange} onClose={onClose} />;
};

export default DataTableColumnFilter;
