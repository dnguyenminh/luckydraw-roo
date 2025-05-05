import React, { forwardRef } from 'react';
import { TabTableRow } from '@/app/lib/api/interfaces';
import { ColumnDef } from '../types';

interface ExpandableDetailProps {
  /**
   * Data row to display details for
   */
  row: TabTableRow;
  
  /**
   * Whether the row is in edit mode
   */
  isEditing: boolean;
  
  /**
   * Column definitions for context
   */
  columns: ColumnDef[];
  
  /**
   * Function to render the detail content
   */
  renderContent: (row: TabTableRow) => React.ReactNode;
  
  /**
   * Additional class name for the detail container
   */
  className?: string;
  
  /**
   * Optional additional props for the container
   */
  containerProps?: React.HTMLAttributes<HTMLDivElement>;
}

/**
 * Component that renders the expanded details view for a table row
 * 
 * Can be forwarded a ref to handle focus management
 */
export const ExpandableDetail = forwardRef<HTMLDivElement, ExpandableDetailProps>(
  ({ 
    row, 
    isEditing, 
    columns, 
    renderContent,
    className = "",
    containerProps = {}
  }, ref) => {
    // Calculate the appropriate z-index for proper layering
    const zIndex = isEditing ? 200 : 'auto';
    
    return (
      <div
        className={`p-4 relative ${className}`}
        ref={ref}
        style={{ zIndex }}
        data-row-id={row.data?.viewId || ''}
        data-editing={isEditing || undefined}
        {...containerProps}
      >
        {renderContent(row)}
      </div>
    );
  }
);

// Set display name for better debugging experience in React DevTools
ExpandableDetail.displayName = 'ExpandableDetail';

export default ExpandableDetail;
