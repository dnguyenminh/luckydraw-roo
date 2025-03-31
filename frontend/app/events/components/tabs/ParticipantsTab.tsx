'use client';

import { useState } from 'react';
import DataTable from '@/app/components/common/DataTable';
import { TableFetchResponse } from "@/app/lib/mockData";

interface ParticipantsTabProps {
  eventId: number;
  initialData?: TableFetchResponse;
}

export default function ParticipantsTab({ eventId, initialData }: ParticipantsTabProps) {
  const [activeTab, setActiveTab] = useState('all');
  
  const tabs = [
    { id: 'all', label: 'All Participants' },
    { id: 'active', label: 'Active' },
    { id: 'winners', label: 'Winners' }
  ];

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">Event Participants</h2>
      
      <div className="flex space-x-2 mb-4">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            className={`px-3 py-1 rounded ${activeTab === tab.id
              ? 'bg-[#007acc] text-white'
              : 'bg-[#2d2d2d] text-gray-300 hover:bg-[#3c3c3c]'}`}
            onClick={() => setActiveTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </div>
      
      <DataTable 
        data={initialData}
        entityType="PARTICIPANT" // Use correct ObjectType key
        addItemButton={{
          label: "Add Participant",
          onClick: () => console.log(`Add participant to event ${eventId}`)
        }}
        urlStatePrefix={`event_${eventId}_participants`}
        emptyMessage="No participants found in this event. Click 'Add Participant' to add one."
        activeTab={activeTab}
      />
    </div>
  );
}
