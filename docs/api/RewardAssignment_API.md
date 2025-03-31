# Reward Assignment API Documentation

## Get All Reward Assignments

Retrieves all reward assignments.

**Endpoint:** `GET /api/reward-assignments`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
[
  {
    "id": 1,
    "rewardId": 2,
    "participantId": 1,
    "assignedAt": "2023-11-15T14:30:45",
    "status": "ASSIGNED"
  },
  {
    "id": 2,
    "rewardId": 3,
    "participantId": 2,
    "assignedAt": "2023-11-15T14:35:12",
    "status": "ASSIGNED"
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
    "path": "/api/reward-assignments"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view reward assignments",
    "path": "/api/reward-assignments"
  }
  ```

## Get Reward Assignment by ID

Retrieves a reward assignment by its ID.

**Endpoint:** `GET /api/reward-assignments/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
{
  "id": 1,
  "rewardId": 2,
  "participantId": 1,
  "assignedAt": "2023-11-15T14:30:45",
  "status": "ASSIGNED"
}
```

**Error Responses:**

- **Not Found:** (404 Not Found)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 404,
    "error": "Not Found",
    "message": "Reward assignment with ID 999 not found",
    "path": "/api/reward-assignments/999"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/reward-assignments/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view this reward assignment",
    "path": "/api/reward-assignments/1"
  }
  ```

## Create Reward Assignment

Assigns a reward to a participant.

**Endpoint:** `POST /api/reward-assignments`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request Body:**
```json
{
  "rewardId": 1,
  "participantId": 3,
  "status": "ASSIGNED"
}
```

**Success Response:** (201 Created)
```json
{
  "id": 3,
  "rewardId": 1,
  "participantId": 3,
  "assignedAt": "2023-11-15T15:10:22",
  "status": "ASSIGNED"
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
        "field": "rewardId",
        "message": "must not be null"
      },
      {
        "field": "participantId",
        "message": "must not be null"
      }
    ],
    "path": "/api/reward-assignments"
  }
  ```

- **Reward Not Found:** (404 Not Found)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 404,
    "error": "Not Found",
    "message": "Reward with ID 999 not found",
    "path": "/api/reward-assignments"
  }
  ```

- **Participant Not Found:** (404 Not Found)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 404,
    "error": "Not Found",
    "message": "Participant with ID 999 not found",
    "path": "/api/reward-assignments"
  }
  ```

- **No Available Rewards:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:36:33.444Z", 
    "status": 400,
    "error": "Bad Request",
    "message": "No more of this reward available for assignment",
    "path": "/api/reward-assignments"
  }
  ```

- **Participant Not Checked In:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:36:33.444Z", 
    "status": 400,
    "error": "Bad Request",
    "message": "Cannot assign reward to participant who hasn't checked in",
    "path": "/api/reward-assignments"
  }
  ```

- **Different Events:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:36:33.444Z", 
    "status": 400,
    "error": "Bad Request",
    "message": "Reward and participant belong to different events",
    "path": "/api/reward-assignments"
  }
  ```

- **Already Assigned:** (409 Conflict)
  ```json
  {
    "timestamp": "2023-11-05T15:35:33.444Z", 
    "status": 409,
    "error": "Conflict",
    "message": "This reward is already assigned to another participant",
    "path": "/api/reward-assignments"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/reward-assignments"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to create reward assignments",
    "path": "/api/reward-assignments"
  }
  ```

## Update Reward Assignment

Updates an existing reward assignment.

**Endpoint:** `PUT /api/reward-assignments/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request Body:**
```json
{
  "rewardId": 2,
  "participantId": 1,
  "status": "CLAIMED"
}
```

**Success Response:** (200 OK)
```json
{
  "id": 1,
  "rewardId": 2,
  "participantId": 1,
  "assignedAt": "2023-11-15T14:30:45",
  "claimedAt": "2023-11-15T16:45:10",
  "status": "CLAIMED"
}
```

**Error Responses:**

