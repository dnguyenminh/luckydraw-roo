'use client';

import { ReactNode } from 'react';
import DataTable from '@/app/components/common/DataTable';
import { TableFetchResponse, getRelatedTableData, ObjectType } from '@/app/lib/mockData';
import { ActionDef } from '../common/DataTable';
import { fetchRelatedTableData } from '@/app/lib/api/tableService';

interface EntityTabContentProps {
  entityId: number;
  sourceTable: TableFetchResponse;
  relatedTableName: string;
  title: string;
  emptyMessage?: string;
  actions?: ActionDef[];
  renderDetailView?: (rowData: any) => ReactNode;
  addItemButton?: {
    label: string;
    onClick: () => void;
  };
  filterOptions?: {
    key: string;
    label: string;
    options: { value: string; label: string }[];
  }[];
}

export default function EntityTabContent({
  entityId,
  sourceTable,
  relatedTableName,
  title,
  emptyMessage = "No data found.",
  actions = [],
  renderDetailView,
  addItemButton,
  filterOptions = []
}: EntityTabContentProps) {
  // Get related data from the source table
  const relatedTable = getRelatedTableData(sourceTable, entityId, relatedTableName) || {
    totalPages: 0,
    currentPage: 0,
    pageSize: 0,
    totalElements: 0,
    tableName: `entity_${relatedTableName}`,
    rows: [],
    originalRequest: {
      page: 0,
      size: 10,
      sorts: [{ field: "name", order: "asc" }],
      filters: [],
      search: {}
    },
    statistics: {}
  };

  // Function to fetch data with filtering, pagination, etc.
  const fetchData = async (request: any) => {
    try {
      return await fetchRelatedTableData(
        sourceTable.tableName,
        entityId,
        relatedTableName,
        request
      );
    } catch (error) {
      console.error(`Error fetching ${relatedTableName} for ${sourceTable.tableName} ${entityId}:`, error);
      return relatedTable; // Return existing data if fetch fails
    }
  };

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">{title}</h2>
      <DataTable 
        data={relatedTable}
        actions={actions}
        detailView={renderDetailView}
        filterOptions={filterOptions}
        addItemButton={addItemButton}
        urlStatePrefix={`entity_${entityId}_${relatedTableName}`}
        emptyMessage={emptyMessage}
        fetchData={fetchData}
      />
    </div>
  );
}
