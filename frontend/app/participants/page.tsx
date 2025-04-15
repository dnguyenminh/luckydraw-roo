'use client';

import EntityListPage from '@/app/components/common/EntityListPage';
import { ObjectType } from '../lib/api/interfaces';

export default function ParticipantsPage() {
  return (
    <EntityListPage
      title="Participants"
      entityType={ObjectType.Participant} // Use correct ObjectType key
      breadcrumbPath="participants"
      description="Overview of participation and engagement"
      // statsCards={[
      //   { label: 'Total Participants', value: 25642 },
      //   { label: 'Active Participants', value: 18735, color: 'green' },
      //   { label: 'Total Spins', value: 45762, color: 'yellow' },
      //   { label: 'Total Winners', value: 12348, color: 'blue' }
      // ]}
      // tabs={[
      //   { id: 'all', label: 'All Participants' },
      //   { id: 'active', label: 'Active' },
      //   { id: 'inactive', label: 'Inactive' }
      // ]}
      // addButtonLabel="Add Participant"
      // onAddButtonClick={() => console.log('Add Participant clicked')}
    />
  );
}
