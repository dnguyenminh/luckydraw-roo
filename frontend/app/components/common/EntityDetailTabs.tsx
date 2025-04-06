'use client';

import { useState, useEffect } from 'react';
import {
  TableFetchRequest,
  TableFetchResponse,
  ObjectType,
  TabTableRow,
  TableRow,
  DataObjectKeyValues,
  DataObjectKey
} from '@/app/lib/api/interfaces';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs';
import DataTable from './DataTable';
import { fetchTableData } from '@/app/lib/api/tableService';

interface EntityDetailTabsProps {
  tableRow: TableRow;
  entityType: string;
}

/**
 * Renders tabs for displaying entity details and related tables
 * Uses the relatedTables array from TabTableRow to determine which related tables to show
 */
export default function EntityDetailTabs({
  tableRow,
  entityType,
}: EntityDetailTabsProps) {
  const [activeTab, setActiveTab] = useState<string>('details');
  const [relatedTableData, setRelatedTableData] = useState<Record<string, TableFetchResponse | null>>({});
  const [loading, setLoading] = useState<Record<string, boolean>>({});
  const [error, setError] = useState<Record<string, string | null>>({});

  // Extract entity ID from the tableRow
  const entityId = tableRow?.data?.id;

  // Check if tableRow is a TabTableRow and has relatedTables
  const isTabTableRow = 'relatedTables' in tableRow;

  // Get the list of related table names from tableRow if it exists
  const relatedTables = isTabTableRow ? (tableRow as TabTableRow).relatedTables || [] : [];

  // Load each related table's data when active tab changes
  useEffect(() => {
    // Skip if active tab is "details" or if there are no related tables
    if (activeTab === 'details' || relatedTables.length === 0) return;
    
    // Otherwise load data for the active tab
    // Convert tab name to ObjectType
    const relatedObjectType = activeTab.toUpperCase() as keyof typeof ObjectType;
    if (relatedObjectType in ObjectType) {
      loadTableData(ObjectType[relatedObjectType], tableRow);
    }
  }, [activeTab, relatedTables, tableRow]);

  // Function to load related table data
  const loadTableData = async (relatedObjectType: ObjectType, tableRow: TableRow) => {
    setLoading(prev => ({ ...prev, [relatedObjectType]: true }));

    try {
      // Get the parent entity's object type
      const parentObjectType = (entityType.toUpperCase() as keyof typeof ObjectType) in ObjectType 
        ? ObjectType[entityType.toUpperCase() as keyof typeof ObjectType] 
        : ObjectType.EVENT;

      // Initialize search criteria with the correct type
      const searchCriteria: Record<ObjectType, DataObjectKeyValues> = Object.values(ObjectType).reduce((acc, type) => {
        acc[type] = { searchCriteria: {} };
        return acc;
      }, {} as Record<ObjectType, DataObjectKeyValues>);
      
      // Add search criteria for the parent entity
      if (tableRow?.data?.id) {
        searchCriteria[parentObjectType].searchCriteria = { id: tableRow.data.id };
      }

      // Create the request with search criteria
      const request: TableFetchRequest = {
        page: 0,
        size: 10,
        sorts: [],
        filters: [],
        search: searchCriteria,
        objectType: relatedObjectType
      };

      // Fetch the data using the request
      const response = await fetchTableData(request);

      // Store the response
      setRelatedTableData(prev => ({
        ...prev,
        [relatedObjectType]: response
      }));

    } catch (err) {
      console.error(`Error loading related table ${relatedObjectType}:`, err);
      setError(prev => ({
        ...prev,
        [relatedObjectType]: `Error loading ${relatedObjectType}`
      }));
    } finally {
      setLoading(prev => ({ ...prev, [relatedObjectType]: false }));
    }
  };

  // Define the tab items (details + related tables)
  const tabs = [
    { id: 'details', label: 'Details' },
    // Add tabs for each related table
    ...relatedTables.map(tableName => ({
      id: tableName,
      label: tableName.charAt(0).toUpperCase() + tableName.slice(1).toLowerCase()
    }))
  ];

  // Render the details content (basic properties of the entity)
  const renderDetailsContent = () => {
    // Access data from tableRow.data
    const excludedFields = ['id', 'relatedTables'];

    return (
      <div className="p-4 bg-[#1e1e1e] rounded">
        <h3 className="text-lg font-medium mb-4">Entity Details</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {tableRow?.data && Object.entries(tableRow.data)
            .filter(([key]) => !excludedFields.includes(key))
            .map(([key, value]) => (
              <div key={key} className="py-2 border-b border-[#3c3c3c]">
                <span className="text-gray-400">{key}: </span>
                <span className="text-white">{
                  value !== null && value !== undefined
                    ? (typeof value === 'object'
                      ? JSON.stringify(value)
                      : value.toString())
                    : 'N/A'
                }</span>
              </div>
            ))}
        </div>
      </div>
    );
  };

  // Render related table content
  const renderRelatedTableContent = (tableName: string) => {
    if (loading[tableName]) {
      return <div className="p-4 text-center">Loading {tableName} data...</div>;
    }

    if (error[tableName]) {
      return <div className="p-4 text-center text-red-500">{error[tableName]}</div>;
    }

    const tableData = relatedTableData[tableName];

    if (!tableData || tableData.rows.length === 0) {
      return <div className="p-4 text-center">No {tableName} data available</div>;
    }

    return (
      <DataTable
        data={tableData}
        entityType={tableName.toUpperCase() as keyof typeof ObjectType}
        showDetailView={false}
      />
    );
  };

  // Don't render tabs if there's no data or related tables
  if (!tableRow?.data) {
    return <div className="p-4 text-center">No data available</div>;
  }

  // If there are no related tables, just show details without tabs
  if (relatedTables.length === 0) {
    return renderDetailsContent();
  }

  // Fix for the string vs ObjectType type mismatch in the onValueChange handler
  const handleTabChange = (value: string) => {
    setActiveTab(value);
  };

  return (
    <Tabs value={activeTab} onValueChange={handleTabChange} className="w-full">
      <TabsList className="bg-[#252525] border-b border-[#3c3c3c] w-full flex overflow-x-auto">
        {tabs.map(tab => (
          <TabsTrigger
            key={tab.id}
            value={tab.id}
            className="data-[state=active]:bg-[#2a2d2e] data-[state=active]:shadow-none"
          >
            {tab.label}
          </TabsTrigger>
        ))}
      </TabsList>

      {/* Details Tab Content */}
      <TabsContent value="details" className="pt-4">
        {renderDetailsContent()}
      </TabsContent>

      {/* Related Tables Content */}
      {relatedTables.map(tableName => (
        <TabsContent key={tableName} value={tableName} className="pt-4">
          {renderRelatedTableContent(tableName)}
        </TabsContent>
      ))}
    </Tabs>
  );
}
