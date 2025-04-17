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
import { fetchTableData } from '@/app/lib/api/tableService';
import { Loader, AlertCircle, ChevronRight } from 'lucide-react';

/**
 * EntityDetailTabs Component
 *
 * Renders detailed information about an entity with tabs for:
 * 1. Basic entity details
 * 2. Related tables (dynamically loaded)
 *
 * Features:
 * - Automatically detects related tables
 * - Lazy loads table data when tabs are selected
 * - Formats entity fields by type (ID, status, dates, simple fields, complex fields)
 * - Supports nested entity relationships
 */
export default function EntityDetailTabs({
  entityType,
  tableRow,
  tableInfo,
  search
}: {
  entityType: ObjectType;
  tableRow: TabTableRow;
  tableInfo: TableFetchResponse;
  search?: Record<ObjectType, DataObject>;
}) {
  // State for tab management and data loading
  const [activeTab, setActiveTab] = useState<string>('details');
  const [loadedTabs, setLoadedTabs] = useState<Record<string, boolean>>({ details: true });
  const [relatedTableData, setRelatedTableData] = useState<Record<string, TableFetchResponse | null>>({});
  const [loading, setLoading] = useState<Record<string, boolean>>({});
  const [error, setError] = useState<Record<string, string | null>>({});

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
          key: tableInfo.key,
          fieldNameMap: tableInfo.fieldNameMap,
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

  // Debug output in useEffect
  useEffect(() => {
    console.log("EntityDetailTabs render state:", {
      entityType,
      rowData: !!rowData,
      relatedTables,
      searchTabs,
      allTabs,
      hasRelatedTablesProperty: 'relatedTables' in (tableRow || {}),
      isRelatedTablesArray: Array.isArray(tableRow?.relatedTables),
      relatedTablesLength: tableRow?.relatedTables?.length
    });
  }, [entityType, rowData, relatedTables, searchTabs, allTabs, tableRow]);


  // Format entity field names for display
  const formatFieldName = (key: string): string => {
    return key.charAt(0).toUpperCase() +
      key.slice(1).replace(/([A-Z])/g, ' $1').trim();
  };

  // Format tab labels
  const formatTabLabel = (tabName: string): string => {
    // If it's a numeric ObjectType, find its name
    if (!isNaN(Number(tabName)) && Object.values(ObjectType).includes(tabName as ObjectType)) {
      const enumKey = Object.keys(ObjectType).find(key =>
        ObjectType[key as keyof typeof ObjectType] === tabName
      );
      if (enumKey) {
        return formatFieldName(enumKey);
      }
    }

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
      // Handle ISO date strings
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

  // Render the detailed information about an entity in a card format
  const renderEntityDetails = (data: any, title: string = 'Details') => {
    // Enhanced logging for debugging
    console.log("renderEntityDetails called with data:", data);

    // More robust null check
    if (!data || Object.keys(data).length === 0) {
      console.warn("No data available for entity details");
      return <div className="p-4 text-center text-gray-400">No data available for {title}</div>;
    }

    // Categorize fields for better display
    const excludedFields = ['relatedTables'];
    const idField = data.id !== undefined ? { id: data.id } : {};
    const statusField = data.status !== undefined ? { status: data.status } : {};
    const dateFields: Record<string, any> = {};
    const simpleFields: Record<string, any> = {};
    const complexFields: Record<string, any> = {};

    // Sort fields by category - more robust implementation
    Object.entries(data).forEach(([key, value]) => {
      if (excludedFields.includes(key) || key === 'id' || key === 'status') return;

      // More explicit type checking
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
          {title}
        </h2>

        {/* ID & Status Section */}
        {(idField.id || statusField.status) && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {idField.id && (
              <div className="bg-[#252525] p-3 rounded">
                <div className="text-sm text-gray-400 mb-1">ID</div>
                <div className="font-medium">{idField.id}</div>
              </div>
            )}

            {statusField.status && (
              <div className="bg-[#252525] p-3 rounded">
                <div className="text-sm text-gray-400 mb-1">Status</div>
                <div>
                  <span className={`px-2 py-1 rounded-full text-xs ${statusField.status === 'ACTIVE' ? 'bg-green-800 text-green-200' :
                    statusField.status === 'INACTIVE' ? 'bg-red-800 text-red-200' :
                      'bg-gray-800 text-gray-200'
                    }`}>
                    {statusField.status}
                  </span>
                </div>
              </div>
            )}
          </div>
        )}

        {/* Basic Fields Section */}
        {Object.keys(simpleFields).length > 0 && (
          <div>
            <h3 className="text-md font-medium text-gray-300 mb-3 border-b border-[#3c3c3c] pb-1">
              Basic Information
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {Object.entries(simpleFields).map(([key, value]) => (
                <div key={key} className="bg-[#252525] p-3 rounded">
                  <div className="text-sm text-gray-400 mb-1">{formatFieldName(key)}</div>
                  <div className="font-medium overflow-hidden text-ellipsis">
                    {formatFieldValue(value)}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Date Fields Section */}
        {Object.keys(dateFields).length > 0 && (
          <div>
            <h3 className="text-md font-medium text-gray-300 mb-3 border-b border-[#3c3c3c] pb-1">
              Date & Time Information
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {Object.entries(dateFields).map(([key, value]) => (
                <div key={key} className="bg-[#252525] p-3 rounded">
                  <div className="text-sm text-gray-400 mb-1">{formatFieldName(key)}</div>
                  <div className="font-medium">{formatFieldValue(value)}</div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Complex Fields Section */}
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

    // If it has data, show it
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

    // Show loading state
    if (loading[tabId]) {
      return (
        <div className="p-8 flex flex-col items-center justify-center">
          <Loader className="h-8 w-8 text-[#007acc] animate-spin mb-2" />
          <p className="text-gray-400">Loading related {formatTabLabel(tabName)} data...</p>
        </div>
      );
    }

    // Show error state
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

    // If we have data, render the DataTable
    if (tableData) {
      // Create merged search context for the nested DataTable
      const mergedSearch: Record<ObjectType, DataObject> = search || {} as Record<ObjectType, DataObject>;


      // Add parent entity to search context
      mergedSearch[entityType] = {
        objectType: entityType,
        key: tableInfo.key,
        fieldNameMap: tableInfo.fieldNameMap,
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

    // Show empty state with load button
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

  // More robust empty data check
  if (!rowData) {
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
          >
            {formatTabLabel(tab)}
          </TabsTrigger>
        ))}
      </TabsList>

      {/* Details Tab - use rowData instead of tableRow.data */}
      <TabsContent value="details" className="pt-4">
        {renderEntityDetails(rowData, `${entityType} Details`)}
      </TabsContent>

      {/* Search Item Tabs */}
      {searchTabs.map(tab => (
        <TabsContent key={tab} value={tab} className="pt-4">
          {activeTab === tab && renderSearchItemTab(tab)}
        </TabsContent>
      ))}

      {/* Related Tables Tabs - Lazy Loaded */}
      {relatedTables.map(tab => (
        <TabsContent key={tab} value={tab} className="pt-4">
          {activeTab === tab && renderRelatedTableTab(tab)}
        </TabsContent>
      ))}
    </Tabs>
  );
}
