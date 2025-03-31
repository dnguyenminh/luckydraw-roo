# Lucky Draw API Documentation

## Overview

This documentation provides details on the RESTful APIs available in the Lucky Draw application. These endpoints allow frontend applications to interact with the backend services for managing events, participants, rewards, and the lucky draw process.

## Authentication

All API endpoints (except for login and registration) require authentication using JWT tokens. See the [Authentication API](./Authentication_API.md) documentation for details on obtaining and using tokens.

## Available API Documentation

- [Authentication API](./Authentication_API.md) - Login, register, and token validation
- [Event API](./Event_API.md) - Event management operations
- [Event Location API](./EventLocation_API.md) - Event locations and venues
- [Participant API](./Participant_API.md) - Participant management and check-in
- [Reward API](./Reward_API.md) - Reward management
- [Reward Assignment API](./RewardAssignment_API.md) - Assigning rewards to participants
- [Common Error Responses](./Error_Responses.md) - Standard error formats

## Important Security Notes

- Always transmit credentials and tokens over HTTPS
- Store tokens securely on the client side
- JWT tokens have an expiration time and should be refreshed
- Handle token expiration gracefully in your frontend application

## Integration Support

For questions or issues related to API integration, please contact the development team at dev-support@example.com or create an issue in the project repository.
