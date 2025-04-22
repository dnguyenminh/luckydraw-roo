import { ObjectType, TableAction, TableActionRequest, TableActionResponse, FilterRequest, TableRow, TableFetchResponse, UploadFile } from './interfaces';
import { apiConfig, entityApiEndpoints, objectTypeToEndpoint } from './tableService';

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
    return `${String(entityType).toLowerCase()}s`;
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

        const url = `${apiConfig.baseUrl}/table-data/action/${getEntityEndpoint(entityType)}`;
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
            const downloadUrl = `${apiConfig.baseUrl}/table-data/download/${fileName}?token=${downloadToken}`;

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
 * @param id The ID of the record to delete
 * @param tableInfo Table information for the record
 * @returns Promise with the response
 */
export async function deleteRecord(entityType: ObjectType, id: number, tableInfo: TableFetchResponse): Promise<TableActionResponse> {
    // Create a TableRow with just the ID for delete operations
    const data: TableRow = {
        data: { id }
    };

    return performTableAction(TableAction.DELETE, entityType, data, tableInfo);
}

/**
 * Export table data via API
 * @param entityType The type of entity
 * @param tableInfo Table information containing filters, sorts, etc.
 * @returns Promise with the response
 */
export async function exportTableData(
    entityType: ObjectType,
    tableInfo: TableFetchResponse
): Promise<void> {
    try {
        // Create an empty TableRow for export operations
        const data: TableRow = { data: {} };

        await performTableAction(TableAction.EXPORT, entityType, data, tableInfo);
    } catch (error) {
        console.error('Error exporting table data:', error);
        alert(`Export failed: ${error instanceof Error ? error.message : 'Unknown error'}`);
        throw error;
    }
}

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

        // Upload the file to get a token
        const uploadUrl = `${apiConfig.baseUrl}/upload`;
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
