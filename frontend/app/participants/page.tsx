'use client';

import EntityListPage from '@/app/components/common/EntityListPage';
import { ObjectType } from '../lib/api/interfaces';
import { ActionDef } from '@/app/components/common/DataTable';
import { Download, Upload } from 'lucide-react';

export default function ParticipantsPage() {
  // Define row-level actions for the DataTable
  const tableActions: ActionDef[] = [
    {
      label: "Export",
      onClick: () => {}, // This is intentionally empty as the actual export will be handled by DataTable
      color: "blue",
      iconLeft: <Download size={14} />,
      showDetail: false,
      isTableAction: true // This will be our marker that this is a table-level action, not a row-level action
    },
    {
      label: "Import",
      onClick: () => {}, // This is intentionally empty as the actual import will be handled by DataTable
      color: "green",
      iconLeft: <Upload size={14} />,
      showDetail: false,
      isTableAction: true // This will be our marker that this is a table-level action, not a row-level action
    }
  ];

  return (
    <EntityListPage
      title="Participants"
      entityType={ObjectType.Participant}
      breadcrumbPath="participants"
      description="Overview of participation and engagement"
      showSearchBox={true}
      actions={tableActions} // Pass the table actions to EntityListPage
    />
  );
}
