/**
 * Toast utility function for showing notifications
 */

interface ToastOptions {
  title: string;
  description?: string;
  variant?: 'default' | 'destructive' | 'success';
}

// Simple toast implementation for use throughout the application
export const toast = (options: ToastOptions) => {
  const { title, description, variant = 'default' } = options;
  
  // For now, just console log the toast message since there's no UI component
  console.log(`[TOAST - ${variant}] ${title}${description ? ': ' + description : ''}`);
  
  // In a real implementation, this would show a toast notification UI
  // You can implement this with a toast library or a custom toast component later
};
