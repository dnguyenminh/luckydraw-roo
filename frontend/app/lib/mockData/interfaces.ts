// Common interfaces for mock data

// Request and response interfaces
export interface SortRequest {
  field: string;
  order: string;
}

export interface FilterRequest {
  field: string;
  operator: string;
  value: string;
}

export interface SearchRequest {
  field: string;
  value: string;
}

// Define ObjectType enum to match the PUML diagram
export enum ObjectType {
  EVENT = 'Event',
  REGION = 'Region',
  PROVINCE = 'Province',
  REWARD = 'Reward',
  GOLDEN_HOUR = 'GoldenHour',
  PARTICIPANT = 'Participant',
  SPIN_HISTORY = 'SpinHistory',
  AUDIT_LOG = 'AuditLog',
  STATIS = 'Statis',
  USER = 'User',
  ROLE = 'Role'
}

export interface TableFetchRequest {
  page: number;
  size: number;
  sorts: {field: string; order: string}[];
  filters: {field: string; value: any; operator: string}[];
  search: Record<string, string>;
  objectType: ObjectType; // Remove the optional marker (?)
}

// Enums for field and sort types
export enum SortType {
  ASCENDING = 'ASCENDING',
  DESCENDING = 'DESCENDING',
  NONE = 'NONE'
}

export enum FieldType {
  STRING = 'STRING',
  NUMBER = 'NUMBER',
  BOOLEAN = 'BOOLEAN',
  DATE = 'DATE',
  DATETIME = 'DATETIME',
  TIME = 'TIME',
  OBJECT = 'OBJECT'
}

// Column information structure
export interface Column {
  fieldName: string;
  fieldType: FieldType;
  sortType: SortType;
  displayName: string;
  filterable: boolean;
}

// Table row structure
export interface TableRow {
  data: Record<string, any>; // Actual row data
}

// Extended table row with related tables
export interface TabTableRow extends TableRow {
  // Empty extension - implementation will happen in the specific context
}

// Table information structure
export interface TableInfo {
  totalPages: number;
  currentPage: number;
  pageSize: number;
  totalElements: number;
  tableName: string;
  columns?: Column[]; // Type info for columns
  rows: TableRow[];
  relatedLinkedObjects?: RelatedLinkedObjectsMap; // Only use related linked objects
  first?: boolean;
  last?: boolean;
  empty?: boolean;
  numberOfElements?: number;
}

// Define interface for related linked objects
export interface RelatedLinkedObject {
  objectType: ObjectType;
  id: number | string;
  name: string;
  [key: string]: any; // Additional properties
}

export interface RelatedLinkedObjectsMap {
  [relationName: string]: {
    [entityId: string]: RelatedLinkedObject[];
  };
}

// Chart information structure
export interface ChartInfo {
  chartName: string;
  chartType: string;
  chartData: Record<string, string[]>;
}

// Statistics information structure
export interface StatisticInfo {
  charts: Record<string, ChartInfo[]>;
}

// Table fetch response - extends TableInfo with additional fields
export interface TableFetchResponse extends TableInfo {
  originalRequest: TableFetchRequest;
  statistics: any;
}

// Entity interfaces
export interface Event {
  id: number;
  name: string;
  description: string;
  startDate: string;
  endDate: string;
  status: string;
  participantCount: number;
  winnerCount: number;
  spinCount: number;
  rewardCount: number;
}

export interface EventDetail {
  id: number;
  name: string;
  description: string;
  startDate: string;
  endDate: string;
  status: string;
  participantCount: number;
  winnerCount: number;
  spinCount: number;
  rewardCount: number;
  createdBy: string;
  createdDate: string;
  modifiedBy: string;
  modifiedDate: string;
  regions: { id: number; name: string }[];
  rewards: { id: number; name: string; quantity: number }[];
}

export interface Region {
  id: number;
  name: string;
  code: string;
  provinceCount: number;
  participantCount: number;
  status: string;
}

export interface RegionDetail {
  id: number;
  name: string;
  code: string;
  description: string;
  created: string;
  lastModified: string;
  totalProvinces: number;
  activeProvinces: number;
  totalParticipants: number;
  activeParticipants: number;
  totalEvents: number; // Add the missing property
}

export interface Province {
  id: number;
  name: string;
  regionName: string;
  population: number;
  participantCount: number;
  status: string;
}

export interface ProvinceDetail {
  id: number;
  name: string;
  regionName: string;
  regionId: number;
  description: string;
  created: string;
  lastModified: string;
  population: number;
  totalParticipants: number;
  activeParticipants: number;
}

export interface Participant {
  id: number;
  name: string;
  email: string;
  province: string;
  status: string;
  spins: number;
  wins: number;
}

export interface ParticipantDetail {
  id: number;
  fullName: string;
  email: string;
  phone: string;
  province: string;
  address: string;
  joinDate: string;
  status: string;
  totalSpins: number;
  availableSpins: number;
  winCount: number;
}

export interface Reward {
  id: number;
  name: string;
  value: number;
  quantity: number;
  claimed: number;
  status: string;
  eventName: string;
}

export interface RewardDetail {
  id: number;
  name: string;
  description: string;
  value: number;
  quantity: number;
  claimed: number;
  available: number;
  winRate: string;
  totalClaimed: number;
  totalWinners: number;
  currentEvent: {
    id: number;
    name: string;
    startTime: string;
    endTime: string;
    status: string;
  }
}

export interface GoldenHour {
  id: number;
  name: string;
  startTime: string;
  endTime: string;
  startDate: string;
  endDate: string;
  multiplier: number;
  status: string;
}

export interface GoldenHourDetail {
  id: number;
  name: string;
  description: string;
  startTime: string;
  endTime: string;
  startDate: string;
  endDate: string;
  multiplier: number;
  totalParticipants: number;
  totalSpins: number;
  totalWinners: number;
  currentEvent: {
    id: number;
    name: string;
    startTime: string;
    endTime: string;
    status: string;
  }
}

export interface ParticipantProfile {
  id: number;
  name: string;
  email: string;
  phone: string;
  province: string;
  address: string;
  registrationDate: string;
  status: string;
  totalSpins: number;
  spinsRemaining: number;
  winCount: number;
}
