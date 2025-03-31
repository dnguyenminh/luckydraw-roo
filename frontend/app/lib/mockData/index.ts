// Main export file for mock data - imports and exports from all mock data modules

// Export interfaces and types
export * from './interfaces';

// Export mock data tables - Order matters for initialization!
export * from './events';
export * from './participants'; // Export participants before spin history
export * from './spinHistory';  // This will now add relationships to participants
export * from './regions';
export * from './provinces';
export * from './rewards';
export * from './goldenHours';
export * from './auditLog';
export * from './users'; // Export users before roles
export * from './roles'; // This will now add relationships to users

// Export utility functions
export * from './utils';

// Initialize relationships between entities - getting called from individual files now
// so this is here just in case additional initialization is needed in the future
export function initializeMockDataRelationships() {
  // Relationships are now set up within the respective modules
  console.log('All mock data relationships successfully initialized');
}
