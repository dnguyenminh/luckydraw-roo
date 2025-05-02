-- Test-specific schema

-- Drop tables if they exist to avoid conflicts
DROP TABLE IF EXISTS spin_histories CASCADE;
DROP TABLE IF EXISTS golden_hours CASCADE;
DROP TABLE IF EXISTS rewards CASCADE;
DROP TABLE IF EXISTS participant_events CASCADE;
DROP TABLE IF EXISTS participants CASCADE;
DROP TABLE IF EXISTS event_locations CASCADE;
DROP TABLE IF EXISTS region_province CASCADE;
DROP TABLE IF EXISTS provinces CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS regions CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS blacklisted_tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- Create tables for entities
CREATE TABLE regions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE TABLE provinces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    version BIGINT DEFAULT 0
);

-- Create many-to-many relationship table
CREATE TABLE region_province (
    province_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    PRIMARY KEY (province_id, region_id),
    FOREIGN KEY (province_id) REFERENCES provinces(id),
    FOREIGN KEY (region_id) REFERENCES regions(id)
);

CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0
);

-- Create event_locations table with compound primary key
CREATE TABLE event_locations (
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    province_id BIGINT NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    max_spin INT DEFAULT 0,
    quantity INT DEFAULT 0,
    win_probability DECIMAL(5,2) DEFAULT 0,
    daily_spin_dist_rate DOUBLE DEFAULT 0,
    remaining_today_spin DOUBLE DEFAULT 0,
    version BIGINT DEFAULT 0,
    PRIMARY KEY (event_id, region_id),
    FOREIGN KEY (event_id) REFERENCES events(id),
    FOREIGN KEY (region_id) REFERENCES regions(id),
    FOREIGN KEY (province_id) REFERENCES provinces(id)
);

-- Create test participants table
CREATE TABLE participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(50),
    address VARCHAR(255),
    province_id BIGINT,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (province_id) REFERENCES provinces(id)
);

-- Create participant_events table with compound key structure
CREATE TABLE participant_events (
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    spins_remaining INT DEFAULT 0,
    version BIGINT DEFAULT 0,
    PRIMARY KEY (event_id, region_id, participant_id),
    FOREIGN KEY (event_id, region_id) REFERENCES event_locations(event_id, region_id),
    FOREIGN KEY (participant_id) REFERENCES participants(id)
);

-- Update rewards table to reference the compound key of event_locations
CREATE TABLE rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    prize_value DECIMAL(10,2) DEFAULT 0,
    probability DECIMAL(5,4) DEFAULT 0,
    quantity INT DEFAULT 0,
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_id, region_id) REFERENCES event_locations(event_id, region_id)
);

-- Update golden_hours table to reference the compound key of event_locations
CREATE TABLE golden_hours (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    multiplier DECIMAL(5,2) DEFAULT 1.0,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_id, region_id) REFERENCES event_locations(event_id, region_id)
);

-- Update spin_histories table to reference compound keys
CREATE TABLE spin_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    spin_time TIMESTAMP NOT NULL,
    reward_id BIGINT,
    reward_event_id BIGINT,
    reward_region_id BIGINT,
    win BOOLEAN DEFAULT false,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_id, region_id, participant_id) REFERENCES participant_events(event_id, region_id, participant_id),
    FOREIGN KEY (reward_id) REFERENCES rewards(id)
);

-- Create indexes for better performance
CREATE INDEX idx_region_province_region ON region_province(region_id);
CREATE INDEX idx_region_province_province ON region_province(province_id);
CREATE INDEX idx_event_location_province ON event_locations(province_id);

-- Replace the old participant_event_id index with indexes on the compound key columns
CREATE INDEX idx_spin_history_event ON spin_histories(event_id);
CREATE INDEX idx_spin_history_region ON spin_histories(region_id);
CREATE INDEX idx_spin_history_participant ON spin_histories(participant_id);
CREATE INDEX idx_spin_history_reward ON spin_histories(reward_id);

CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    permission_type VARCHAR(50), -- Changed from 'type' to 'permission_type'
    description VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    full_name VARCHAR(255),
    role_id BIGINT,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(id)
);
