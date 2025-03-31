# Authentication API Documentation

## Login

Authenticates a user and returns a JWT token.

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Success Response:** (200 OK)
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

**Error Responses:**

- **Invalid Credentials:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:22:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid username or password",
    "path": "/api/auth/login"
  }
  ```

- **Invalid Input:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:24:33.444Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed",
    "errors": [
      {
        "field": "username",
        "message": "must not be blank"
      }
    ],
    "path": "/api/auth/login"
  }
  ```

## Register

Registers a new user.

**Endpoint:** `POST /api/auth/register`

**Request Body:**
```json
{
  "username": "newuser",
  "email": "user@example.com",
  "password": "password123",
  "roles": ["USER"]
}
```

**Success Response:** (201 Created)
```
User registered successfully
```

**Error Responses:**

- **Username Already Exists:** (409 Conflict)
  ```json
  {
    "timestamp": "2023-11-05T15:32:33.444Z", 
    "status": 409,
    "error": "Conflict",
    "message": "Username is already taken!",
    "path": "/api/auth/register"
  }
  ```

- **Email Already Exists:** (409 Conflict)
  ```json
  {
    "timestamp": "2023-11-05T15:32:43.444Z", 
    "status": 409,
    "error": "Conflict",
    "message": "Email is already in use!",
    "path": "/api/auth/register"
  }
  ```

- **Invalid Input:** (400 Bad Request)
  ```json
  {
    "timestamp": "2023-11-05T15:24:33.444Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed",
    "errors": [
      {
        "field": "email",
        "message": "must be a well-formed email address"
      },
      {
        "field": "password",
        "message": "must be at least 6 characters"
      }
    ],
    "path": "/api/auth/register"
  }
  ```

## Validate Token

Validates if the current JWT token is valid and active.

**Endpoint:** `GET /api/auth/validate`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response:** (200 OK)
Empty response body with status 200 indicates a valid token.

**Error Responses:**

- **Invalid Token:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:33.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid JWT token",
    "path": "/api/auth/validate"
  }
  ```

- **Expired Token:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:23:55.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Expired JWT token",
    "path": "/api/auth/validate"
  }
  ```

- **Missing Token:** (401 Unauthorized)
  ```json
  {
    "timestamp": "2023-11-05T15:24:10.444Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Authorization header is required",
    "path": "/api/auth/validate"
  }
  ```
