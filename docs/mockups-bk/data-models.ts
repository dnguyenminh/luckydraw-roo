// Common Types
export type Status = 'ACTIVE' | 'INACTIVE';
export type DurationType = 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY';

// Base Interfaces
interface BaseEntity {
  id: number;
  code: string;
  name: string;
  status: Status;
  createdAt: string;
  updatedAt: string;
}

// Main Entities
export interface Event extends BaseEntity {
  startTime: string;
  endTime: string;
  locations: EventLocation[];
}

export interface EventLocation extends BaseEntity {
  eventId: number;
  regionId: number;
  maxCapacity: number;
  defaultWinProbability: number;
  startTime: string;
  endTime: string;
  rewards: EventLocationReward[];
  goldenHours: GoldenHour[];
  participants: ParticipantEvent[];
}

export interface Region extends BaseEntity {
  provinces: Province[];
  eventLocations: EventLocation[];
}

export interface Province extends BaseEntity {
  regionId: number;
  participants: Participant[];
}

export interface Reward extends BaseEntity {
  value: number;
  quantity: number;
  itemsPerDuration: number;
  durationType: DurationType;
  remaining: number;
  spinHistories: SpinHistory[];
  eventLocations: EventLocation[];
}

export interface EventLocationReward {
  eventLocationId: number;
  rewardId: number;
  quantity: number;
  remaining: number;
  itemsPerDuration: number;
  durationType: DurationType;
}

export interface GoldenHour {
  id: number;
  eventLocationId: number;
  startTime: string;
  endTime: string;
  multiplier: number;
  active: boolean;
  spinHistories: SpinHistory[];
}

export interface Participant extends BaseEntity {
  provinceId: number;
  phone: string;
  email: string;
  events: ParticipantEvent[];
}

export interface ParticipantEvent {
  participantId: number;
  eventLocationId: number;
  spinsRemaining: number;
  spinHistories: SpinHistory[];
}

export interface SpinHistory {
  id: number;
  participantEventId: number;
  rewardId: number | null;
  goldenHourId: number | null;
  spinTime: string;
  won: boolean;
}

// Response Types
export interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error?: {
    code: string;
    message: string;
  };
}

// Statistics Types
export interface EventStatistics {
  totalParticipants: number;
  totalSpins: number;
  totalWins: number;
  winRate: number;
  participationByHour: { hour: number; count: number }[];
  participationByLocation: { locationId: number; count: number }[];
  winsByReward: { rewardId: number; count: number }[];
}

export interface LocationStatistics {
  activeEvents: number;
  totalParticipants: number;
  totalSpins: number;
  winRate: number;
  currentCapacityUtilization: number;
  participationByHour: { hour: number; count: number }[];
  rewardAllocation: { rewardId: number; total: number; remaining: number }[];
}

export interface RewardStatistics {
  totalAllocated: number;
  totalRemaining: number;
  winRate: number;
  winsByLocation: { locationId: number; count: number }[];
  winsByHour: { hour: number; count: number }[];
  goldenHourWins: number;
}

export interface ParticipantStatistics {
  totalEvents: number;
  totalSpins: number;
  totalWins: number;
  winRate: number;
  goldenHourParticipation: number;
  spinsByEvent: { eventId: number; count: number }[];
  winsByReward: { rewardId: number; count: number }[];
}

// Filter Types
export interface EventFilter {
  search?: string;
  status?: Status;
  startDate?: string;
  endDate?: string;
  locationId?: number;
  rewardId?: number;
}

export interface LocationFilter {
  search?: string;
  status?: Status;
  regionId?: number;
  provinceId?: number;
  eventId?: number;
  hasGoldenHour?: boolean;
}

export interface RewardFilter {
  search?: string;
  status?: Status;
  durationType?: DurationType;
  minValue?: number;
  maxValue?: number;
  eventId?: number;
  locationId?: number;
}

export interface ParticipantFilter {
  search?: string;
  status?: Status;
  provinceId?: number;
  eventId?: number;
  locationId?: number;
  hasWon?: boolean;
}
