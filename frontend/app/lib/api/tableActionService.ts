import { ObjectType, TableAction, TableActionRequest, TableActionResponse, FilterRequest, TableRow, TableFetchResponse, UploadFile } from './interfaces';
import { apiConfig, entityApiEndpoints, objectTypeToEndpoint, getApiUrl } from './tableService';

/**
 * Service for handling table action operations
 */

// Helper function to get API endpoint for an entity type
function getEntityEndpoint(entityType: ObjectType): string {
    // Get endpoint from mapping
    if (typeof entityType === 'object' && objectTypeToEndpoint[entityType]) {
        return objectTypeToEndpoint[entityType];
    }

    // Last resort: pluralize the entity type
    // Ensure we don't add a trailing slash
    return `${String(entityType).toLowerCase()}s`.replace(/\/+$/, '');
}

/**
 * Core function to perform any table action
 * @param action The action to perform (ADD, UPDATE, DELETE, etc)
 * @param entityType The type of entity
 * @param data The record data
 * @param tableInfo Table information from the response
 * @param uploadFile Optional file for import operations
 * @returns Promise with the response
 */
async function performTableAction(
    action: TableAction,
    entityType: ObjectType,
    data: TableRow,
    tableInfo: TableFetchResponse,
    uploadFile?: UploadFile
): Promise<TableActionResponse> {
    console.log(`Performing ${action} for entity type:`, entityType, 'with data:', data);

    try {
        // Prepare the request object
        const request: TableActionRequest = {
            objectType: entityType,
            action: action,
            entityName: entityType.toString(),
            data: data
        };

        // Add uploadFile if provided
        if (uploadFile) {
            request.uploadFile = uploadFile;
        }

        // Add filters from tableInfo if available
        if (tableInfo && tableInfo.originalRequest && tableInfo.originalRequest.filters) {
            request.filters = tableInfo.originalRequest.filters;
        }

        // Add sorts from tableInfo if available
        if (tableInfo && tableInfo.originalRequest && tableInfo.originalRequest.sorts) {
            request.sorts = tableInfo.originalRequest.sorts;
        }

        // Add search context from tableInfo if available
        if (tableInfo && tableInfo.originalRequest && tableInfo.originalRequest.search) {
            request.search = tableInfo.originalRequest.search;
        }

        const url = getApiUrl(`table-data/action/${getEntityEndpoint(entityType)}`);
        console.log('Making API call to:', url);

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                ...apiConfig.headers
            },
            body: JSON.stringify(request)
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error(`Error performing ${action}:`, errorText);
            return {
                success: false,
                message: `API error: ${response.status} ${errorText || ''}`
            } as TableActionResponse;
        }

        const result = await response.json();
        console.log(`${action} API response:`, result);

        // Handle download for EXPORT action
        if (action === TableAction.EXPORT &&
            result.data && result.data.data && result.data.data.downloadToken) {
            const downloadToken = result.data.data.downloadToken;
            const fileName = result.data.data.fileName || `export.xlsx`;

            // Create the download URL
            const downloadUrl = getApiUrl(`table-data/download/${fileName}?token=${downloadToken}`);

            // Create a temporary link and trigger download
            const link = document.createElement('a');
            link.href = downloadUrl;
            link.download = fileName;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        }

        return {
            success: true,
            ...result
        };
    } catch (error) {
        console.error(`Error performing ${action}:`, error);
        return {
            success: false,
            message: error instanceof Error ? error.message : 'Unknown error'
        } as TableActionResponse;
    }
}

/**
 * Add a new record via API
 * @param entityType The type of entity to add
 * @param data The record data to add
 * @param tableInfo Table information for the record
 * @returns Promise with the response
 */
export async function addRecord(entityType: ObjectType, data: TableRow, tableInfo: TableFetchResponse): Promise<TableActionResponse> {
    return performTableAction(TableAction.ADD, entityType, data, tableInfo);
}

/**
 * Update an existing record via API
 * @param entityType The type of entity to update
 * @param data The record data to update
 * @param tableInfo Table information for the record
 * @returns Promise with the response
 */
