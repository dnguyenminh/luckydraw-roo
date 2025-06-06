'use client';

import { useState, useEffect, useMemo } from 'react';
import {
  TableFetchRequest,
  TableFetchResponse,
  ObjectType,
  TabTableRow,
  DataObject,
  TableInfo,
  TableRow,
} from '@/app/lib/api/interfaces';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs';
import DataTable from './DataTable';
import { ColumnDef } from './datatable/utils/tableUtils';
import { fetchTableData } from '@/app/lib/api/tableService';
import { Loader, AlertCircle, ChevronRight, Save, X } from 'lucide-react';

export interface EntityDetailTabsProps {
  tableRow: TabTableRow;
  entityType: ObjectType;
  tableInfo?: TableFetchResponse;
  search?: Record<ObjectType, DataObject>;
  isEditing?: boolean;
  isNewRow?: boolean;
  onCancelEdit?: () => void;
  onSaveEdit?: (editedData: any) => void;
  columns?: ColumnDef[]; // Add columns property
  excludedStatusOptions?: string[]; // Add excludedStatusOptions property
}

const EntityDetailTabs: React.FC<EntityDetailTabsProps> = ({
  tableRow,
  entityType,
  tableInfo,
  search,
  isEditing = false,
  isNewRow = false,
  onCancelEdit,
  onSaveEdit,
  columns, // Add columns parameter
  excludedStatusOptions = [] // Add excludedStatusOptions with empty array default
}) => {
  // State for tab management and data loading
  const [activeTab, setActiveTab] = useState<string>('details');
  const [loadedTabs, setLoadedTabs] = useState<Record<string, boolean>>({ details: true });
  const [relatedTableData, setRelatedTableData] = useState<Record<string, TableFetchResponse | null>>({});
  const [loading, setLoading] = useState<Record<string, boolean>>({});
  const [error, setError] = useState<Record<string, string | null>>({});
  
  // State for editing
  const [editedData, setEditedData] = useState<Record<string, any>>({});

  // Add the missing getStatusOptions function
  const getStatusOptions = () => {
    // Default status options available in the system
    const defaultOptions = ['ACTIVE', 'INACTIVE', 'DELETE'];
    
    // Filter out any excluded options (like 'DELETE')
    return defaultOptions.filter(option => 
      !excludedStatusOptions.includes(option)
    );
  };

  // Extract the actual data object, handling both TabTableRow format and direct data objects
  const rowData = useMemo(() => {
    console.log("Normalizing tableRow data structure:", tableRow);

    // Case 1: TableRow has proper data property (expected structure)
    if (tableRow?.data) {
      return tableRow.data;
    }

    // Case 2: TableRow is itself the data object (direct data object)
    // Check if it has typical data fields but not TabTableRow structure
    if (tableRow && typeof tableRow === 'object' &&
      !('relatedTables' in tableRow) && !('tableInfo' in tableRow)) {
      console.log("TableRow appears to be direct data object, using as row data");
      return tableRow;
    }

    // Case 3: No valid data found
    console.warn("No valid data found in tableRow");
    return null;
  }, [tableRow]);

  // Initialize editedData from rowData when entering edit mode
  useEffect(() => {
    if (isEditing && rowData) {
      setEditedData({ ...rowData });
    }
  }, [isEditing, rowData]);

  // Inspect tableRow for debugging - but don't modify it
  useEffect(() => {
    console.log("Full tableRow object:", tableRow);
    console.log("tableRow.relatedTables:", tableRow?.relatedTables);
  }, [tableRow]);

  // Get related tables - strictly from tableRow.relatedTables only
  const relatedTables = useMemo(() => {
    console.log("Getting related tables strictly from tableRow.relatedTables");

    // Only use tableRow.relatedTables if available
    if (tableRow?.relatedTables && Array.isArray(tableRow.relatedTables)) {
      console.log("Found relatedTables in tableRow:", tableRow.relatedTables);
      return tableRow.relatedTables;
    }

    // Return empty array if no related tables found
    console.log("No relatedTables found in tableRow, showing empty list");
    return [];
  }, [tableRow]);

  // Get tabs from search parameter
  const searchTabs = useMemo(() => {
    if (!search) return [];
    return Object.keys(search)
      .filter(key => {
        // Get the enum value for current entity type
        const currentEntityTypeEnum = ObjectType[entityType.toUpperCase() as keyof typeof ObjectType];
        // Compare the string key with the current entity type value
        return key !== String(currentEntityTypeEnum);
      });
  }, [search, entityType]);

  // Combine all tabs
  const allTabs = useMemo(() => {
    const tabs = ['details', ...searchTabs, ...relatedTables];
    return Array.from(new Set(tabs)); // Remove duplicates
  }, [searchTabs, relatedTables]);

  // Set active tab to details when entering edit mode
  useEffect(() => {
    if (isEditing) {
      setActiveTab('details');
    }
  }, [isEditing]);

  // Handle field change in edit mode
  const handleFieldChange = (key: string, value: any) => {
    setEditedData(prev => ({
      ...prev,
      [key]: value
    }));
  };

  // Helper to get ObjectType from tab name
  const getObjectTypeForTab = (tabName: string): ObjectType => {
    // Check if tabName is an ObjectType key
    if (tabName in ObjectType) {
      return ObjectType[tabName as keyof typeof ObjectType];
    }

    // Check in numeric keys (from search tabs)
    if (search && tabName in search) {
      return ObjectType[tabName as keyof typeof ObjectType];
    }

    // Common mappings
    const mappings: Record<string, ObjectType> = {
      'PARTICIPANT': ObjectType.Participant,
      'EVENT': ObjectType.Event,
      'REWARD': ObjectType.Reward,
      'SPIN_HISTORY': ObjectType.SpinHistory,
      'USER': ObjectType.User,
      'BLACKLISTED_TOKEN': ObjectType.BlacklistedToken
    };

    return mappings[tabName.toUpperCase()];
  };

  // Load data when tab changes
  useEffect(() => {
    loadRelatedTableData(activeTab, loadedTabs, relatedTables); 
  }, [activeTab, loadedTabs, relatedTables]);

  const loadRelatedTableData = async (activeTab: string, loadedTabs: Record<string, boolean>, relatedTables: string[]) => {
    if (activeTab === 'details' || loadedTabs[activeTab]) return;

    // Mark this tab as loaded
    setLoadedTabs(prev => ({ ...prev, [activeTab]: true }));

    // Load data if it's a related table
    if (relatedTables.includes(activeTab)) {
      const objectType = getObjectTypeForTab(activeTab);
      if (objectType !== undefined) {
        const searchContext: Record<ObjectType, DataObject> = search || {} as Record<ObjectType, DataObject>;
        searchContext[entityType] = {
          objectType: entityType,
          key: tableInfo?.key || '',
          fieldNameMap: tableInfo?.fieldNameMap || {},
          description: '',
          data: tableRow as TableRow
        } as DataObject;

        const tabId = String(objectType);

        if (loading[tabId]) return;

        setLoading(prev => ({ ...prev, [tabId]: true }));
        setError(prev => ({ ...prev, [tabId]: null }));
        try {

          // Create and execute request
          const request: TableFetchRequest = {
            page: 0,
            size: 20,
            sorts: [],
            filters: [],
            search: searchContext,
            objectType: objectType,
            entityName: objectType
          };

          const response = await fetchTableData(request);
          setRelatedTableData(prev => ({ ...prev, [tabId]: response }));
        } catch (err) {
          const message = err instanceof Error ? err.message : 'Failed to load data';
          setError(prev => ({ ...prev, [tabId]: message }));
        } finally {
          setLoading(prev => ({ ...prev, [tabId]: false }));
        }
      }
    }
  }

  // Format entity field names for display
  const formatFieldName = (key: string): string => {
    return key.charAt(0).toUpperCase() +
      key.slice(1).replace(/([A-Z])/g, ' $1').trim();
  };

  // Format tab labels
  const formatTabLabel = (tabName: string): string => {
    return tabName
      .split('_')
      .map(part => part.charAt(0).toUpperCase() + part.slice(1).toLowerCase())
      .join(' ');
  };

  // Format field values for display
  const formatFieldValue = (value: any): string => {
    if (value === null || value === undefined) return '-';

    if (value instanceof Date) {
      return value.toLocaleString();
    }

    if (typeof value === 'string') {
      if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/.test(value)) {
        try {
          return new Date(value).toLocaleString();
        } catch (e) {
          return value;
        }
      }
      return value;
    }

    if (typeof value === 'boolean') {
      return value ? 'Yes' : 'No';
    }

    if (typeof value === 'object') {
      if (value.name) return value.name;
      if (value.id) return `ID: ${value.id}`;
      return JSON.stringify(value);
    }

    return String(value);
  };

  // Render editable field based on field type
  const renderEditableField = (key: string, value: any, type: string) => {
    // Add 'version' to the list of non-editable fields
    if (key === 'id' || key === 'version' || key.includes('createdBy') || key.includes('updatedBy') || 
        key.includes('createdDate') || key.includes('lastModifiedDate')) {
      return formatFieldValue(value);
    }

    if (typeof value === 'boolean' || value === true || value === false) {
      return (
        <select
          className="w-full bg-[#2d2d2d] text-white p-2 rounded border border-[#3c3c3c]"
          value={editedData[key] === true ? "true" : editedData[key] === false ? "false" : ""}
          onChange={(e) => handleFieldChange(key, e.target.value === "true" ? true : e.target.value === "false" ? false : null)}
        >
          <option value="">Select</option>
          <option value="true">Yes</option>
          <option value="false">No</option>
        </select>
      );
    }

    if (type === 'date') {
      const dateValue = value ? new Date(value).toISOString().split('T')[0] : '';
      return (
        <input
          type="date"
          className="w-full bg-[#2d2d2d] text-white p-2 rounded border border-[#3c3c3c]"
          value={editedData[key] ? new Date(editedData[key]).toISOString().split('T')[0] : ''}
          onChange={(e) => handleFieldChange(key, e.target.value)}
        />
      );
    }

    if (type === 'datetime') {
      const datetimeValue = value ? new Date(value).toISOString().slice(0, 16) : '';
      return (
        <input
          type="datetime-local"
          className="w-full bg-[#2d2d2d] text-white p-2 rounded border border-[#3c3c3c]"
          value={editedData[key] ? new Date(editedData[key]).toISOString().slice(0, 16) : ''}
          onChange={(e) => handleFieldChange(key, e.target.value)}
        />
      );
    }

    if (typeof value === 'number') {
      return (
        <input
          type="number"
          className="w-full bg-[#2d2d2d] text-white p-2 rounded border border-[#3c3c3c]"
          value={editedData[key] || ''}
          onChange={(e) => handleFieldChange(key, e.target.value ? Number(e.target.value) : null)}
        />
      );
    }
    
    return (
      <input
        type="text"
        className="w-full bg-[#2d2d2d] text-white p-2 rounded border border-[#3c3c3c]"
        value={editedData[key] || ''}
        onChange={(e) => handleFieldChange(key, e.target.value)}
      />
    );
  };

  // Render the detailed information about an entity in a card format
  const renderEntityDetails = (data: any, title: string = 'Details') => {
    console.log("renderEntityDetails called with data:", data);

    if (!data || Object.keys(data).length === 0) {
      const emptyData = {
        status: 'ACTIVE', // Default status for new entities
        // Add any other default values needed for new entities
      };

      if (isEditing) {
        // If we're creating a new entity, show empty form fields
        return (
          <div className="space-y-6 p-4 bg-[#1e1e1e] rounded border border-[#3c3c3c]">
            <h2 className="text-lg font-medium text-white border-b border-[#3c3c3c] pb-2">
              Create New {entityType} <span className="text-[#007acc] ml-2">(Edit Mode)</span>
            </h2>
            
            {/* Render edit form with empty data */}
            {renderEntityDetails(emptyData, `New ${entityType}`)}
          </div>
        );
      }
      
      console.warn("No data available for entity details");
      return <div className="p-4 text-center text-gray-400">No data available for {title}</div>;
    }

    const excludedFields = ['relatedTables'];
    // Exclude id field from details view and currentServerTime field by default
    excludedFields.push('currentServerTime', 'id');
    
    // When adding a new entity, also exclude all audit fields as they don't exist yet
    if (isEditing && isNewRow) {
      excludedFields.push(
        'createdBy', 'updatedBy', 'lastUpdatedBy', 
        'createdDate', 'lastModifiedDate', 'updatedDate',
        'createdAt', 'updatedAt', 'created', 'updated' // Add additional date field variations
      );
    }
    
    // Replace idField with viewIdField
    const viewIdField = data.viewId !== undefined ? { viewId: data.viewId } : {};
    const statusField = data.status !== undefined ? { status: data.status } : {};
    const dateFields: Record<string, any> = {};
    const simpleFields: Record<string, any> = {};
    const complexFields: Record<string, any> = {};
    const auditFields: Record<string, any> = {}; // New separate category for audit fields

    // Use columns metadata if available
    const columnDefs = columns ? 
      columns.reduce((acc, col) => ({ ...acc, [col.key]: col }), {} as Record<string, ColumnDef>) : 
      null;

    Object.entries(data).forEach(([key, value]) => {
      // Skip excluded fields and fields marked as hidden in columns definition
      if (excludedFields.includes(key) || 
          key === 'id' || 
          key === 'status' ||
          (columnDefs && columnDefs[key]?.hidden) ||
          // Also exclude fields that match any of the audit field patterns in new mode
          (isNewRow && excludedFields.some(pattern => key.toLowerCase().includes(pattern.toLowerCase())))) {
        return;
      }

      // Separate audit fields into their own category but don't exclude them when NOT in new mode
      if (!isNewRow && (
          key === 'createdBy' || key === 'updatedBy' || key === 'lastUpdatedBy' ||
          key === 'createdDate' || key === 'lastModifiedDate' || key === 'updatedDate' ||
          key === 'createdAt' || key === 'updatedAt' || // Add explicit checks for these fields
          key.toLowerCase().includes('created') || key.toLowerCase().includes('updated') ||
          key.toLowerCase().includes('modified'))) {
        
        // For date values
        if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/.test(value)) {
          auditFields[key] = value;
        } else {
          auditFields[key] = value;
        }
        return;
      }

      if (value !== null && typeof value === 'object') {
        if (value instanceof Date) {
          dateFields[key] = value;
        } else {
          complexFields[key] = value;
        }
      } else if (
        typeof value === 'string' &&
        /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/.test(value)
      ) {
        dateFields[key] = value;
      } else {
        simpleFields[key] = value;
      }
    });

    return (
      <div className="space-y-6 p-4 bg-[#1e1e1e] rounded border border-[#3c3c3c]">
        <h2 className="text-lg font-medium text-white border-b border-[#3c3c3c] pb-2">
          {title} {isEditing && <span className="text-[#007acc] ml-2">(Edit Mode)</span>}
        </h2>

        {(viewIdField.viewId || statusField.status) && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {viewIdField.viewId && (
              <div className="bg-[#252525] p-3 rounded">
                <div className="text-sm text-gray-400 mb-1">ID</div>
                <div className="font-medium">{viewIdField.viewId}</div>
              </div>
            )}

            {statusField.status && (
              <div className="bg-[#252525] p-3 rounded">
                <div className="text-sm text-gray-400 mb-1">Status</div>
                <div>
                  {isEditing ? (
                    <select
                      className="w-full bg-[#2d2d2d] text-white p-2 rounded border border-[#3c3c3c]"
                      value={editedData.status || ''}
                      onChange={(e) => handleFieldChange('status', e.target.value)}
                    >
                      {getStatusOptions().map(option => (
                        <option key={option} value={option}>
                          {option}
                        </option>
                      ))}
                    </select>
                  ) : (
                    <span className={`px-2 py-1 rounded-full text-xs ${statusField.status === 'ACTIVE' ? 'bg-green-800 text-green-200' :
                      statusField.status === 'INACTIVE' ? 'bg-red-800 text-red-200' :
                        'bg-gray-800 text-gray-200'
                      }`}>
                      {statusField.status}
                    </span>
                  )}
                </div>
              </div>
            )}
          </div>
        )}

        {Object.keys(simpleFields).length > 0 && (
          <div>
            <h3 className="text-md font-medium text-gray-300 mb-3 border-b border-[#3c3c3c] pb-1">
              Basic Information
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {Object.entries(simpleFields)
                .filter(([key]) => {
                  // Additional check for hidden fields based on columns definition
                  if (columnDefs && columnDefs[key]) {
                    return isEditing ? columnDefs[key].editable !== false : true;
                  }
                  return true;
                })
                .map(([key, value]) => (
                  <div key={key} className="bg-[#252525] p-3 rounded">
                    <div className="text-sm text-gray-400 mb-1">{formatFieldName(key)}</div>
                    <div className="font-medium overflow-hidden text-ellipsis">
                      {isEditing 
                        ? renderEditableField(key, value, typeof value === 'boolean' ? 'boolean' : 'text')
                        : formatFieldValue(value)
                      }
                    </div>
                  </div>
                ))}
            </div>
          </div>
        )}

        {Object.keys(dateFields).length > 0 && (
          <div>
            <h3 className="text-md font-medium text-gray-300 mb-3 border-b border-[#3c3c3c] pb-1">
              Date & Time Information
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {Object.entries(dateFields)
                .filter(([key]) => {
                  // Only filter out currentServerTime in edit mode, not audit fields
                  if (isEditing && key === 'currentServerTime') {
                    return false;
                  }
                  
                  // Filter out created/updated fields in new mode
                  if (isNewRow && (
                      key === 'createdAt' || key === 'updatedAt' ||
                      key.toLowerCase().includes('created') || 
                      key.toLowerCase().includes('updated')
                  )) {
                    return false;
                  }
                  
                  // Additional check for hidden fields based on columns definition
                  if (columnDefs && columnDefs[key]) {
                    return isEditing ? columnDefs[key].editable !== false : true;
                  }
                  
                  return true;
                })
                .map(([key, value]) => (
                  <div key={key} className="bg-[#252525] p-3 rounded">
                    <div className="text-sm text-gray-400 mb-1">{formatFieldName(key)}</div>
                    <div className="font-medium">
                      {isEditing && !key.includes('created') && !key.includes('updated') && !key.includes('modified')
                        ? renderEditableField(key, value, 'datetime')
                        : formatFieldValue(value)
                      }
                    </div>
                  </div>
                ))}
            </div>
          </div>
        )}

        {Object.keys(complexFields).length > 0 && (
          <div>
            <h3 className="text-md font-medium text-gray-300 mb-3 border-b border-[#3c3c3c] pb-1">
              Related Information
            </h3>
            <div className="grid grid-cols-1 gap-4">
              {Object.entries(complexFields).map(([key, value]) => (
                <div key={key} className="bg-[#252525] p-3 rounded">
                  <div className="text-sm text-gray-400 mb-1">{formatFieldName(key)}</div>
                  <div className="font-medium break-all">{formatFieldValue(value)}</div>
                </div>
              ))}
            </div>
          </div>
        )}
        
        {/* Only show Audit Information section if we have audit fields AND we're not adding a new entity */}
        {Object.keys(auditFields).length > 0 && !isNewRow && (
          <div>
            <h3 className="text-md font-medium text-gray-300 mb-3 border-b border-[#3c3c3c] pb-1">
              Audit Information
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {Object.entries(auditFields).map(([key, value]) => (
                <div key={key} className="bg-[#252525] p-3 rounded">
                  <div className="text-sm text-gray-400 mb-1">{formatFieldName(key)}</div>
                  <div className="font-medium">{formatFieldValue(value)}</div>
                </div>
              ))}
            </div>
          </div>
        )}

        {isEditing && (
          <div className="flex justify-end space-x-3 pt-4 mt-4 border-t border-[#3c3c3c]">
            <button 
              className="flex items-center px-4 py-2 bg-[#3c3c3c] text-white rounded hover:bg-[#4c4c4c]"
              onClick={onCancelEdit}
              type="button"
            >
              <X size={16} className="mr-2" /> {isNewRow ? "Cancel Add" : "Cancel Edit"}
            </button>
            <button 
              className="flex items-center px-4 py-2 bg-[#007acc] text-white rounded hover:bg-[#0069ac]"
              onClick={() => onSaveEdit && onSaveEdit(editedData)}
              type="button"
            >
              <Save size={16} className="mr-2" /> {isNewRow ? "Create" : "Save Changes"}
            </button>
          </div>
        )}
      </div>
    );
  };

  // Render search item details tab
  const renderSearchItemTab = (tabKey: string) => {
    console.log(`Rendering search tab for key: ${tabKey}, search:`, search);

    if (!search || !search[tabKey as unknown as ObjectType]) {
      return <div className="p-4 text-center text-gray-400">No search data available for {tabKey}</div>;
    }

    const searchItem = search[tabKey as unknown as ObjectType];
    console.log(`Search item for ${tabKey}:`, searchItem);

    if (searchItem?.data?.data) {
      return renderEntityDetails(
        searchItem.data.data,
        `${formatTabLabel(tabKey)} Information`
      );
    }

    return <div className="p-4 text-center text-gray-400">No details available for {formatTabLabel(tabKey)}</div>;
  };

  // Render related table content
  const renderRelatedTableTab = (tabName: string) => {
    const objectType = getObjectTypeForTab(tabName);
    const tabId = String(objectType);

    if (!objectType) {
      return (
        <div className="p-4 text-center text-red-500">
          <AlertCircle className="h-6 w-6 mx-auto mb-2" />
          <p>Configuration error: No matching ObjectType found for "{tabName}"</p>
        </div>
      );
    }

    if (loading[tabId]) {
      return (
        <div className="p-8 flex flex-col items-center justify-center">
          <Loader className="h-8 w-8 text-[#007acc] animate-spin mb-2" />
          <p className="text-gray-400">Loading related {formatTabLabel(tabName)} data...</p>
        </div>
      );
    }

    if (error[tabId]) {
      return (
        <div className="p-6 text-center">
          <div className="bg-red-900/20 p-4 rounded-md mb-3">
            <p className="text-red-400 mb-2">{error[tabId]}</p>
            <button
              onClick={() => loadRelatedTableData(activeTab, loadedTabs, relatedTables)}
              className="bg-[#3c3c3c] hover:bg-[#4c4c4c] text-white px-3 py-1 rounded"
            >
              Retry
            </button>
          </div>
        </div>
      );
    }

    const tableData = relatedTableData[tabId];

    if (tableData) {
      const mergedSearch: Record<ObjectType, DataObject> = search || {} as Record<ObjectType, DataObject>;

      mergedSearch[entityType] = {
        objectType: entityType,
        key: tableInfo?.key || '',
        fieldNameMap: tableInfo?.fieldNameMap || {},
        description: '',
        data: tableRow,
        order: 0
      } as DataObject;

      return (
        <DataTable
          data={tableData}
          entityType={objectType}
          showDetailView={true}
          detailViewMode="tabs"
          emptyMessage={`No ${tabName.toLowerCase()} records found related to this ${entityType}`}
          search={mergedSearch}
        />
      );
    }

    return (
      <div className="p-8 flex flex-col items-center justify-center text-gray-400">
        <ChevronRight className="h-6 w-6 mb-2" />
        <p className="mb-3">Click the button below to load related {formatTabLabel(tabName)}.</p>
        <button
          onClick={() => loadRelatedTableData(activeTab, loadedTabs, relatedTables)}
          className="bg-[#007acc] hover:bg-[#0069ac] text-white px-4 py-2 rounded"
        >
          Load Data
        </button>
      </div>
    );
  };

  if (!rowData || Object.keys(rowData).length === 0) {
    const emptyData = {
      status: 'ACTIVE', // Default status for new entities
      // Add any other default values needed for new entities
    };

    if (isEditing) {
      // When creating a new entity, show only the edit form
      return (
        <div className="space-y-6 p-4 bg-[#1e1e1e] rounded border border-[#3c3c3c]">
          <h2 className="text-lg font-medium text-white border-b border-[#3c3c3c] pb-2">
            Create New {entityType} <span className="text-[#007acc] ml-2">(Edit Mode)</span>
          </h2>
          
          {/* Render edit form with empty data */}
          {renderEntityDetails(emptyData, `New ${entityType}`)}
        </div>
      );
    }
    
    console.warn("EntityDetailTabs: No rowData available");
    return <div className="p-4 text-center text-gray-400">No entity data available</div>;
  }

  return (
    <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
      <TabsList className="bg-[#252525] border-b border-[#3c3c3c] w-full flex overflow-x-auto">
        {allTabs.map(tab => (
          <TabsTrigger
            key={tab}
            value={tab}
            className="data-[state=active]:bg-[#2a2d2e] data-[state=active]:shadow-none"
            disabled={isEditing && tab !== 'details'}
          >
            {formatTabLabel(tab)}
          </TabsTrigger>
        ))}
      </TabsList>

      <TabsContent value="details" className="pt-4">
        {renderEntityDetails(rowData, `${entityType} Details`)}
      </TabsContent>

      {searchTabs.map(tab => (
        <TabsContent key={tab} value={tab} className="pt-4">
          {activeTab === tab && renderSearchItemTab(tab)}
        </TabsContent>
      ))}

      {relatedTables.map(tab => (
        <TabsContent key={tab} value={tab} className="pt-4">
          {activeTab === tab && renderRelatedTableTab(tab)}
        </TabsContent>
      ))}
    </Tabs>
  );
};

export default EntityDetailTabs;
