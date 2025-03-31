# Reward API Documentation

## Get All Rewards

Retrieves all rewards.

**Endpoint:** `GET /api/rewards`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
[
  {
    "id": 1,
    "name": "Apple iPad",
    "description": "Latest model iPad",
    "value": 499.99,
    "quantity": 1,
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
    "path": "/api/rewards"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view rewards",
    "path": "/api/rewards"
  }
  ```

## Get Reward by ID

Retrieves a reward by its ID.

**Endpoint:** `GET /api/rewards/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
{
  "id": 1,
  "name": "Apple iPad",
  "description": "Latest model iPad",
  "value": 499.99,
  "quantity": 1,
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
    "message": "Reward with ID 999 not found",
    "path": "/api/rewards/999"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/rewards/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view this reward",
    "path": "/api/rewards/1"
  }
  ```

## Create Reward

Creates a new reward.

**Endpoint:** `POST /api/rewards`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Bluetooth Headphones",
  "description": "Premium wireless headphones",
  "value": 199.99,
  "quantity": 5,
  "eventId": 1
}
```

**Success Response:** (201 Created)
```json
{
  "id": 3,
  "name": "Bluetooth Headphones",
  "description": "Premium wireless headphones",
  "value": 199.99,
  "quantity": 5,
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
        "field": "value",
        "message": "must be greater than or equal to 0"
      },
      {
        "field": "quantity",
        "message": "must be greater than 0"
      }
    ],
    "path": "/api/rewards"
  }
  ```

- **Event Not Found:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Event with ID 999 not found",
    "path": "/api/rewards"
  }
  ```

- **Duplicate Reward:** (409 Conflict)
  ```json
  {
    "timestamp": "2023-11-05T15:33:33.444Z", 
    "status": 409,
    "error": "Conflict",
    "message": "Reward with this name already exists for this event",
    "path": "/api/rewards"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/rewards"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to create rewards",
    "path": "/api/rewards"
  }
  ```

## Update Reward

Updates an existing reward.

**Endpoint:** `PUT /api/rewards/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Apple iPad Pro",
  "description": "Latest model iPad Pro 11-inch",
  "value": 699.99,
  "quantity": 1,
  "eventId": 1
}
```

**Success Response:** (200 OK)
```json
{
  "id": 1,
  "name": "Apple iPad Pro",
  "description": "Latest model iPad Pro 11-inch",
  "value": 699.99,
  "quantity": 1,
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
    "message": "Reward with ID 999 not found",
    "path": "/api/rewards/999"
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
        "field": "quantity",
        "message": "must be greater than 0"
      }
    ],
    "path": "/api/rewards/1"
  }
  ```

- **Event Not Found:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Event with ID 999 not found",
    "path": "/api/rewards/1"
  }
  ```

- **Duplicate Reward:** (409 Conflict)
  ```json
  {
    "timestamp": "2023-11-05T15:33:33.444Z", 
    "status": 409,
    "error": "Conflict",
    "message": "Another reward with this name already exists for this event",
    "path": "/api/rewards/1"
  }
  ```

- **Insufficient Quantity:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:36:33.444Z", 
    "status": 400,
    "error": "Bad Request",
    "message": "Cannot reduce quantity below the number of rewards already assigned",
    "path": "/api/rewards/1"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/rewards/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to update rewards",
    "path": "/api/rewards/1"
  }
  ```

## Delete Reward

Deletes a reward by its ID.

**Endpoint:** `DELETE /api/rewards/{id}`

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
    "message": "Reward with ID 999 not found",
    "path": "/api/rewards/999"
  }
  ```

- **Already Assigned:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:34:33.444Z", 
    "status": 400,
    "error": "Bad Request",
    "message": "Cannot delete a reward that has been assigned to participants",
    "path": "/api/rewards/1"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/rewards/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to delete rewards",
    "path": "/api/rewards/1"
  }
  ```

- **Internal Server Error:** (500 Internal Server Error)
  ```json
  {
    "timestamp": "2023-11-05T15:28:33.444Z",
    "status": 500,
    "error": "Internal Server Error",
    "message": "An unexpected error occurred",
    "path": "/api/rewards/1"
  }
  ```

## Get Rewards by Event ID

Retrieves all rewards for a specific event.

**Endpoint:** `GET /api/rewards/event/{eventId}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
[
  {
    "id": 1,
    "name": "Apple iPad Pro",
    "description": "Latest model iPad Pro 11-inch",
    "value": 699.99,
    "quantity": 1,
    "eventId": 1
  },
  {
    "id": 2,
    "name": "Amazon Gift Card",
    "description": "$50 Amazon Gift Card",
    "value": 50.00,
    "quantity": 10,
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
    "path": "/api/rewards/event/999"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/rewards/event/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view rewards for this event",
    "path": "/api/rewards/event/1"
  }
  ```

## Assign Reward to Participant

Assigns a reward to a participant.

**Endpoint:** `POST /api/rewards/{rewardId}/assign/{participantId}`

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

- **Reward Not Found:** (404 Not Found)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 404,
    "error": "Not Found",
    "message": "Reward with ID 999 not found",
    "path": "/api/rewards/999/assign/1"
  }
  ```

- **Participant Not Found:** (404 Not Found)
  ```json
  {
    "timestamp": "2023-11-05T15:26:33.444Z",
    "status": 404,
    "error": "Not Found",
    "message": "Participant with ID 999 not found",
    "path": "/api/rewards/1/assign/999"
  }
  ```

- **No Available Rewards:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:36:33.444Z", 
    "status": 400,
    "error": "Bad Request",
    "message": "No more of this reward available for assignment",
    "path": "/api/rewards/1/assign/3"
  }
  ```

- **Participant Not Checked In:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:36:33.444Z", 
    "status": 400,
    "error": "Bad Request",
    "message": "Cannot assign reward to participant who hasn't checked in",
    "path": "/api/rewards/1/assign/3"
  }
  ```

- **Different Events:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:36:33.444Z", 
    "status": 400,
    "error": "Bad Request",
    "message": "Reward and participant belong to different events",
    "path": "/api/rewards/1/assign/3"
  }
  ```

- **Already Assigned:** (409 Conflict)
  ```json
  {
    "timestamp": "2023-11-05T15:35:33.444Z", 
    "status": 409,
    "error": "Conflict",
    "message": "This reward is already assigned to another participant",
    "path": "/api/rewards/1/assign/3"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/rewards/1/assign/3"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to assign rewards",
    "path": "/api/rewards/1/assign/3"
  }
  ```

## Get Available Rewards

Retrieves all rewards that have not yet been assigned for a specific event.

**Endpoint:** `GET /api/rewards/available/{eventId}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
```json
[
  {
    "id": 3,
    "name": "Bluetooth Headphones",
    "description": "Premium wireless headphones",
    "value": 199.99,
    "quantity": 5,
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
    "path": "/api/rewards/available/999"
  }
  ```

- **Unauthorized:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/rewards/available/1"
  }
  ```

- **Forbidden:** (403 Forbidden)
  ```json
  {
    "timestamp": "2023-11-05T15:30:33.444Z",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have permission to view available rewards",
    "path": "/api/rewards/available/1"
  }
  ```