export async function updateRecord(entityType: ObjectType, data: TableRow, tableInfo: TableFetchResponse): Promise<TableActionResponse> {
    return performTableAction(TableAction.UPDATE, entityType, data, tableInfo);
}

/**
 * Delete a record via API
 * @param entityType The type of entity
 * @param data The record data to delete
 * @param tableInfo Table information for the record
 * @returns Promise with the response
 */
export async function deleteRecord(entityType: ObjectType, data: TableRow, tableInfo: TableFetchResponse): Promise<TableActionResponse> {
    return performTableAction(TableAction.DELETE, entityType, data, tableInfo);
}

/**
 * Simple export table data via API (legacy version)
 * @param entityType The type of entity
 * @param tableInfo Table information containing filters, sorts, etc.
 * @returns Promise with the response
 */
export async function exportTableDataBasic(
    entityType: ObjectType,
    tableInfo: TableFetchResponse
): Promise<void> {
    try {
        // Create an empty TableRow for export operations
        const data: TableRow = { data: {} };

        // Create a modified table info object that won't trigger a reload
        // We make a deep clone to avoid modifying the original object
        const modifiedTableInfo = JSON.parse(JSON.stringify(tableInfo));
        
        // Set special flags to prevent reload behavior
        modifiedTableInfo.noReload = true;
        
        await performTableAction(TableAction.EXPORT, entityType, data, modifiedTableInfo);
    } catch (error) {
        console.error('Error exporting table data:', error);
        throw error;
    }
}

/**
 * Export table data to Excel with user feedback and polling
 */
export const exportTableData = async (objectType: ObjectType, data: TableFetchResponse): Promise<boolean> => {
    try {
        // Show user feedback that export has started
        const notification = createNotification('Export started', 'Your export is being prepared...', 'info');
        
        // Get a proper object type string to use in the URL
        const entityType = typeof objectType === 'string' 
            ? objectType.toLowerCase() 
            : typeof objectType === 'object' && objectTypeToEndpoint[objectType]
                ? objectTypeToEndpoint[objectType]
                : String(objectType).toLowerCase();
        
        // Add 's' for pluralization if needed
        const pluralizedType = entityType.endsWith('s') ? entityType : `${entityType}s`;
        
        // Use the configured baseUrl from apiConfig to ensure request goes to correct server
        const apiUrl = getApiUrl(`table-data/action/${pluralizedType}`);
        
        console.log('Using final API URL:', apiUrl);
        
        // Prepare the request payload with filters and sorts from the data
        const payload = {
            action: 'EXPORT',
            objectType,
            filters: data.originalRequest?.filters || [],
            sorts: data.originalRequest?.sorts || [],
            search: data.originalRequest?.search || {},
        };
        
        console.log('Export request payload:', payload);
        
        // Use direct fetch with clean URL
        const response = await fetch(apiUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload),
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error('Export failed with status:', response.status);
            console.error('Error response:', errorText);
            updateNotification(notification, 'Export failed', `Server error: ${response.status}`, 'error');
            return false;
        }
        
        const result = await response.json();

        // Extract download token and filename from the response
        const downloadToken = result.data?.data?.downloadToken;
        const fileName = result.data?.data?.fileName;

        if (downloadToken && fileName) {
            // Create download URL with token using getApiUrl
            const downloadUrl = getApiUrl(`table-data/download/${fileName}?token=${downloadToken}`);
            
            // Update notification to show progress
            updateNotification(notification, 'Export in progress', 'Your file is being prepared...', 'info');
            
            // Start polling for file completion
            const downloadSuccess = await pollForFileCompletion(downloadUrl, fileName, notification);
            return downloadSuccess;
        } else {
            console.error('Invalid export response:', result);
            updateNotification(notification, 'Export failed', 'Invalid response from server', 'error');
            return false;
        }
    } catch (error) {
        console.error('Error exporting data:', error);
        return false;
    }
};

/**
 * Poll the server to check if export file is ready
 */
