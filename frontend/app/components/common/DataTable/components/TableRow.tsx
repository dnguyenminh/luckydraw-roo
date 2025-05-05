import React from 'react';
import { ChevronRight as ExpandIcon, ChevronDown as CollapseIcon } from 'lucide-react';
import { ColumnDef, ActionDef } from '../types';
import { TableRow } from '@/app/lib/api/interfaces';

interface DataTableRowProps {
  /**
   * The row data to display
   */
  row: TableRow;
  
  /**
   * Index of the row in the table (for striping)
   */
  index: number;
  
  /**
   * Column definitions for the table
   */
  columns: ColumnDef[];
  
  /**
   * Whether this row is currently expanded
   */
  isExpanded: boolean;
  
  /**
   * Whether this row is currently being edited
   */
  isEditing: boolean;
  
  /**
   * Available actions for this row
   */
  actions: ActionDef[];
  
  /**
   * Whether to show the expand/collapse control
   */
  showDetailView: boolean;
  
  /**
   * Whether to show action buttons
   */
  showActions: boolean;
  
  /**
   * Function to call when the row is clicked
   */
  onRowClick: (rowViewId: number | undefined) => void;
  
  /**
   * Function to call when an action is clicked
   */
  onActionClick: (action: ActionDef, row: TableRow) => void;
  
  /**
   * Function to render cell values that need special handling
   */
  renderValue: (value: any, row: TableRow, column: ColumnDef) => React.ReactNode;
}

/**
 * Component for rendering a single data row in the table
 */
export const DataTableRow: React.FC<DataTableRowProps> = ({
  row,
  index,
  columns,
  isExpanded,
  isEditing,
  actions,
  showDetailView,
  showActions,
  onRowClick,
  onActionClick,
  renderValue
}) => {
  const rowViewId = row.data?.viewId;
  const rowClassNames = [
    index % 2 === 0 ? 'bg-[#1e1e1e]' : 'bg-[#252525]',
    showDetailView ? 'cursor-pointer hover:bg-[#2a2d2e]' : '',
    isExpanded ? 'bg-[#2a2d2e]' : '',
    isEditing ? 'relative z-[200]' : ''
  ].join(' ');
  
  const handleRowClicked = () => {
    onRowClick(rowViewId);
  };
  
  const handleActionClicked = (e: React.MouseEvent, action: ActionDef) => {
    e.stopPropagation();
    if (isEditing) return;
    onActionClick(action, row);
  };
  
  return (
    <tr
      className={rowClassNames}
      onClick={handleRowClicked}
      role="row"
      data-row-id={rowViewId}
      data-expanded={isExpanded || undefined}
      data-editing={isEditing || undefined}
    >
      {isEditing && (
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
          {isExpanded ? (
            <CollapseIcon className="h-4 w-4 text-[#007acc]" />
          ) : (
            <ExpandIcon className="h-4 w-4 text-gray-400" />
          )}
        </td>
      )}

      {columns.map((column) => (
        <td key={column.key} className="p-3 break-words" role="cell">
          {column.render
            ? column.render(row.data[column.key], row)
            : renderValue 
              ? renderValue(row.data[column.key], row, column)
              : row.data[column.key] !== null && row.data[column.key] !== undefined
                ? String(row.data[column.key])
                : '-'}
        </td>
      ))}

      {showActions && (
        <td className="p-3 text-right" role="cell">
          <div className="flex justify-end space-x-2">
            {actions.map((action) => (
              <button
                key={action.label}
                className={`flex items-center text-xs px-2 py-1 rounded ${
                  action.color === 'blue' ? 'bg-blue-800 text-blue-100' :
                  action.color === 'red' ? 'bg-red-800 text-red-100' :
                  action.color === 'green' ? 'bg-green-800 text-green-100' :
                  action.color === 'yellow' ? 'bg-yellow-800 text-yellow-100' :
                  'bg-[#3c3c3c] text-white'
                }`}
                onClick={(e) => handleActionClicked(e, action)}
                disabled={isEditing}
                aria-label={`${action.label} row ${rowViewId}`}
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
  );
};

export default DataTableRow;
