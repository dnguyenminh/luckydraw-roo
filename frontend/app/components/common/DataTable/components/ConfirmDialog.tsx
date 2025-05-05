import React from 'react';

interface ConfirmDialogProps {
  /**
   * Whether the dialog is visible
   */
  isOpen: boolean;
  
  /**
   * Title of the dialog
   */
  title: string;
  
  /**
   * Main message/question displayed in the dialog
   */
  message: string;
  
  /**
   * Label for the confirm button
   */
  confirmLabel: string;
  
  /**
   * Label for the cancel button
   */
  cancelLabel: string;
  
  /**
   * Function called when the user confirms the action
   */
  onConfirm: () => void;
  
  /**
   * Function called when the user cancels the action
   */
  onCancel: () => void;
  
  /**
   * CSS class for the confirm button (optional)
   */
  confirmButtonClass?: string;
  
  /**
   * Icon to display in the confirm button (optional)
   */
  confirmIcon?: React.ReactNode;
  
  /**
   * Icon to display in the cancel button (optional)
   */
  cancelIcon?: React.ReactNode;
  
  /**
   * Additional content to display in the dialog (optional)
   */
  children?: React.ReactNode;
}

/**
 * A reusable confirmation dialog component
 * 
 * Used for confirming user actions like save, delete, or cancel operations
 */
export const ConfirmDialog: React.FC<ConfirmDialogProps> = ({
  isOpen,
  title,
  message,
  confirmLabel,
  cancelLabel,
  onConfirm,
  onCancel,
  confirmButtonClass = "bg-red-700 text-white rounded hover:bg-red-800",
  confirmIcon,
  cancelIcon,
  children
}) => {
  if (!isOpen) return null;
  
  // Handle confirm with keyboard Enter key
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      onConfirm();
    } else if (e.key === 'Escape') {
      onCancel();
    }
  };
  
  return (
    <div 
      className="fixed inset-0 bg-black bg-opacity-50 z-[300] flex items-center justify-center" 
      role="dialog" 
      aria-modal="true" 
      aria-labelledby="confirm-dialog-title"
      onKeyDown={handleKeyDown}
    >
      <div 
        className="bg-[#2d2d2d] p-6 rounded-lg shadow-lg max-w-md w-full"
        onClick={(e) => e.stopPropagation()}
      >
        <h3 
          id="confirm-dialog-title" 
          className="text-lg font-medium mb-4"
        >
          {title}
        </h3>
        
        <p className="mb-6 text-gray-300">{message}</p>
        
        {/* Optional additional content */}
        {children && (
          <div className="mb-6">
            {children}
          </div>
        )}
        
        <div className="flex justify-end space-x-3">
          <button
            className="px-4 py-2 bg-[#3c3c3c] text-white rounded hover:bg-[#4c4c4c] flex items-center"
            onClick={onCancel}
            aria-label={cancelLabel}
          >
            {cancelIcon && <span className="mr-2">{cancelIcon}</span>}
            {cancelLabel}
          </button>
          
          <button
            className={`px-4 py-2 flex items-center ${confirmButtonClass}`}
            onClick={onConfirm}
            aria-label={confirmLabel}
            autoFocus
          >
            {confirmIcon && <span className="mr-2">{confirmIcon}</span>}
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmDialog;
