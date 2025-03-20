# Architecture Documentation

## Overview
This document describes the key architectural decisions and patterns used in the Lucky Draw application.

## Domain Model Organization

### Geographical Hierarchy
- **Region**: Top-level geographical unit
  * Contains multiple provinces for administrative purposes
  * Directly manages event locations
  * Controls location availability through status
  * Provides geographical segmentation for events

- **Province**: Secondary geographical unit
  * Belongs to a region
  * Manages participant residency
  * Used for participant organization and reporting

### Event Organization
- **Event**: Central organizing entity
  * Configures participation rules
  * Manages rewards and bonus periods
  * Operates through event locations
  * Tracks participation through ParticipantEvent records

- **EventLocation**: Physical event venue
  * Located within a region (direct relationship)
  * Manages participant capacity
  * Tracks participation at location level
  * Status controlled by parent region

### Participant Management
- **Participant**: User participating in events
  * Registered to a province (for residency)
  * Can participate in multiple events
  * Participation tracked per event-location
  * Points and rewards tracked across events

### Reward System
- **Reward**: Prize configuration
  * Belongs to an event
  * Has win probability and inventory
  * Tracks remaining quantities

- **GoldenHour**: Bonus period
  * Configures multipliers for specific times
  * Applies to all locations in an event

### Audit and Status
All major entities inherit:
- Audit information (creation, modification)
- Status management (ACTIVE/INACTIVE)
- Version control for optimistic locking

## Key Design Decisions

1. **Region-EventLocation Relationship**
   - Direct relationship between regions and event locations
   - Enables regional control over event venues
   - Simplifies location management and reporting
   - Separates location organization from participant organization

2. **Province-Participant Relationship**
   - Provinces manage participant residency
   - Enables demographic reporting
   - Separates participant organization from event organization

3. **Event-ParticipantEvent Design**
   - Tracks participation context:
     * Which event
     * Which location
     * Which participant
     * Remaining spins
   - Enables flexible reporting and constraints

4. **Status Hierarchy**
   - Region status affects event location availability
   - Event status controls participation
   - Location status manages local availability

## Security Architecture

1. **Authentication**
   - Token-based authentication
   - Token blacklisting support
   - Audit trail for security events

2. **Authorization**
   - Role-based access control
   - Regional access restrictions
   - Operation-level permissions

## Validation Rules

1. **Location Constraints**
   - Must belong to active region
   - Must have valid capacity limits
   - Status changes follow region status

2. **Event Rules**
   - Valid time periods required
   - Participant limits enforced
   - Spin limits per participant

3. **Participation Rules**
   - Daily spin limits
   - Location capacity limits
   - Active status requirements