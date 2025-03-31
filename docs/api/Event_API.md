# Event API Documentation

## Get All Events

Retrieves all events.

**Endpoint:** `GET /api/events`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
[
  {
    "id": 1,
    "name": "Annual Sales Conference",
    "description": "Yearly sales team event with lucky draw prizes",
    "startDate": "2023-11-15T09:00:00",
    "endDate": "2023-11-15T17:00:00",
    "status": "ACTIVE"
  }
]
```

**Error Responses:**

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/events"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view events",
    "path": "/api/events"
  }
  ```

## Get Event by ID

Retrieves an event by its ID.

**Endpoint:** `GET /api/events/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
{
  "id": 1,
  "name": "Annual Sales Conference",
  "description": "Yearly sales team event with lucky draw prizes",
  "startDate": "2023-11-15T09:00:00",
  "endDate": "2023-11-15T17:00:00",
  "status": "ACTIVE"
}
```

**Error Responses:**

- **Not Found:** (404 Not Found)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 404,
    "error": "Not Found",
    "message": "Event with ID 999 not found",
    "path": "/api/events/999"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/events/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view this event",
    "path": "/api/events/1"
  }
  ```

## Create Event

Creates a new event.

**Endpoint:** `POST /api/events`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "End of Year Party",
  "description": "Celebration with lucky draws for employees",
  "startDate": "2023-12-22T18:00:00",
  "endDate": "2023-12-22T23:00:00",
  "status": "PLANNED"
}
```

**Success Response:** (201 Created)
```json
{
  "id": 3,
  "name": "End of Year Party",
  "description": "Celebration with lucky draws for employees",
  "startDate": "2023-12-22T18:00:00",
  "endDate": "2023-12-22T23:00:00",
  "status": "PLANNED"
}
```

**Error Responses:**

- **Validation Error:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:24:33.444Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed",
    "errors": [
      {
        "field": "name",
        "message": "must not be blank"
      },
      {
        "field": "startDate",
        "message": "must not be null"
      }
    ],
    "path": "/api/events"
  }
  ```

- **Invalid Date Range:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:26:00.444Z",
    "status": 400,
    "error": "Bad Request",
    "message": "End date must be after start date",
    "path": "/api/events"
  }
  ```

- **Duplicate Event:** (409 Conflict)
  ```json
  {
    "timestamp": "2023-11-05T15:33:33.444Z", 
    "status": 409,
    "error": "Conflict",
    "message": "Event with the same name and date already exists",
    "path": "/api/events"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/events"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to create events",
    "path": "/api/events"
  }
  ```

## Update Event

Updates an existing event.

**Endpoint:** `PUT /api/events/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Annual Sales Conference 2023",
  "description": "Updated description for yearly sales team event",
  "startDate": "2023-11-15T10:00:00",
  "endDate": "2023-11-15T18:00:00",
  "status": "ACTIVE"
}
```

**Success Response:** (200 OK)
```json
{
  "id": 1,
  "name": "Annual Sales Conference 2023",
  "description": "Updated description for yearly sales team event",
  "startDate": "2023-11-15T10:00:00",
  "endDate": "2023-11-15T18:00:00",
  "status": "ACTIVE"
}
```

**Error Responses:**

- **Not Found:** (404 Not Found)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 404,
    "error": "Not Found",
    "message": "Event with ID 999 not found",
    "path": "/api/events/999"
  }
  ```

- **Validation Error:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:24:33.444Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed",
    "errors": [
      {
        "field": "name",
        "message": "must not be blank"
      },
      {
        "field": "status",
        "message": "must be one of: PLANNED, ACTIVE, COMPLETED, CANCELLED"
      }
    ],
    "path": "/api/events/1"
  }
  ```

- **Invalid Status Transition:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:34:33.444Z", 
    "status": 400,
    "error": "Bad Request",
    "message": "Cannot change event status from COMPLETED to PLANNED",
    "path": "/api/events/1"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/events/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to update this event",
    "path": "/api/events/1"
  }
  ```

## Delete Event

Deletes an event by its ID.

**Endpoint:** `DELETE /api/events/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (204 No Content)
Empty response body with status 204.

**Error Responses:**

- **Not Found:** (404 Not Found)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 404,
    "error": "Not Found",
    "message": "Event with ID 999 not found",
    "path": "/api/events/999"
  }
  ```

- **Cannot Delete Active Event:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:34:33.444Z", 
    "status": 400,
    "error": "Bad Request",
    "message": "Cannot delete an active event with participants",
    "path": "/api/events/1"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/events/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to delete events",
    "path": "/api/events/1"
  }
  ```

- **Internal Server Error:** (500 Internal Server Error)
  ```json
  {
    "timestamp": "2023-11-05T15:28:33.444Z",
    "status": 500,
    "error": "Internal Server Error",
    "message": "An unexpected error occurred",
    "path": "/api/events/1"
  }
  ```

## Get Active Events

Retrieves all active events.

**Endpoint:** `GET /api/events/active`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
[
  {
    "id": 1,
    "name": "Annual Sales Conference 2023",
    "description": "Updated description for yearly sales team event",
    "startDate": "2023-11-15T10:00:00",
    "endDate": "2023-11-15T18:00:00",
    "status": "ACTIVE"
  }
]
```

**Error Responses:**

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/events/active"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view events",
    "path": "/api/events/active"
  }
  ```
