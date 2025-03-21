# Architecture Documentation

## Table of Contents
1. [Overview](#overview)
2. [System Architecture](#system-architecture)
3. [Data Architecture](#data-architecture)
4. [Demo Data Management](#demo-data-management)

## Overview

The Lucky Draw application is designed with a modular, maintainable architecture following Domain-Driven Design principles. The system supports event management, participant tracking, and reward distribution through a secure, scalable platform.

## System Architecture

### Layers
1. Presentation Layer
   - REST Controllers
   - DTOs
   - Request/Response mapping

2. Business Layer (Service Layer)
   - Services with generic BaseService interface
   - Domain logic including event processing
   - Authentication and JWT handling
   - Transaction management
   - Request validation
   - DTO-Entity mapping
   - Specialized services:
     * AuthenticationService: Handles user authentication
     * JwtService: Manages JWT token operations
     * SpinService: Controls spin game logic
     * Event & Location management services
     * User & Role management services

3. Data Layer (Repository Layer)
   - Generic BaseRepository interface
   - JPA repositories for all entities
   - Entity mapping and relationships
   - Query optimization
   - Data access patterns
   - Specialized repositories for:
     * Event management
     * User authentication
     * Participant tracking
     * Spin history recording
     * Location management

### Architecture Diagrams

#### Repository Class Structure
![Repository Classes](src/main/resources/diagram/repository-classes.png)

The repository layer provides a robust data access abstraction with:
- Generic JpaRepository interface from Spring Data JPA
- Custom BaseRepository interface with common operations
- Specialized repository interfaces with domain-specific queries
- Support for soft delete operations
- Custom finder methods for efficient querying
- Transaction management integration

Key Repository Features:
1. Base Operations (BaseRepository)
   - Standard CRUD operations inherited from JpaRepository
   - Status-based querying (findByStatus)
   - Soft delete functionality (softDelete/restore)
   - Type-safe generic implementations

2. Domain-Specific Operations with Generated Queries
   - Event Repository:
     * Code-based lookup: `SELECT e FROM Event e WHERE e.code = ?1`
     * Date range queries: `SELECT e FROM Event e WHERE e.startDate BETWEEN ?1 AND ?2`
     * Active events: `SELECT e FROM Event e WHERE e.status = 'ACTIVE' AND e.endDate > CURRENT_TIMESTAMP`

   - SpinHistory Repository:
     * Participant history: `SELECT sh FROM SpinHistory sh WHERE sh.participantEvent.id = ?1`
     * Time analysis: `SELECT sh FROM SpinHistory sh WHERE sh.spinTime BETWEEN ?1 AND ?2`
     * Reward tracking: `SELECT COUNT(sh) FROM SpinHistory sh WHERE sh.reward.id = ?1`

   - Reward Repository:
     * Availability: `SELECT r FROM Reward r WHERE r.remainingQuantity > 0 AND r.status = 'ACTIVE'`
     * Quantity management: `UPDATE Reward r SET r.remainingQuantity = ?2 WHERE r.id = ?1`
     * Probability filtering: `SELECT r FROM Reward r WHERE r.probability > ?1`

   - GoldenHour Repository:
     * Active periods: Complex query with time functions and day-of-week checking
     * Time range filtering: `SELECT gh FROM GoldenHour gh WHERE gh.startTime >= ?1 AND gh.endTime <= ?2`

3. Query Optimization
   - Efficient join fetching for related entities
   - Custom projections for specific use cases
   - Pagination support for large datasets
   - Caching integration points

#### High-Level Architecture
![Repository and Service Layer UML](src/main/resources/diagram/repository-service-uml.png)

#### Implementation Details
![Implementation Architecture](src/main/resources/diagram/implementation-uml.png)

The implementation diagram shows the concrete relationships between services, repositories, and mappers, including:
- How JpaRepository is extended by our BaseRepository
- Service implementation inheritance hierarchy
- Mapper integration with services
- Cross-layer dependencies between services and repositories

### Key Design Patterns and Implementation Details
1. Generic Repository Pattern
   - BaseRepository<T, ID> interface
   - Type-safe CRUD operations
   - Extensible for custom queries

2. Generic Service Pattern
   - BaseService<T, ID, R, C, U> interface
   - AbstractBaseService implementation
   - Standardized CRUD operations
   - Type-safe request/response DTOs

3. DTO Pattern
   - Separate DTOs for create/update/response
   - Validation annotations
   - Clean separation from entities

4. Mapper Pattern
   - Dedicated mapper interfaces
   - Type-safe conversions
   - Structured mapping rules

### Key Components
- Security: Spring Security with JWT
- Persistence: Spring Data JPA
- Validation: Bean Validation
- Testing: JUnit 5, AssertJ

## Data Architecture

### Entity Relationships
![Repository Entities](src/main/resources/diagram/repository-entities.png)

The diagram above shows the core entities and their relationships in the system:

1. User Management
   - User: Core user entity with authentication details
   - Role: Defines user roles (ADMIN, MANAGER, USER, PARTICIPANT)
   - Many-to-many relationship between Users and Roles

2. Event Management
   - Event: Central entity for managing promotional events
   - EventLocation: Physical locations where events take place
   - GoldenHour: Special time periods with enhanced rewards
   - Region and Province: Geographical hierarchy for event organization

3. Participant Management
   - Participant: Represents event participants
   - ParticipantEvent: Tracks participant registration in events
   - SpinHistory: Records spin game results
   - Reward: Defines available prizes and their probabilities

### Key Relationships
- Events have multiple locations and golden hours
- Locations are organized by provinces and regions
- Participants join events through ParticipantEvent
- SpinHistory tracks rewards won by participants
- Users have multiple roles for authorization

### Data Flow and State Management
1. Event Creation
2. Participant Registration
3. Reward Distribution
4. History Tracking

## Demo Data Management

### Overview
The application includes a comprehensive demo data setup that provides a realistic testing and demonstration environment. The demo data includes historical, current, and future records to demonstrate the full system lifecycle.

### Enabling Demo Data

1. Configure application profile:
```yaml
spring:
  profiles:
    active: demo
```

2. The demo data will be automatically populated by DemoDataInitializer when the application starts with the "demo" profile.

### Demo Data Structure

The demo data includes:

1. User Roles and Access Levels:
   - ADMIN: System administrators
   - OPERATOR: System maintenance staff
   - MANAGER: Event managers
   - USER: Basic system users
   - PARTICIPANT: Regular event participants
   - VIP: Premium participants

2. Events Timeline:
   - Past events (2022-2023): Historical data with complete spin histories
   - Current events (2024): Active events for testing
   - Future events (2024-2025): Planned events
   
3. Geographic Coverage:
   - 3 regions (North, Central, South)
   - 6 key provinces
   - 1200+ event locations

4. Participant Data:
   - 1500+ participants
   - Mix of regular and VIP users
   - Distributed across regions
   - Various registration dates

5. Event Activity:
   - 5000+ spin histories
   - Different reward types
   - Golden hour participation
   - Points and rewards tracking

### Data Time Periods

The demo data spans multiple time periods to demonstrate system features:

- Past (Historical):
  - 3 years ago: Initial events
  - 2 years ago: Historical activities
  - 1 year ago: Recent history
  
- Current:
  - Active events
  - Ongoing participation
  - Current golden hours
  
- Future:
  - Upcoming events
  - Scheduled activities
  - Future promotions

### Using Demo Data

1. For Development:
   ```bash
   # Start application with demo profile
   ./mvnw spring-boot:run -Dspring.profiles.active=demo
   ```

2. For Testing:
   - Demo data provides comprehensive test scenarios
   - Covers all business cases
   - Includes edge cases and special conditions

3. For Demonstration:
   - Shows full event lifecycle
   - Demonstrates participant progression
   - Illustrates VIP features
   - Displays historical analysis capabilities

### Refreshing Demo Data

The demo data can be refreshed in several ways:

1. Using the Provided Script (Recommended):
   ```bash
   # Make script executable
   chmod +x scripts/refresh-demo-data.sh
   
   # Run with default configuration
   ./scripts/refresh-demo-data.sh
   
   # Or with custom database settings
   DB_HOST=custom-host DB_PORT=5433 ./scripts/refresh-demo-data.sh
   ```
   The script will:
   - Clean existing data
   - Start application with demo profile
   - Reload all demo data
   - Use environment variables for configuration

2. Application Restart:
   ```bash
   # Start with demo profile - data will be refreshed
   ./mvnw spring-boot:run -Dspring.profiles.active=demo
   ```

3. Manual Database Reset:
   ```sql
   -- Clear all data
   DELETE FROM spin_histories;
   DELETE FROM participant_events;
   -- ... [other tables]
   
   -- Restart application with demo profile
   ```

### Data Volumes

The demo data includes:
- Users: 1500+
- Events: 10+
- Locations: 1200+
- Participants: 1500+
- Spins: 5000+
- Rewards: 100+
- Golden Hours: 200+

This provides sufficient data volume for:
- Performance testing
- Data analysis
- UI pagination
- Report generation

### Maintenance

The demo data is maintained through:

1. SQL Template:
   - Location: `src/main/resources/db/demo/demo-data-template.sql`
   - Contains all data generation logic
   - Uses timestamp placeholders

2. Data Initializer:
   - Class: `DemoDataInitializer`
   - Handles timestamp updates
   - Manages data population

3. Version Control:
   - Template is versioned with application
   - Changes tracked in Git
   - Documentation updated with changes

## Security Architecture
[Security documentation continues...]