const pollForFileCompletion = async (
    url: string, 
    fileName: string, 
    notificationId: string,
    maxAttempts = 30, 
    initialDelay = 2000
): Promise<boolean> => {
    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
        try {
            updateNotification(
                notificationId, 
                'Export in progress', 
                `Waiting for file to be ready... (attempt ${attempt}/${maxAttempts})`, 
                'info'
            );
            
            // Wait before checking
            await new Promise(resolve => setTimeout(resolve, initialDelay));
            
            // Check file status with a HEAD request
            // We don't need to modify the url here as it now comes in with the correct base URL
            const response = await fetch(url, { method: 'HEAD' });
            
            // If file is ready
            if (response.ok) {
                // Trigger download
                const downloadSuccess = await triggerFileDownload(url, fileName);
                
                if (downloadSuccess) {
                    updateNotification(notificationId, 'Export complete', 'Your file is ready', 'success');
                    return true;
                } else {
                    updateNotification(notificationId, 'Export failed', 'Download could not be started', 'error');
                    return false;
                }
            }
            
            // If still processing, continue polling
            if (response.status === 202) {
                // Increase delay with each attempt (exponential backoff)
                initialDelay = Math.min(initialDelay * 1.5, 10000); // Cap at 10 seconds
                continue;
            }
            
            // Handle other status codes
            updateNotification(
                notificationId, 
                'Export failed', 
                `Server returned status ${response.status}`, 
                'error'
            );
            return false;
        } catch (error) {
            console.error('Error polling for file:', error);
        }
    }
    
    // If we've exceeded max attempts
    updateNotification(notificationId, 'Export timed out', 'File took too long to prepare', 'error');
    return false;
};

/**
 * Trigger actual file download
 */
const triggerFileDownload = async (url: string, fileName: string): Promise<boolean> => {
    return new Promise(resolve => {
        try {
            // Use fetch to get the file directly instead of relying on link click events
            fetch(url)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`HTTP error! Status: ${response.status}`);
                    }
                    return response.blob();
                })
                .then(blob => {
                    // Create URL for the blob
                    const blobUrl = window.URL.createObjectURL(blob);
                    
                    // Create and trigger download
                    const link = document.createElement('a');
                    link.href = blobUrl;
                    link.download = fileName;
                    link.style.display = 'none';
                    document.body.appendChild(link);
                    
                    // Trigger the download
                    link.click();
                    
                    // Clean up
                    setTimeout(() => {
                        window.URL.revokeObjectURL(blobUrl);
                        document.body.removeChild(link);
                        resolve(true);
                    }, 100);
                })
                .catch(error => {
                    console.error('Download error:', error);
                    resolve(false);
                });
        } catch (error) {
            console.error('Error triggering download:', error);
            resolve(false);
        }
    });
};

/**
 * Create a notification element for user feedback
 */
const createNotification = (title: string, message: string, type: 'info' | 'success' | 'error'): string => {
    // This is a simplified implementation - you might want to use a proper notification library
    const id = `notification-${Date.now()}`;
    
    // Check if we already have a notification container
    let container = document.getElementById('notification-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'notification-container';
        container.style.position = 'fixed';
        container.style.bottom = '20px';
        container.style.right = '20px';
        container.style.zIndex = '9999';
        document.body.appendChild(container);
    }
    
    // Create the notification element
    const notification = document.createElement('div');
    notification.id = id;
    notification.className = `notification notification-${type}`;
    notification.style.padding = '12px 16px';
    notification.style.margin = '8px 0';
    notification.style.borderRadius = '4px';
    notification.style.boxShadow = '0 2px 4px rgba(0,0,0,0.2)';
    notification.style.backgroundColor = type === 'info' ? '#007acc' : 
                                         type === 'success' ? '#28a745' : 
                                         '#dc3545';
    notification.style.color = 'white';
    
    // Add title and message
    notification.innerHTML = `
        <div style="font-weight: bold;">${title}</div>
        <div style="font-size: 0.875em;">${message}</div>
    `;
    
    // Add close button
    const closeButton = document.createElement('button');
    closeButton.innerHTML = '&times;';
    closeButton.style.position = 'absolute';
    closeButton.style.top = '8px';
    closeButton.style.right = '8px';
    closeButton.style.background = 'none';
    closeButton.style.border = 'none';
    closeButton.style.color = 'white';
    closeButton.style.fontSize = '16px';
    closeButton.style.cursor = 'pointer';
    closeButton.onclick = () => container!.removeChild(notification);
    notification.appendChild(closeButton);
    
    // Add to container
    container.appendChild(notification);
    
    // Auto-remove after 20 seconds unless it's an error
    if (type !== 'error') {
        setTimeout(() => {
            if (document.getElementById(id)) {
                container!.removeChild(notification);
            }
        }, 20000);
    }
    
    return id;
};

