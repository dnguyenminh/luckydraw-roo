# Event Location API Documentation

## Get All Locations

Retrieves all event locations.

**Endpoint:** `GET /api/event-locations`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
[
  {
    "id": 1,
    "name": "Conference Room A",
    "address": "123 Corporate Drive, Floor 5",
    "capacity": 100,
    "eventId": 1
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
    "path": "/api/event-locations"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view event locations",
    "path": "/api/event-locations"
  }
  ```

## Get Location by ID

Retrieves an event location by its ID.

**Endpoint:** `GET /api/event-locations/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
{
  "id": 1,
  "name": "Conference Room A",
  "address": "123 Corporate Drive, Floor 5",
  "capacity": 100,
  "eventId": 1
}
```

**Error Responses:**

- **Not Found:** (404 Not Found)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 404,
    "error": "Not Found",
    "message": "Event location with ID 999 not found",
    "path": "/api/event-locations/999"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/event-locations/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view this event location",
    "path": "/api/event-locations/1"
  }
  ```

## Create Location

Creates a new event location.

**Endpoint:** `POST /api/event-locations`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Rooftop Garden",
  "address": "789 Corporate Park, 15th Floor",
  "capacity": 150,
  "eventId": 3
}
```

**Success Response:** (201 Created)
```json
{
  "id": 3,
  "name": "Rooftop Garden",
  "address": "789 Corporate Park, 15th Floor",
  "capacity": 150,
  "eventId": 3
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
        "field": "capacity",
        "message": "must be greater than 0"
      }
    ],
    "path": "/api/event-locations"
  }
  ```

- **Event Not Found:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Event with ID 999 not found",
    "path": "/api/event-locations"
  }
  ```

- **Duplicate Location:** (409 Conflict)
  ```json
  {
    "timestamp": "2023-11-05T15:33:33.444Z", 
    "status": 409,
    "error": "Conflict",
    "message": "Location with this name already exists for this event",
    "path": "/api/event-locations"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/event-locations"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to create event locations",
    "path": "/api/event-locations"
  }
  ```

## Update Location

Updates an existing event location.

**Endpoint:** `PUT /api/event-locations/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Conference Room A - East Wing",
  "address": "123 Corporate Drive, Floor 5, East Wing",
  "capacity": 120,
  "eventId": 1
}
```

**Success Response:** (200 OK)
```json
{
  "id": 1,
  "name": "Conference Room A - East Wing",
  "address": "123 Corporate Drive, Floor 5, East Wing",
  "capacity": 120,
  "eventId": 1
}
```

**Error Responses:**

- **Not Found:** (404 Not Found)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 404,
    "error": "Not Found",
    "message": "Event location with ID 999 not found",
    "path": "/api/event-locations/999"
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
        "field": "capacity",
        "message": "must be greater than 0"
      }
    ],
    "path": "/api/event-locations/1"
  }
  ```

- **Event Not Found:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Event with ID 999 not found",
    "path": "/api/event-locations/1"
  }
  ```

- **Duplicate Location:** (409 Conflict)
  ```json
  {
    "timestamp": "2023-11-05T15:33:33.444Z", 
    "status": 409,
    "error": "Conflict",
    "message": "Another location with this name already exists for this event",
    "path": "/api/event-locations/1"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/event-locations/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to update event locations",
    "path": "/api/event-locations/1"
  }
  ```

## Delete Location

Deletes an event location by its ID.

**Endpoint:** `DELETE /api/event-locations/{id}`

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
    "message": "Event location with ID 999 not found",
    "path": "/api/event-locations/999"
  }
  ```

- **In Use Error:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:34:33.444Z", 
    "status": 400,
    "error": "Bad Request",
    "message": "Cannot delete location that is currently in use",
    "path": "/api/event-locations/1"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/event-locations/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to delete event locations",
    "path": "/api/event-locations/1"
  }
  ```

- **Internal Server Error:** (500 Internal Server Error)
  ```json
  {
    "timestamp": "2023-11-05T15:28:33.444Z",
    "status": 500,
    "error": "Internal Server Error",
    "message": "An unexpected error occurred",
    "path": "/api/event-locations/1"
  }
  ```

## Get Locations by Event ID

Retrieves all locations for a specific event.

**Endpoint:** `GET /api/event-locations/event/{eventId}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
[
  {
    "id": 1,
    "name": "Conference Room A - East Wing",
    "address": "123 Corporate Drive, Floor 5, East Wing",
    "capacity": 120,
    "eventId": 1
  }
]
```

**Error Responses:**

- **Event Not Found:** (404 Not Found)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 404,
    "error": "Not Found",
    "message": "Event with ID 999 not found",
    "path": "/api/event-locations/event/999"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/event-locations/event/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view locations for this event",
    "path": "/api/event-locations/event/1"
  }
  ```
