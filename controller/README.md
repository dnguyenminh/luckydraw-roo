# Controller Module Documentation

## Overview
This module contains REST controllers for the Lucky Draw application, including security configurations and comprehensive test coverage.

## Package Structure
```
controller/
├── src/
│   ├── main/
│   │   └── java/vn/com/fecredit/app/controller/
│   │       ├── auth/
│   │       │   ├── AuthenticationController.java
│   │       │   └── dto/
│   │       ├── event/
│   │       │   ├── EventController.java
│   │       │   └── EventLocationController.java
│   │       ├── participant/
│   │       │   └── ParticipantController.java
│   │       ├── reward/
│   │       │   └── RewardController.java
│   │       ├── user/
│   │       │   └── UserController.java
│   │       ├── config/
│   │       │   ├── SecurityConfig.java
│   │       │   └── WebMvcConfig.java
│   │       ├── error/
│   │       │   └── GlobalExceptionHandler.java
│   │       └── util/
│   │           └── SecurityUtils.java
│   └── test/
│       └── java/vn/com/fecredit/app/controller/
│           ├── base/
│           │   └── BaseControllerTest.java
│           ├── auth/
│           ├── config/
│           └── util/
```

## Features
- RESTful API endpoints
- JWT-based authentication
- Role-based access control
- Request validation
- Global error handling
- Comprehensive test coverage

## Security Features
1. JWT Authentication
   - Token-based authentication
   - Token refresh mechanism
   - Token validation
   - Role-based authorization

2. Security Configuration
   - CORS support
   - CSRF protection
   - Role hierarchies
   - Protected endpoints
   - Security filters

## Testing Infrastructure

### Test Configuration
1. Base Test Setup
```java
@SpringBootTest(classes = {TestSecurityConfiguration.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseControllerTest {
    // Base test functionality
}
```

2. Security Test Support
```java
@WithMockCustomUser(username = "admin", roles = {"ADMIN"})
void testSecuredEndpoint() {
    // Test code
}
```

### Available Test Utilities
1. Security Context Setup
```java
setupSecurityContext("testuser", "ADMIN", "USER");
```

2. JWT Token Creation
```java
String token = createAuthToken("testuser", "ADMIN");
```

3. Mock User Creation
```java
@WithMockCustomUser(
    username = "testuser",
    roles = {"USER", "ADMIN"},
    authorities = {"WRITE", "READ"}
)
```

## Testing Guide

### Running Tests
```bash
# Run all tests
./gradlew :controller:test

# Run specific test class
./gradlew :controller:test --tests "vn.com.fecredit.app.controller.auth.AuthenticationControllerTest"

# Run with test coverage report
./gradlew :controller:test jacocoTestReport
```

### Writing Tests
1. Controller Tests
```java
class YourControllerTest extends BaseControllerTest {
    @Override
    protected Object getController() {
        return yourController;
    }

    @Test
    @WithMockCustomUser(roles = "ADMIN")
    void testSecuredEndpoint() {
        // Test code
    }
}
```

2. Security Tests
```java
@Test
void testJwtToken() {
    String token = createAuthToken("user", "ADMIN");
    mockMvc.perform(get("/api/secured")
        .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
}
```

## Best Practices
1. Security
   - Always use @WithMockCustomUser for secured endpoints
   - Test both positive and negative security scenarios
   - Verify token validation and expiration
   - Test role-based access control

2. Testing
   - Extend BaseControllerTest for consistency
   - Use meaningful test names
   - Test error scenarios
   - Clean up security context after tests
   - Mock external dependencies

3. API Design
   - Use proper HTTP methods
   - Follow REST naming conventions
   - Include validation
   - Handle errors consistently
   - Document endpoints

## Dependencies
See `build.gradle` for complete list of dependencies.

## Configuration
- Main configuration: `application.properties`
- Test configuration: `application-test.properties`
- Security configuration: `SecurityConfig.java`
- Web configuration: `WebMvcConfig.java`