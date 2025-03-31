'use client';

import { useState } from 'react';
import ShellLayout from '@/app/components/VSCodeLayout/ShellLayout';
import DataTable from '@/app/components/common/DataTable';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink } from '@/components/ui/breadcrumb';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { ObjectType } from '@/app/lib/mockData'; // Import ObjectType enum

interface StatCard {
  label: string;
  value: string | number;
  color?: 'default' | 'green' | 'yellow' | 'blue' | 'red';
}

interface Tab {
  id: string;
  label: string;
}

interface EntityListPageProps {
  title: string;
  entityType: keyof typeof ObjectType; // Update to use ObjectType keys
  breadcrumbPath: string;
  description?: string;
  statsCards: StatCard[];
  tabs: Tab[];
  addButtonLabel?: string;
  onAddButtonClick?: () => void;
  additionalActions?: React.ReactNode;
}

export default function EntityListPage({
  title,
  entityType,
  breadcrumbPath,
  description = `Manage all ${title.toLowerCase()} in the system`,
  statsCards = [],
  tabs = [
    { id: 'all', label: 'All' },
    { id: 'active', label: 'Active' },
    { id: 'inactive', label: 'Inactive' }
  ],
  addButtonLabel,
  onAddButtonClick,
  additionalActions
}: EntityListPageProps) {
  const [activeTab, setActiveTab] = useState(tabs.length > 0 ? tabs[0].id : 'all');
  
  const getColorClass = (color: StatCard['color']) => {
    switch(color) {
      case 'green': return 'text-green-500';
      case 'yellow': return 'text-yellow-500';
      case 'blue': return 'text-blue-500';
      case 'red': return 'text-red-500';
      default: return '';
    }
  };

  return (
    <ShellLayout>
      <main className="container mx-auto py-6 px-4">
        <Breadcrumb className="mb-6">
          <BreadcrumbItem>
            <BreadcrumbLink href="/">Home</BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbItem isCurrentPage>
            <BreadcrumbLink href={`/${breadcrumbPath}`}>{title}</BreadcrumbLink>
          </BreadcrumbItem>
        </Breadcrumb>
        
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold">{title} Management</h1>
          {additionalActions}
        </div>
        
        {statsCards.length > 0 && (
          <Card className="mb-6">
            <CardHeader>
              <CardTitle>{title} Statistics</CardTitle>
              {description && <CardDescription>{description}</CardDescription>}
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                {statsCards.map((stat, index) => (
                  <div key={index} className="bg-[#2d2d2d] p-4 rounded-md flex flex-col items-center">
                    <p className={`text-2xl font-bold ${getColorClass(stat.color)}`}>{stat.value}</p>
                    <p className="text-sm text-gray-400">{stat.label}</p>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        )}
        
        <Card>
          <CardHeader>
            <div className="flex justify-between items-center">
              <CardTitle>{title}</CardTitle>
              {tabs.length > 0 && (
                <div className="flex space-x-2">
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
              )}
            </div>
          </CardHeader>
          <CardContent>
            <DataTable 
              entityType={entityType}
              detailViewMode="tabs"
              activeTab={activeTab}
              statusField="status"
              addItemButton={addButtonLabel ? {
                label: addButtonLabel,
                onClick: onAddButtonClick || (() => console.log(`Add ${entityType} clicked`))
              } : undefined}
              urlStatePrefix={breadcrumbPath}
            />
          </CardContent>
        </Card>
      </main>
    </ShellLayout>
  );
}
