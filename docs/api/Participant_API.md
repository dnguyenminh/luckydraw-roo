# Participant API Documentation

## Get All Participants

Retrieves all participants.

**Endpoint:** `GET /api/participants`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "1234567890",
    "department": "Sales",
    "checkedIn": false,
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
    "path": "/api/participants"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view participants",
    "path": "/api/participants"
  }
  ```

## Get Participant by ID

Retrieves a participant by their ID.

**Endpoint:** `GET /api/participants/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "1234567890",
  "department": "Sales",
  "checkedIn": false,
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
    "message": "Participant with ID 999 not found",
    "path": "/api/participants/999"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/participants/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view this participant",
    "path": "/api/participants/1"
  }
  ```

## Create Participant

Creates a new participant.

**Endpoint:** `POST /api/participants`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Robert Johnson",
  "email": "robert.johnson@example.com",
  "phone": "5556667777",
  "department": "IT",
  "checkedIn": false,
  "eventId": 1
}
```

**Success Response:** (201 Created)
```json
{
  "id": 3,
  "name": "Robert Johnson",
  "email": "robert.johnson@example.com",
  "phone": "5556667777",
  "department": "IT",
  "checkedIn": false,
  "eventId": 1
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
        "field": "email",
        "message": "must be a well-formed email address"
      }
    ],
    "path": "/api/participants"
  }
  ```

- **Event Not Found:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Event with ID 999 not found",
    "path": "/api/participants"
  }
  ```

- **Duplicate Email:** (409 Conflict)
  ```json
  {
    "timestamp": "2023-11-05T15:33:33.444Z", 
    "status": 409,
    "error": "Conflict",
    "message": "Participant with this email already exists for this event",
    "path": "/api/participants"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/participants"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to create participants",
    "path": "/api/participants"
  }
  ```

## Update Participant

Updates an existing participant.

**Endpoint:** `PUT /api/participants/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john.doe@company.com",
  "phone": "1234567890",
  "department": "Sales Director",
  "checkedIn": true,
  "eventId": 1
}
```

**Success Response:** (200 OK)
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@company.com",
  "phone": "1234567890",
  "department": "Sales Director",
  "checkedIn": true,
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
    "message": "Participant with ID 999 not found",
    "path": "/api/participants/999"
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
        "field": "email",
        "message": "must be a well-formed email address"
      }
    ],
    "path": "/api/participants/1"
  }
  ```

- **Event Not Found:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Event with ID 999 not found",
    "path": "/api/participants/1"
  }
  ```

- **Duplicate Email:** (409 Conflict)
  ```json
  {
    "timestamp": "2023-11-05T15:33:33.444Z", 
    "status": 409,
    "error": "Conflict",
    "message": "Another participant with this email already exists for this event",
    "path": "/api/participants/1"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/participants/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to update this participant",
    "path": "/api/participants/1"
  }
  ```

## Delete Participant

Deletes a participant by their ID.

**Endpoint:** `DELETE /api/participants/{id}`

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
    "message": "Participant with ID 999 not found",
    "path": "/api/participants/999"
  }
  ```

- **Cannot Delete Checked-in Participant:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:34:33.444Z", 
    "status": 400,
    "error": "Bad Request",
    "message": "Cannot delete a participant who has already checked in",
    "path": "/api/participants/1"
  }
  ```

- **Participant Has Rewards:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:34:53.444Z", 
    "status": 400,
    "error": "Bad Request",
    "message": "Cannot delete a participant who has been assigned rewards",
    "path": "/api/participants/1"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/participants/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to delete participants",
    "path": "/api/participants/1"
  }
  ```

- **Internal Server Error:** (500 Internal Server Error)
  ```json
  {
    "timestamp": "2023-11-05T15:28:33.444Z",
    "status": 500,
    "error": "Internal Server Error",
    "message": "An unexpected error occurred",
    "path": "/api/participants/1"
  }
  ```

## Get Participants by Event ID

Retrieves all participants for a specific event.

**Endpoint:** `GET /api/participants/event/{eventId}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@company.com",
    "phone": "1234567890",
    "department": "Sales Director",
    "checkedIn": true,
    "eventId": 1
  },
  {
    "id": 2,
    "name": "Jane Smith",
    "email": "jane.smith@example.com",
    "phone": "9876543210",
    "department": "Marketing",
    "checkedIn": true,
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
    "path": "/api/participants/event/999"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/participants/event/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view participants for this event",
    "path": "/api/participants/event/1"
  }
  ```

## Get Checked-In Participants

Retrieves all participants who have checked in for a specific event.

**Endpoint:** `GET /api/participants/check-in/{eventId}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@company.com",
    "phone": "1234567890",
    "department": "Sales Director",
    "checkedIn": true,
    "eventId": 1
  },
  {
    "id": 2,
    "name": "Jane Smith",
    "email": "jane.smith@example.com",
    "phone": "9876543210",
    "department": "Marketing",
    "checkedIn": true,
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
    "path": "/api/participants/check-in/999"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/participants/check-in/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view checked-in participants",
    "path": "/api/participants/check-in/1"
  }
  ```

## Check In Participant

Marks a participant as checked in.

**Endpoint:** `POST /api/participants/{id}/check-in`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
{
  "id": 3,
  "name": "Robert Johnson",
  "email": "robert.johnson@example.com",
  "phone": "5556667777",
  "department": "IT",
  "checkedIn": true,
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
    "message": "Participant with ID 999 not found",
    "path": "/api/participants/999/check-in"
  }
  ```

- **Already Checked In:** (409 Conflict)
  ```json
  {
    "timestamp": "2023-11-05T15:35:33.444Z", 
    "status": 409,
    "error": "Conflict",
    "message": "Participant is already checked in",
    "path": "/api/participants/1/check-in"
  }
  ```

- **Event Inactive:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:34:33.444Z", 
    "status": 400,
    "error": "Bad Request",
    "message": "Cannot check in participant for an inactive event",
    "path": "/api/participants/3/check-in"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/participants/3/check-in"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to check in participants",
    "path": "/api/participants/3/check-in"
  }
  ```
