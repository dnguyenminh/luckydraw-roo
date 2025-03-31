'use client';

import EntityListPage from '@/app/components/common/EntityListPage';

export default function RolesPage() {
  return (
    <EntityListPage
      title="Roles"
      entityType="ROLE" // Use correct ObjectType key
      breadcrumbPath="roles"
      description="Manage system roles and permissions"
      statsCards={[
        { label: 'Total Roles', value: 5 },
        { label: 'Active Roles', value: 4, color: 'green' },
        { label: 'Total Users', value: 15, color: 'yellow' },
        { label: 'Permissions', value: 30, color: 'blue' }
      ]}
      tabs={[
        { id: 'all', label: 'All Roles' },
        { id: 'active', label: 'Active' },
        { id: 'inactive', label: 'Inactive' }
      ]}
      addButtonLabel="Add Role"
      onAddButtonClick={() => console.log('Add role clicked')}
    />
  );
}
