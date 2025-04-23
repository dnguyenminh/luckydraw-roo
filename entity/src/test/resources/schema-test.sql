-- Schema definition for Entity module tests
-- Clear existing tables to avoid conflicts
DROP TABLE IF EXISTS spin_histories;
DROP TABLE IF EXISTS golden_hours;
DROP TABLE IF EXISTS reward_events;
DROP TABLE IF EXISTS rewards;
DROP TABLE IF EXISTS participant_events;
DROP TABLE IF EXISTS participants;
DROP TABLE IF EXISTS event_locations;
DROP TABLE IF EXISTS provinces;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS regions;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS blacklisted_tokens;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS configurations;
DROP TABLE IF EXISTS audit_logs;

-- Regions table - MUST BE CREATED BEFORE event_locations
CREATE TABLE regions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    version BIGINT DEFAULT 0
);

-- Events table - MUST BE CREATED BEFORE event_locations
CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0
);

-- Event Locations table with composite primary key
CREATE TABLE event_locations (
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    description TEXT,
    max_spin INT DEFAULT 100,
    today_spin INT DEFAULT 100,
    daily_spin_dist_rate DOUBLE DEFAULT 0,  -- Original field from entity
    remaining_today_spin DOUBLE DEFAULT 0,  -- Add the missing column that Hibernate expects
    version BIGINT DEFAULT 0,
    PRIMARY KEY (event_id, region_id),
    FOREIGN KEY (event_id) REFERENCES events(id),
    FOREIGN KEY (region_id) REFERENCES regions(id)
);

-- Roles table - MUST BE CREATED BEFORE users table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    role_type VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(255),
    display_order INT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0
);

-- Add other tables as needed for your entity tests
-- This minimal schema focuses on fixing the event_locations table