/**
 * Update an existing notification
 */
const updateNotification = (
    id: string, 
    title: string, 
    message: string, 
    type: 'info' | 'success' | 'error'
): void => {
    const notification = document.getElementById(id);
    if (!notification) return;
    
    // Update styles based on type
    notification.style.backgroundColor = type === 'info' ? '#007acc' : 
                                        type === 'success' ? '#28a745' : 
                                        '#dc3545';
    
    // Update content
    notification.innerHTML = `
        <div style="font-weight: bold;">${title}</div>
        <div style="font-size: 0.875em;">${message}</div>
    `;
    
    // Add close button
    const closeButton = document.createElement('button');
    closeButton.innerHTML = '&times;';
    closeButton.style.position = 'absolute';
    closeButton.style.top = '8px';
    closeButton.style.right = '8px';
    closeButton.style.background = 'none';
    closeButton.style.border = 'none';
    closeButton.style.color = 'white';
    closeButton.style.fontSize = '16px';
    closeButton.style.cursor = 'pointer';
    closeButton.onclick = () => {
        const parent = notification.parentElement;
        if (parent) parent.removeChild(notification);
    };
    notification.appendChild(closeButton);
    
    // Auto-remove successful notifications after 5 seconds
    if (type === 'success') {
        setTimeout(() => {
            if (document.getElementById(id)) {
                const parent = notification.parentElement;
                if (parent) parent.removeChild(notification);
            }
        }, 5000);
    }
};

// Proper API request helper
const makeApiRequest = async (url: string, options = {}): Promise<any> => {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // Ensure URL doesn't have trailing slashes
    const cleanUrl = url.replace(/\/+$/, '');
    
    const response = await fetch(cleanUrl, { ...defaultOptions, ...options });
    
    if (!response.ok) {
        throw new Error(`API error: ${response.status} ${await response.text()}`);
    }
    
    return await response.json();
};

/**
 * Import data into table via API
 * @param entityType The type of entity
 * @param file The file to import
 * @param tableInfo Table information for the import
 * @returns Promise with the response
 */
export async function importTableData(
    entityType: ObjectType,
    file: File,
    tableInfo: TableFetchResponse
): Promise<TableActionResponse> {
    try {
        // First create FormData to upload the file
        const formData = new FormData();
        formData.append('file', file);

        // Upload the file to get a token - use getApiUrl
        const uploadUrl = getApiUrl('upload');
        const uploadResponse = await fetch(uploadUrl, {
            method: 'POST',
            headers: {
                ...apiConfig.headers
            },
            body: formData
        });

        if (!uploadResponse.ok) {
            const errorText = await uploadResponse.text();
            throw new Error(`Upload API error: ${uploadResponse.status} ${errorText || ''}`);
        }

        const uploadResult = await uploadResponse.json();

        // Create an upload file object
        const uploadFile: UploadFile = {
            fileName: file.name,
            fileType: file.type,
            fileContent: null // We don't need to pass the actual content
        };

        // Create an empty TableRow for import operations
        const data: TableRow = {
            data: { fileToken: uploadResult.token }
        };

        return performTableAction(TableAction.IMPORT, entityType, data, tableInfo, uploadFile);
    } catch (error) {
        console.error('Error importing table data:', error);
        return {
            success: false,
            message: error instanceof Error ? error.message : 'Unknown error'
        } as TableActionResponse;
    }
}
