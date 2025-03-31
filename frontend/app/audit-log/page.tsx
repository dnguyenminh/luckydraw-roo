'use client';

import EntityListPage from '@/app/components/common/EntityListPage';
import { Download } from 'lucide-react';

export default function AuditLogPage() {
  return (
    <EntityListPage
      title="Audit Log"
      entityType="AUDIT_LOG" // Use correct ObjectType key
      breadcrumbPath="audit-log"
      description="System activity tracking and audit trail"
      statsCards={[
        { label: 'Total Entries', value: 1248 },
        { label: 'User Actions', value: 845, color: 'blue' },
        { label: 'System Events', value: 403, color: 'yellow' },
        { label: 'Last 24 Hours', value: 57, color: 'green' }
      ]}
      tabs={[
        { id: 'all', label: 'All Actions' },
        { id: 'update', label: 'Updates' },
        { id: 'add', label: 'Additions' },
        { id: 'delete', label: 'Deletions' }
      ]}
      additionalActions={
        <button className="bg-[#007acc] text-white px-3 py-2 rounded hover:bg-[#0069ac] flex items-center">
          <Download className="h-4 w-4 mr-2" />
          Export Log
        </button>
      }
    />
  );
}
