import {
    TableFetchRequest,
    TableFetchResponse,
    ObjectType,
    FetchStatus,
    SortType,
    FilterType,
    DataObject
} from './interfaces';

/**
 * Generate random mock data for entities
 * @param entityType Type of entity to generate
 * @param count Number of entities to generate
 */
function generateMockData(entityType: ObjectType, count: number): any[] {
    const data = [];
    const now = new Date();
    const statuses = ['ACTIVE', 'INACTIVE', 'PENDING', 'COMPLETED'];

    for (let i = 1; i <= count; i++) {
        const createdDate = new Date(now);
        createdDate.setDate(now.getDate() - Math.floor(Math.random() * 30));

        // Base object properties that all entities have
        const baseEntity = {
            id: i,
            version: 0,
            createdBy: `user${Math.floor(Math.random() * 5) + 1}`,
            createdDate: createdDate.toISOString(),
            lastModifiedBy: `user${Math.floor(Math.random() * 5) + 1}`,
            lastModifiedDate: new Date().toISOString(),
            status: statuses[Math.floor(Math.random() * statuses.length)],
        };

        // Add entity-specific properties based on entity type
        switch (entityType) {
            case ObjectType.Event:
                data.push({
                    ...baseEntity,
                    name: `Event ${i}`,
                    description: `Description for Event ${i}`,
                    startDate: new Date(now.getTime() + 86400000 * i).toISOString(),
                    endDate: new Date(now.getTime() + 86400000 * (i + 7)).toISOString(),
                    location: `Location ${i}`,
                    maxParticipants: 100 + i * 10,
                    currentParticipants: Math.floor(Math.random() * (100 + i * 10))
                });
                break;

            case ObjectType.Participant:
                data.push({
                    ...baseEntity,
                    name: `Participant ${i}`,
                    email: `participant${i}@example.com`,
                    phone: `+1${Math.floor(1000000000 + Math.random() * 9000000000)}`,
                    joinDate: createdDate.toISOString(),
                    points: Math.floor(Math.random() * 1000),
                    eventId: Math.floor(Math.random() * 5) + 1
                });
                break;

            case ObjectType.Reward:
                data.push({
                    ...baseEntity,
                    name: `Reward ${i}`,
                    description: `Description for Reward ${i}`,
                    points: Math.floor(Math.random() * 500) + 50,
                    quantity: Math.floor(Math.random() * 100),
                    eventId: Math.floor(Math.random() * 5) + 1,
                    redeemCount: Math.floor(Math.random() * 50)
                });
                break;

            case ObjectType.Region:
                data.push({
                    ...baseEntity,
                    name: `Region ${i}`,
                    code: `R${i}`,
                    description: `Description for Region ${i}`
                });
                break;

            case ObjectType.Province:
                data.push({
                    ...baseEntity,
                    name: `Province ${i}`,
                    code: `P${i}`,
                    regionId: Math.floor(Math.random() * 5) + 1
                });
                break;

            case ObjectType.GoldenHour:
                data.push({
                    ...baseEntity,
                    name: `Golden Hour ${i}`,
                    startTime: `${Math.floor(Math.random() * 24)}:00`,
                    endTime: `${Math.floor(Math.random() * 24)}:00`,
                    multiplier: Math.random() * 3 + 1,
                    eventId: Math.floor(Math.random() * 5) + 1
                });
                break;

            case ObjectType.User:
                data.push({
                    ...baseEntity,
                    username: `user${i}`,
                    email: `user${i}@example.com`,
                    fullName: `User ${i}`,
                    roleId: Math.floor(Math.random() * 3) + 1,
                    lastLogin: new Date(now.getTime() - Math.floor(Math.random() * 86400000 * 7)).toISOString()
                });
                break;

            case ObjectType.Role:
                data.push({
                    ...baseEntity,
                    name: `Role ${i}`,
                    description: `Description for Role ${i}`,
                    permissions: ['READ', 'WRITE', 'DELETE'].slice(0, Math.floor(Math.random() * 3) + 1).join(',')
                });
                break;

            case ObjectType.AuditLog:
                data.push({
                    ...baseEntity,
                    action: ['CREATE', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT'][Math.floor(Math.random() * 5)],
                    entityType: Object.values(ObjectType)[Math.floor(Math.random() * Object.values(ObjectType).length)],
                    entityId: Math.floor(Math.random() * 100) + 1,
                    userId: Math.floor(Math.random() * 5) + 1,
                    details: `User performed action on ${new Date(now.getTime() - Math.floor(Math.random() * 86400000 * 30)).toISOString()}`
                });
                break;

            case ObjectType.SpinHistory:
                data.push({
                    ...baseEntity,
                    participantId: Math.floor(Math.random() * 100) + 1,
                    eventId: Math.floor(Math.random() * 5) + 1,
                    rewardId: Math.floor(Math.random() * 20) + 1,
                    spinDate: new Date(now.getTime() - Math.floor(Math.random() * 86400000 * 30)).toISOString(),
                    pointsUsed: Math.floor(Math.random() * 100) + 10,
                    result: Math.random() > 0.3 ? 'WIN' : 'LOSE'
                });
                break;

            default:
                data.push({
                    ...baseEntity,
                    name: `Item ${i}`,
                    description: `Generic item ${i} description`
                });
        }
    }

    return data;
}

/**
 * Get field type mapping for an entity
 */
function getFieldTypeMap(entityType: ObjectType): Record<string, { fieldName: string; fieldType: string; sortType: SortType }> {
    const commonFields = {
        id: { fieldName: 'ID', fieldType: 'NUMBER', sortType: SortType.NONE },
        version: { fieldName: 'Version', fieldType: 'NUMBER', sortType: SortType.NONE },
        createdBy: { fieldName: 'Created By', fieldType: 'STRING', sortType: SortType.NONE },
        createdDate: { fieldName: 'Created Date', fieldType: 'DATETIME', sortType: SortType.NONE },
        lastModifiedBy: { fieldName: 'Last Modified By', fieldType: 'STRING', sortType: SortType.NONE },
        lastModifiedDate: { fieldName: 'Last Modified Date', fieldType: 'DATETIME', sortType: SortType.NONE },
        status: { fieldName: 'Status', fieldType: 'STRING', sortType: SortType.NONE },
    };

    const entityFields: Record<ObjectType, Record<string, { fieldName: string; fieldType: string; sortType: SortType }>> = {
        [ObjectType.Event]: {
            ...commonFields,
            name: { fieldName: 'Name', fieldType: 'STRING', sortType: SortType.NONE },
            description: { fieldName: 'Description', fieldType: 'STRING', sortType: SortType.NONE },
            startDate: { fieldName: 'Start Date', fieldType: 'DATETIME', sortType: SortType.NONE },
            endDate: { fieldName: 'End Date', fieldType: 'DATETIME', sortType: SortType.NONE },
            location: { fieldName: 'Location', fieldType: 'STRING', sortType: SortType.NONE },
            maxParticipants: { fieldName: 'Max Participants', fieldType: 'NUMBER', sortType: SortType.NONE },
            currentParticipants: { fieldName: 'Current Participants', fieldType: 'NUMBER', sortType: SortType.NONE },
        },
        [ObjectType.Participant]: {
            ...commonFields,
            name: { fieldName: 'Name', fieldType: 'STRING', sortType: SortType.NONE },
            email: { fieldName: 'Email', fieldType: 'STRING', sortType: SortType.NONE },
            phone: { fieldName: 'Phone', fieldType: 'STRING', sortType: SortType.NONE },
            joinDate: { fieldName: 'Join Date', fieldType: 'DATETIME', sortType: SortType.NONE },
            points: { fieldName: 'Points', fieldType: 'NUMBER', sortType: SortType.NONE },
            eventId: { fieldName: 'Event ID', fieldType: 'NUMBER', sortType: SortType.NONE },
        },
        [ObjectType.Reward]: {
            ...commonFields,
            name: { fieldName: 'Name', fieldType: 'STRING', sortType: SortType.NONE },
            description: { fieldName: 'Description', fieldType: 'STRING', sortType: SortType.NONE },
            points: { fieldName: 'Points', fieldType: 'NUMBER', sortType: SortType.NONE },
            quantity: { fieldName: 'Quantity', fieldType: 'NUMBER', sortType: SortType.NONE },
            eventId: { fieldName: 'Event ID', fieldType: 'NUMBER', sortType: SortType.NONE },
            redeemCount: { fieldName: 'Redeem Count', fieldType: 'NUMBER', sortType: SortType.NONE },
        },
        [ObjectType.Region]: {
            ...commonFields,
            name: { fieldName: 'Name', fieldType: 'STRING', sortType: SortType.NONE },
            code: { fieldName: 'Code', fieldType: 'STRING', sortType: SortType.NONE },
            description: { fieldName: 'Description', fieldType: 'STRING', sortType: SortType.NONE },
        },
        [ObjectType.Province]: {
            ...commonFields,
            name: { fieldName: 'Name', fieldType: 'STRING', sortType: SortType.NONE },
            code: { fieldName: 'Code', fieldType: 'STRING', sortType: SortType.NONE },
            regionId: { fieldName: 'Region ID', fieldType: 'NUMBER', sortType: SortType.NONE },
        },
        [ObjectType.GoldenHour]: {
            ...commonFields,
            name: { fieldName: 'Name', fieldType: 'STRING', sortType: SortType.NONE },
            startTime: { fieldName: 'Start Time', fieldType: 'STRING', sortType: SortType.NONE },
            endTime: { fieldName: 'End Time', fieldType: 'STRING', sortType: SortType.NONE },
            multiplier: { fieldName: 'Multiplier', fieldType: 'NUMBER', sortType: SortType.NONE },
            eventId: { fieldName: 'Event ID', fieldType: 'NUMBER', sortType: SortType.NONE },
        },
        [ObjectType.User]: {
            ...commonFields,
            username: { fieldName: 'Username', fieldType: 'STRING', sortType: SortType.NONE },
            email: { fieldName: 'Email', fieldType: 'STRING', sortType: SortType.NONE },
            fullName: { fieldName: 'Full Name', fieldType: 'STRING', sortType: SortType.NONE },
            roleId: { fieldName: 'Role ID', fieldType: 'NUMBER', sortType: SortType.NONE },
            lastLogin: { fieldName: 'Last Login', fieldType: 'DATETIME', sortType: SortType.NONE },
        },
        [ObjectType.Role]: {
            ...commonFields,
            name: { fieldName: 'Name', fieldType: 'STRING', sortType: SortType.NONE },
            description: { fieldName: 'Description', fieldType: 'STRING', sortType: SortType.NONE },
            permissions: { fieldName: 'Permissions', fieldType: 'STRING', sortType: SortType.NONE },
        },
        [ObjectType.AuditLog]: {
            ...commonFields,
            action: { fieldName: 'Action', fieldType: 'STRING', sortType: SortType.NONE },
            entityType: { fieldName: 'Entity Type', fieldType: 'STRING', sortType: SortType.NONE },
            entityId: { fieldName: 'Entity ID', fieldType: 'NUMBER', sortType: SortType.NONE },
            userId: { fieldName: 'User ID', fieldType: 'NUMBER', sortType: SortType.NONE },
            details: { fieldName: 'Details', fieldType: 'STRING', sortType: SortType.NONE },
        },
        [ObjectType.SpinHistory]: {
            ...commonFields,
            participantId: { fieldName: 'Participant ID', fieldType: 'NUMBER', sortType: SortType.NONE },
            eventId: { fieldName: 'Event ID', fieldType: 'NUMBER', sortType: SortType.NONE },
            rewardId: { fieldName: 'Reward ID', fieldType: 'NUMBER', sortType: SortType.NONE },
            spinDate: { fieldName: 'Spin Date', fieldType: 'DATETIME', sortType: SortType.NONE },
            pointsUsed: { fieldName: 'Points Used', fieldType: 'NUMBER', sortType: SortType.NONE },
            result: { fieldName: 'Result', fieldType: 'STRING', sortType: SortType.NONE },
        },
        [ObjectType.Statistics]: {
            ...commonFields,
            name: { fieldName: 'Name', fieldType: 'STRING', sortType: SortType.NONE },
            category: { fieldName: 'Category', fieldType: 'STRING', sortType: SortType.NONE },
            value: { fieldName: 'Value', fieldType: 'NUMBER', sortType: SortType.NONE },
            period: { fieldName: 'Period', fieldType: 'STRING', sortType: SortType.NONE },
            date: { fieldName: 'Date', fieldType: 'DATETIME', sortType: SortType.NONE },
        },
        [ObjectType.Permission]: {
            ...commonFields,
            name: { fieldName: 'Name', fieldType: 'STRING', sortType: SortType.NONE },
            code: { fieldName: 'Code', fieldType: 'STRING', sortType: SortType.NONE },
            description: { fieldName: 'Description', fieldType: 'STRING', sortType: SortType.NONE },
            roleId: { fieldName: 'Role ID', fieldType: 'NUMBER', sortType: SortType.NONE },
        },
        [ObjectType.Configuration]: {
            ...commonFields,
            key: { fieldName: 'Key', fieldType: 'STRING', sortType: SortType.NONE },
            value: { fieldName: 'Value', fieldType: 'STRING', sortType: SortType.NONE },
            description: { fieldName: 'Description', fieldType: 'STRING', sortType: SortType.NONE },
            type: { fieldName: 'Type', fieldType: 'STRING', sortType: SortType.NONE },
        },
        [ObjectType.BlacklistedToken]: {
            ...commonFields,
            token: { fieldName: 'Token', fieldType: 'STRING', sortType: SortType.NONE },
            expiryDate: { fieldName: 'Expiry Date', fieldType: 'DATETIME', sortType: SortType.NONE },
            reason: { fieldName: 'Reason', fieldType: 'STRING', sortType: SortType.NONE },
            userId: { fieldName: 'User ID', fieldType: 'NUMBER', sortType: SortType.NONE },
        },
        [ObjectType.EventLocation]: {
            ...commonFields,
            name: { fieldName: 'Name', fieldType: 'STRING', sortType: SortType.NONE },
            address: { fieldName: 'Address', fieldType: 'STRING', sortType: SortType.NONE },
            city: { fieldName: 'City', fieldType: 'STRING', sortType: SortType.NONE },
            state: { fieldName: 'State', fieldType: 'STRING', sortType: SortType.NONE },
            country: { fieldName: 'Country', fieldType: 'STRING', sortType: SortType.NONE },
            zipCode: { fieldName: 'Zip Code', fieldType: 'STRING', sortType: SortType.NONE },
            capacity: { fieldName: 'Capacity', fieldType: 'NUMBER', sortType: SortType.NONE },
            eventId: { fieldName: 'Event ID', fieldType: 'NUMBER', sortType: SortType.NONE },
        },
        [ObjectType.ParticipantEvent]: {
            ...commonFields,
            participantId: { fieldName: 'Participant ID', fieldType: 'NUMBER', sortType: SortType.NONE },
            eventId: { fieldName: 'Event ID', fieldType: 'NUMBER', sortType: SortType.NONE },
            joinDate: { fieldName: 'Join Date', fieldType: 'DATETIME', sortType: SortType.NONE },
            checkInDate: { fieldName: 'Check-in Date', fieldType: 'DATETIME', sortType: SortType.NONE },
            attendance: { fieldName: 'Attendance', fieldType: 'BOOLEAN', sortType: SortType.NONE },
            notes: { fieldName: 'Notes', fieldType: 'STRING', sortType: SortType.NONE },
        }
    };

    return entityFields[entityType] || commonFields;
}

/**
 * Apply sorting to data
 */
function applySorting(data: any[], sorts: { field: string; sortType: SortType }[]): any[] {
    if (!sorts || sorts.length === 0) return data;

    return [...data].sort((a, b) => {
        for (const sort of sorts) {
            const { field, sortType } = sort;

            if (a[field] === undefined || b[field] === undefined) continue;

            if (a[field] < b[field]) {
                return sortType === SortType.ASCENDING ? -1 : 1;
            }
            if (a[field] > b[field]) {
                return sortType === SortType.ASCENDING ? 1 : -1;
            }
        }
        return 0;
    });
}

/**
 * Apply filtering to data
 */
function applyFiltering(data: any[], filters: { field: string; filterType: FilterType; minValue: string; maxValue: string }[]): any[] {
    if (!filters || filters.length === 0) return data;

    return data.filter(item => {
        return filters.every(filter => {
            const { field, filterType, minValue, maxValue } = filter;
            const value = item[field];

            if (value === undefined) return true;

            switch (filterType) {
                case FilterType.EQUALS:
                    if (typeof value === 'number') {
                        return value === Number(minValue);
                    }
                    return value === minValue;

                case FilterType.CONTAINS:
                    return typeof value === 'string' && value.toLowerCase().includes(minValue.toLowerCase());

                case FilterType.STARTS_WITH:
                    return typeof value === 'string' && value.toLowerCase().startsWith(minValue.toLowerCase());

                case FilterType.ENDS_WITH:
                    return typeof value === 'string' && value.toLowerCase().endsWith(minValue.toLowerCase());

                case FilterType.BETWEEN:
                    if (typeof value === 'number') {
                        const min = minValue !== '' ? Number(minValue) : null;
                        const max = maxValue !== '' ? Number(maxValue) : null;
                        return (min === null || value >= min) && (max === null || value <= max);
                    }
                    if (value instanceof Date || typeof value === 'string' && !isNaN(Date.parse(value))) {
                        const dateValue = new Date(value).getTime();
                        const min = minValue !== '' ? new Date(minValue).getTime() : null;
                        const max = maxValue !== '' ? new Date(maxValue).getTime() : null;
                        return (min === null || dateValue >= min) && (max === null || dateValue <= max);
                    }
                    return true;

                default:
                    return true;
            }
        });
    });
}

/**
 * Apply search to data
 */
function applySearch(data: any[], search: Record<ObjectType, DataObject>, objectType: ObjectType): any[] {
    if (!search || Object.keys(search).length === 0) return data;

    const entitySearch = search[objectType];
    if (!entitySearch?.data?.data?._search) return data;

    const searchTerm = entitySearch.data.data._search.toLowerCase();

    return data.filter(item => {
        return Object.values(item).some(value => {
            if (value === null || value === undefined) return false;
            return String(value).toLowerCase().includes(searchTerm);
        });
    });
}

/**
 * Apply pagination to data
 */
function applyPagination(data: any[], page: number, size: number): { items: any[]; totalItems: number; totalPages: number } {
    const totalItems = data.length;
    const totalPages = Math.ceil(totalItems / size);

    const start = page * size;
    const end = start + size;
    const items = data.slice(start, end);

    return {
        items,
        totalItems,
        totalPages
    };
}

/**
 * Mock function to fetch table data
 */
export function mockFetchTableData(request: TableFetchRequest): TableFetchResponse {
    const { objectType, page, size, sorts, filters, search } = request;
    const entityType = objectType;

    try {
        // Generate mock data
        const allData = generateMockData(entityType, 100);

        // Apply search
        const searchedData = applySearch(allData, search, entityType);

        // Apply filtering
        const filteredData = applyFiltering(searchedData, filters);

        // Apply sorting
        const sortedData = applySorting(filteredData, sorts);

        // Apply pagination
        const { items, totalItems, totalPages } = applyPagination(sortedData, page, size);

        // Format rows for response
        const rows = items.map(item => ({
            data: item,
            tableInfo: {
                key: { keys: [] }, // Add missing key property
                status: FetchStatus.SUCCESS,
                message: '',
                totalPage: totalPages,
                currentPage: page,
                pageSize: size,
                totalElements: totalItems,
                tableName: entityType,
                fieldNameMap: getFieldTypeMap(entityType),
                rows: [],
                relatedLinkedObjects: {} as Record<ObjectType, DataObject>
            }
        }));

        // Create response
        const response: TableFetchResponse = {
            key: { keys: [] }, // Add missing key property here too
            status: FetchStatus.SUCCESS,
            message: '',
            totalPage: totalPages,
            currentPage: page,
            pageSize: size,
            totalElements: totalItems,
            tableName: entityType,
            rows: rows,
            fieldNameMap: getFieldTypeMap(entityType),
            originalRequest: request,
            statistics: {
                charts: {
                    statusDistribution: [{
                        chartName: 'Status Distribution',
                        chartType: 'pie',
                        chartData: {
                            'ACTIVE': [Math.floor(Math.random() * 30) + 20 + ''],
                            'INACTIVE': [Math.floor(Math.random() * 20) + 10 + ''],
                            'PENDING': [Math.floor(Math.random() * 15) + 5 + ''],
                            'COMPLETED': [Math.floor(Math.random() * 10) + 2 + '']
                        }
                    }]
                }
            },
            relatedLinkedObjects: {} as Record<ObjectType, DataObject>,
            first: page === 0,
            last: page === totalPages - 1,
            empty: rows.length === 0,
            numberOfElements: rows.length
        };

        return response;

    } catch (error) {
        console.error('Error generating mock data:', error);
        return {
            key: { keys: [] }, // Add missing key property
            status: FetchStatus.ERROR,
            message: `Error generating mock data: ${(error as Error).message}`,
            totalPage: 0,
            currentPage: 0,
            pageSize: 0,
            totalElements: 0,
            tableName: entityType,
            rows: [],
            fieldNameMap: {},
            originalRequest: request,
            statistics: { charts: {} },
            relatedLinkedObjects: {} as Record<ObjectType, DataObject>,
            first: true,
            last: true,
            empty: true,
            numberOfElements: 0
        };
    }
}

/**
 * Mock function to create an entity
 */
export function mockCreateEntity(entityType: ObjectType, data: any): any {
    const id = Math.floor(Math.random() * 10000) + 1000;
    return {
        ...data,
        id,
        createdDate: new Date().toISOString(),
        lastModifiedDate: new Date().toISOString(),
        createdBy: 'mock-user',
        lastModifiedBy: 'mock-user',
        version: 0
    };
}

/**
 * Mock function to update an entity
 */
export function mockUpdateEntity(entityType: ObjectType, id: string | number, data: any): any {
    return {
        ...data,
        id,
        lastModifiedDate: new Date().toISOString(),
        lastModifiedBy: 'mock-user',
        version: (data.version || 0) + 1
    };
}

/**
 * Mock function to delete an entity
 */
export function mockDeleteEntity(entityType: ObjectType, id: string | number): void {
    // In a real implementation, this would remove the entity from a mock store
    console.log(`Deleted entity ${entityType} with ID ${id}`);
}

/**
 * Mock function to get entity by ID
 */
export function mockGetEntityById(entityType: ObjectType, id: string | number): any {
    const mockData = generateMockData(entityType, 1)[0];
    return {
        ...mockData,
        id: Number(id)
    };
}
