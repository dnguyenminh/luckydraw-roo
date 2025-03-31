# Common Error Responses

This document describes common error responses that may be returned by the Lucky Draw APIs. All API error responses follow a standard format.

## Error Response Format

```json
{
  "timestamp": "2023-11-05T15:22:33.444Z",
  "status": 400,
  "error": "Error Type",
  "message": "Human-readable error message",
  "path": "/api/resource-path"
}
```

For validation errors, additional field-specific errors are included:

```json
{
  "timestamp": "2023-11-05T15:24:33.444Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "fieldName",
      "message": "error description"
    }
  ],
  "path": "/api/resource-path"
}
```

## Authentication Errors

### Invalid Credentials

**Response:** (401 Unauthorized)
```json
{
  "timestamp": "2023-11-05T15:22:33.444Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid username or password",
  "path": "/api/auth/login"
}
```

### Invalid Token

**Response:** (401 Unauthorized)
```json
{
  "timestamp": "2023-11-05T15:23:33.444Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid JWT token",
  "path": "/api/events"
}
```

### Expired Token

**Response:** (401 Unauthorized)
```json
{
  "timestamp": "2023-11-05T15:23:55.444Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Expired JWT token",
  "path": "/api/events"
}
```

### Missing Token

**Response:** (401 Unauthorized)
```json
{
  "timestamp": "2023-11-05T15:24:10.444Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authorization header is required",
  "path": "/api/events"
}
```

## Validation Errors

### Missing Required Field

**Response:** (400 Bad Request)
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
    }
  ],
  "path": "/api/events"
}
```

### Invalid Email Format

**Response:** (400 Bad Request)
```json
{
  "timestamp": "2023-11-05T15:25:33.444Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "must be a well-formed email address"
    }
  ],
  "path": "/api/participants"
}
```

### Invalid Date Format

**Response:** (400 Bad Request)
```json
{
  "timestamp": "2023-11-05T15:26:00.444Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Could not parse date time: Invalid date format",
  "path": "/api/events"
}
```

## Resource Errors

### Resource Not Found

**Response:** (404 Not Found)
```json
{
  "timestamp": "2023-11-05T15:26:33.444Z",
  "status": 404,
  "error": "Not Found",
  "message": "Resource with ID 999 not found",
  "path": "/api/events/999"
}
```

### Resource Conflict

**Response:** (409 Conflict)
```json
{
  "timestamp": "2023-11-05T15:32:33.444Z", 
  "status": 409,
  "error": "Conflict",
  "message": "Username is already taken",
  "path": "/api/auth/register"
}
```

### Duplicate Entry

**Response:** (409 Conflict)
```json
{
  "timestamp": "2023-11-05T15:33:33.444Z", 
  "status": 409,
  "error": "Conflict",
  "message": "Event with the same name and date already exists",
  "path": "/api/events"
}
```

## Permission Errors

### Forbidden Access

**Response:** (403 Forbidden)
```json
{
  "timestamp": "2023-11-05T15:30:33.444Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. You don't have permission to perform this operation",
  "path": "/api/events/1"
}
```

### Insufficient Role

**Response:** (403 Forbidden)
```json
{
  "timestamp": "2023-11-05T15:31:33.444Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. Required role: ADMIN",
  "path": "/api/users"
}
```

## Business Logic Errors

### Invalid State Transition

**Response:** (400 Bad Request)
```json
{
  "timestamp": "2023-11-05T15:34:33.444Z", 
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot change event status from COMPLETED to PLANNED",
  "path": "/api/events/1"
}
```

### Resource Already Assigned

**Response:** (409 Conflict)
```json
{
  "timestamp": "2023-11-05T15:35:33.444Z", 
  "status": 409,
  "error": "Conflict",
  "message": "Reward has already been assigned to another participant",
  "path": "/api/rewards/1/assign/2"
}
```

### Resource Unavailable

**Response:** (400 Bad Request)
```json
{
  "timestamp": "2023-11-05T15:36:33.444Z", 
  "status": 400,
  "error": "Bad Request",
  "message": "No more rewards available for this event",
  "path": "/api/reward-assignments"
}
```

## Server Errors

### Internal Server Error

**Response:** (500 Internal Server Error)
```json
{
  "timestamp": "2023-11-05T15:28:33.444Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/rewards"
}
```

### Service Unavailable

**Response:** (503 Service Unavailable)
```json
{
  "timestamp": "2023-11-05T15:29:33.444Z",
  "status": 503,
  "error": "Service Unavailable",
  "message": "The service is temporarily unavailable. Please try again later.",
  "path": "/api/events"
}
```

## How To Handle Errors

When implementing a client application, consider these best practices for handling errors:

1. **Check status codes**: Always check the HTTP status code first to determine the category of error.
2. **Display meaningful messages**: Show human-readable error messages from the response to users.
3. **Field validation**: For 400 errors, check for field-specific validation errors to highlight form issues.
4. **Authentication handling**: For 401 errors, redirect users to login or refresh tokens.
5. **Retry logic**: For 5xx errors, implement reasonable retry logic with exponential backoff.
