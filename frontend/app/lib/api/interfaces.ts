// Common interfaces for API data based on updated UML

// Request and response interfaces
export interface SortRequest {
    field: string;
    sortType: SortType;
}

export interface FilterRequest {
    field: string;
    filterType: FilterType;
    minValue: string;
    maxValue: string;
}

export interface SearchRequest {
    field: string;
    value: string;
}

// Define ObjectType enum to match the updated PUML diagram with keys matching values exactly
export enum ObjectType {
    Event = 'Event',
    Region = 'Region',
    Province = 'Province',
    Reward = 'Reward',
    GoldenHour = 'GoldenHour',
    SpinHistory = 'SpinHistory',
    AuditLog = 'AuditLog',
    Statistics = 'Statistics',
    User = 'User',
    Role = 'Role',
    Permission = 'Permission',
    Configuration = 'Configuration',
    BlacklistedToken = 'BlacklistedToken',
    EventLocation = 'EventLocation',
    Participant = 'Participant',
    ParticipantEvent = 'ParticipantEvent'
}

// Updated to match UML definition
export enum FetchStatus {
    SUCCESS = 'SUCCESS',
    NO_DATA = 'NO_DATA',
    ERROR = 'ERROR',
    INVALID_REQUEST = 'INVALID_REQUEST',
    ACCESS_DENIED = 'ACCESS_DENIED'
}

export enum SortType {
    ASCENDING = 'ASCENDING',
    DESCENDING = 'DESCENDING',
    NONE = 'NONE'
}

export enum FilterType {
    EQUALS = 'EQUALS',
    NOT_EQUALS = 'NOT_EQUALS',
    LESS_THAN = 'LESS_THAN',
    LESS_THAN_OR_EQUALS = 'LESS_THAN_OR_EQUALS',
    GREATER_THAN = 'GREATER_THAN',
    GREATER_THAN_OR_EQUALS = 'GREATER_THAN_OR_EQUALS',
    BETWEEN = 'BETWEEN',
    IN = 'IN',
    NOT_IN = 'NOT_IN',
    CONTAINS = 'CONTAINS',
    STARTS_WITH = 'STARTS_WITH',
    ENDS_WITH = 'ENDS_WITH'
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

// DataObjectKey from UML
export interface DataObjectKey {
    keys: string[];
}


// Updated to match the UML definition
export interface TableFetchRequest {
    objectType: ObjectType;
    entityName?: string;
    page: number;
    size: number;
    sorts: SortRequest[];
    filters: FilterRequest[];
    search: Record<ObjectType, DataObject>;
}

// Column information structure
export interface ColumnInfo {
    fieldName: string;
    fieldType: string;
    sortType: SortType;
}

// Table information structure - updated to match UML
export interface TableInfo {
    key?: DataObjectKey;
    status: FetchStatus;
    message: string;
    totalPage: number;
    currentPage: number;
    pageSize: number;
    totalElements: number;
    tableName: string;
    fieldNameMap: Record<string, ColumnInfo>;
    rows: TableRow[];
    relatedLinkedObjects: Record<string, DataObject>;
    first?: boolean;
    last?: boolean;
    empty?: boolean;
    numberOfElements?: number;
}

// Table row structure updated to match UML with tableInfo reference
export interface TableRow {
    data: Record<string, any>; // Actual row data
    tableInfo?: TableInfo; // Reference to the parent table info
    }

// Extended table row with related tables - updated based on UML
export interface TabTableRow extends TableRow {
    relatedTables: string[]; // List of related table names
}

// DataObject structure from UML
export interface DataObject {
    objectType: ObjectType;
    key: DataObjectKey;
    fieldNameMap: Record<string, ColumnInfo>;
    description: string;
    data: TableRow;
    order: number;
}

// Chart information structure
export interface ChartInfo {
    chartName: string;
    chartType: string;
    chartData: Record<string, string[]>;
}

// Statistics information structure
export interface StatisticsInfo {
    charts: Record<string, ChartInfo[]>;
}

// Table fetch response - extends TableInfo with additional fields
export interface TableFetchResponse extends TableInfo {
    originalRequest: TableFetchRequest;
    statistics: StatisticsInfo;
}

// For backward compatibility
export interface RelatedLinkedObject {
    objectType: ObjectType;
    id: number | string;
    name: string;
    [key: string]: any;
}

export interface RelatedLinkedObjectsMap {
    [relationName: string]: {
        [entityId: string]: RelatedLinkedObject[];
    };
}

// Filter operator interfaces for the enhanced DataTable
export enum TextFilterOperator {
    EQUALS = 'equals',
    CONTAINS = 'contains',
    STARTS_WITH = 'startsWith',
    ENDS_WITH = 'endsWith'
}

export enum NumericFilterOperator {
    EQUALS = 'equals',
    BETWEEN = 'between',
    GREATER_THAN = 'greaterThan',
    LESS_THAN = 'lessThan'
}

export interface FilterState {
    field: string;
    operator: TextFilterOperator | NumericFilterOperator | string;
    value: any;
    secondValue?: any; // For BETWEEN operator
    active: boolean;
}

export interface PaginationState {
    pageIndex: number;
    pageSize: number;
    totalItems: number;
    totalPages: number;
}

// Tab configuration for detail view
export interface DetailTab {
    id: string;
    label: string;
    type: 'details' | 'search' | 'related';
    content?: React.ReactNode;
    entityType?: ObjectType;
    searchCriteria?: Record<ObjectType, DataObject>;
}