- **Not Found:** (404 Not Found)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 404,
    "error": "Not Found",
    "message": "Reward assignment with ID 999 not found",
    "path": "/api/reward-assignments/999"
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
        "field": "status",
        "message": "must be one of: ASSIGNED, CLAIMED, CANCELLED"
      }
    ],
    "path": "/api/reward-assignments/1"
  }
  ```

- **Invalid Status Transition:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:34:33.444Z", 
    "status": 400,
    "error": "Bad Request",
    "message": "Cannot change status from CLAIMED to ASSIGNED",
    "path": "/api/reward-assignments/1"
  }
  ```

- **Reward Not Found:** (404 Not Found)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 404,
    "error": "Not Found",
    "message": "Reward with ID 999 not found",
    "path": "/api/reward-assignments/1"
  }
  ```

- **Participant Not Found:** (404 Not Found)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 404,
    "error": "Not Found",
    "message": "Participant with ID 999 not found",
    "path": "/api/reward-assignments/1"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/reward-assignments/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to update reward assignments",
    "path": "/api/reward-assignments/1"
  }
  ```

## Delete Reward Assignment

Deletes a reward assignment.

**Endpoint:** `DELETE /api/reward-assignments/{id}`

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
    "message": "Reward assignment with ID 999 not found",
    "path": "/api/reward-assignments/999"
  }
  ```

- **Cannot Delete Claimed Reward:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:34:33.444Z", 
    "status": 400,
    "error": "Bad Request",
    "message": "Cannot delete a reward assignment that has been claimed",
    "path": "/api/reward-assignments/1"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/reward-assignments/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to delete reward assignments",
    "path": "/api/reward-assignments/1"
  }
  ```

## Get Reward Assignments by Event

Retrieves all reward assignments for a specific event.

**Endpoint:** `GET /api/reward-assignments/event/{eventId}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
[
  {
    "id": 1,
    "rewardId": 2,
    "participantId": 1,
    "assignedAt": "2023-11-15T14:30:45",
    "status": "ASSIGNED"
  },
  {
    "id": 2,
    "rewardId": 3,
    "participantId": 2,
    "assignedAt": "2023-11-15T14:35:12",
    "status": "ASSIGNED"
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
    "path": "/api/reward-assignments/event/999"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/reward-assignments/event/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view reward assignments for this event",
    "path": "/api/reward-assignments/event/1"
  }
  ```

## Get Reward Assignments by Participant

Retrieves all rewards assigned to a specific participant.

**Endpoint:** `GET /api/reward-assignments/participant/{participantId}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
[
  {
    "id": 1,
    "rewardId": 2,
    "participantId": 1,
    "assignedAt": "2023-11-15T14:30:45",
    "status": "ASSIGNED"
  }
]
```

**Error Responses:**

- **Participant Not Found:** (404 Not Found)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 404,
    "error": "Not Found",
    "message": "Participant with ID 999 not found",
    "path": "/api/reward-assignments/participant/999"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/reward-assignments/participant/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view reward assignments for this participant",
    "path": "/api/reward-assignments/participant/1"
  }
  ```

## Mark Reward as Claimed

Updates the status of a reward assignment to CLAIMED.

**Endpoint:** `PUT /api/reward-assignments/{id}/claim`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
{
  "id": 1,
  "rewardId": 2,
  "participantId": 1,
  "assignedAt": "2023-11-15T14:30:45",
  "claimedAt": "2023-11-15T16:45:10",
  "status": "CLAIMED"
}
```

**Error Responses:**

- **Not Found:** (404 Not Found)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 404,
    "error": "Not Found",
    "message": "Reward assignment with ID 999 not found",
    "path": "/api/reward-assignments/999/claim"
  }
  ```

- **Already Claimed:** (409 Conflict)
  ```json
  {
    "timestamp": "2023-11-05T15:35:33.444Z", 
    "status": 409,
    "error": "Conflict",
    "message": "This reward has already been claimed",
    "path": "/api/reward-assignments/1/claim"
  }
  ```

- **Invalid Status:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:34:33.444Z", 
    "status": 400,
    "error": "Bad Request",
    "message": "Cannot claim a reward that is in CANCELLED status",
    "path": "/api/reward-assignments/2/claim"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/reward-assignments/1/claim"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to claim rewards",
    "path": "/api/reward-assignments/1/claim"
  }
  ```
