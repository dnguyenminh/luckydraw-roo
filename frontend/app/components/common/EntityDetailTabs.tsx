'use client';

import { ReactNode, useMemo } from 'react';
import { Users, Calendar, BarChart2, History, ClipboardList, Gift, Clock, Map, BookOpen } from 'lucide-react';
import TabInterface from './TabInterface';
import EntityTabContent from './EntityTabContent';
import StatisticsTab from './StatisticsTab';
import { TableFetchResponse, ObjectType } from '@/app/lib/mockData';
import { fetchRelatedTableData } from '@/app/lib/api/tableService';

// Define tab configuration interface
interface TabConfig {
  id: string;
  label: string;
  icon: ReactNode;
  visible: boolean;
  content: ReactNode;
}

// Mapping of relation names to tab configurations
const relationTabMap: Record<string, {
  label: string;
  icon: ReactNode;
  emptyMessage: string;
}> = {
  participants: {
    label: "Participants",
    icon: <Users className="w-4 h-4" />,
    emptyMessage: "No participants found for this entity."
  },
  regions: {
    label: "Regions",
    icon: <Map className="w-4 h-4" />,
    emptyMessage: "No regions associated with this entity."
  },
  provinces: {
    label: "Provinces",
    icon: <Map className="w-4 h-4" />,
    emptyMessage: "No provinces associated with this entity."
  },
  events: {
    label: "Events",
    icon: <Calendar className="w-4 h-4" />,
    emptyMessage: "No events associated with this entity."
  },
  rewards: {
    label: "Rewards",
    icon: <Gift className="w-4 h-4" />,
    emptyMessage: "No rewards associated with this entity."
  },
  goldenHours: {
    label: "Golden Hours",
    icon: <Clock className="w-4 h-4" />,
    emptyMessage: "No golden hours assigned to this entity."
  },
  spinHistory: {
    label: "Spin History",
    icon: <History className="w-4 h-4" />,
    emptyMessage: "No spin history found for this entity."
  },
  auditLog: {
    label: "Audit Log",
    icon: <ClipboardList className="w-4 h-4" />,
    emptyMessage: "No audit log entries found for this entity."
  },
  winners: {
    label: "Winners",
    icon: <Users className="w-4 h-4" />,
    emptyMessage: "No winners found for this entity."
  },
  details: {
    label: "Details",
    icon: <BookOpen className="w-4 h-4" />,
    emptyMessage: "No details available for this entity."
  },
  role: {
    label: "Role",
    icon: <Users className="w-4 h-4" />,
    emptyMessage: "No role assigned to this user."
  },
  users: {
    label: "Users",
    icon: <Users className="w-4 h-4" />,
    emptyMessage: "No users assigned to this role."
  },
  loginHistory: {
    label: "Login History",
    icon: <History className="w-4 h-4" />,
    emptyMessage: "No login history found for this user."
  },
  permissions: {
    label: "Permissions",
    icon: <BookOpen className="w-4 h-4" />,
    emptyMessage: "No permissions assigned."
  }
};

interface EntityDetailTabsProps {
  entityId: number;
  entityTable: TableFetchResponse;
  entityType: keyof typeof ObjectType; // Update to use ObjectType keys
  additionalTabs?: TabConfig[];
  customTabContent?: Record<string, ReactNode>;
}

export default function EntityDetailTabs({
  entityId,
  entityTable,
  entityType,
  additionalTabs = [],
  customTabContent = {}
}: EntityDetailTabsProps) {
  // Find all related tables for this entity
  const relatedTablesKeys = useMemo(() => {
    if (!entityTable.relatedTables) return [];
    
    // Get all available relation types
    const allRelationTypes = Object.keys(entityTable.relatedTables);
    
    // Filter to only relations that exist for this specific entity
    return allRelationTypes.filter(relationType => {
      const relationData = entityTable.relatedTables?.[relationType]?.[entityId];
      // Only include if the relation exists and has rows
      return relationData && relationData.rows && relationData.rows.length > 0;
    });
  }, [entityId, entityTable]);

  // Generate tabs based on related tables
  const tabs = useMemo(() => {
    // Start with statistics tab which is always available
    const baseTabs: TabConfig[] = [
      {
        id: "statistics",
        label: "Statistics",
        icon: <BarChart2 className="w-4 h-4" />,
        visible: true,
        content: customTabContent?.statistics || <StatisticsTab id={entityId} entityType={entityType as any} />
      }
    ];
    
    // Add all related tables as tabs
    const relationTabs = relatedTablesKeys.map(relationKey => {
      const config = relationTabMap[relationKey] || {
        label: relationKey.charAt(0).toUpperCase() + relationKey.slice(1),
        icon: <BookOpen className="w-4 h-4" />,
        emptyMessage: `No ${relationKey} data available.`
      };
      
      return {
        id: relationKey,
        label: config.label,
        icon: config.icon,
        visible: true,
        content: customTabContent?.[relationKey] || (
          <EntityTabContent
            entityId={entityId}
            sourceTable={entityTable}
            relatedTableName={relationKey}
            title={config.label}
            emptyMessage={config.emptyMessage}
          />
        )
      };
    });
    
    // Add any additional custom tabs
    return [...baseTabs, ...relationTabs, ...additionalTabs];
  }, [entityId, entityType, relatedTablesKeys, customTabContent, additionalTabs, entityTable]);

  return (
    <div>
      <TabInterface tabs={tabs} />
    </div>
  );
}
