# Lucky Draw API Documentation

This document outlines the RESTful APIs provided by the Lucky Draw application for integration with frontend applications.

## Table of Contents

1. [Overview](#overview)
2. [API Documentation by Domain](#api-documentation-by-domain)
3. [Security Notes](#security-notes)
4. [Frontend Integration Examples](#frontend-integration-examples)

## Overview

The Lucky Draw application provides a set of RESTful APIs for managing events, participants, rewards, and more. All API endpoints return JSON responses and follow standard HTTP status codes.

## API Documentation by Domain

For detailed API documentation, refer to the following domain-specific files:

- [Authentication APIs](./docs/api/Authentication_API.md)
- [Event APIs](./docs/api/Event_API.md)
- [Event Location APIs](./docs/api/EventLocation_API.md)
- [Participant APIs](./docs/api/Participant_API.md)
- [Reward APIs](./docs/api/Reward_API.md)
- [Reward Assignment APIs](./docs/api/RewardAssignment_API.md)
- [Common Error Responses](./docs/api/Error_Responses.md)

## Security Notes

- All endpoints require authentication except for the login and register endpoints.
- Use the `Authorization` header with the `Bearer` token for authenticated requests.
- Tokens have an expiration time and will need to be refreshed by logging in again after expiration.
- Different API endpoints may require specific roles/permissions which are validated server-side.
- CORS is configured to allow requests from any origin during development. In production, this should be restricted.

## Frontend Integration Examples

### Example: Fetch Events

```javascript
fetch('/api/events', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + accessToken
  }
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

### Example: Create Event

```javascript
fetch('/api/events', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + accessToken
  },
  body: JSON.stringify({
    name: 'Lucky Draw Event',
    description: 'Annual company event with prizes',
    startDate: '2023-12-31T18:00:00',
    endDate: '2023-12-31T23:00:00',
    status: 'PLANNED'
  })
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

### Example: Authentication

```javascript
// Login
fetch('/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'admin',
    password: 'password123'
  })
})
.then(response => response.json())
.then(data => {
  // Store the token
  localStorage.setItem('accessToken', data.accessToken);
})
.catch(error => console.error('Error:', error));

// Using the token for authenticated requests
const accessToken = localStorage.getItem('accessToken');
```

### Example: Assign Reward to Participant

```javascript
fetch('/api/rewards/2/assign/1', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + accessToken
  }
})
.then(response => response.json())
.then(data => console.log('Reward assigned:', data))
.catch(error => console.error('Error:', error));
```

### Example: Check In Participant

```javascript
fetch('/api/participants/3/check-in', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + accessToken
  }
})
.then(response => response.json())
.then(data => console.log('Participant checked in:', data))
.catch(error => console.error('Error:', error));
```
