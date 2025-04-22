-- Create schema for test database with quoted table names to preserve case

-- Users table (corrected to match entity model with direct role_id foreign key)
CREATE TABLE IF NOT EXISTS "users" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    role_id BIGINT,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (role_id) REFERENCES "roles"(id)
);

-- Roles table
CREATE TABLE IF NOT EXISTS "roles" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100),
    role_type VARCHAR(100),
    status VARCHAR(50),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Permissions table
CREATE TABLE IF NOT EXISTS "permissions" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100),
    status VARCHAR(50),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Role permissions join table
CREATE TABLE IF NOT EXISTS "role_permissions" (
    role_id BIGINT,
    permission_id BIGINT,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES "roles"(id),
    FOREIGN KEY (permission_id) REFERENCES "permissions"(id)
);

-- Region table
CREATE TABLE IF NOT EXISTS "regions" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100),
    status VARCHAR(50),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Province table
CREATE TABLE IF NOT EXISTS "provinces" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100),
    region_id BIGINT,
    status VARCHAR(50),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (region_id) REFERENCES "regions"(id)
);

-- Event table
CREATE TABLE IF NOT EXISTS "events" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100),
    description TEXT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(50),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Event Location table with composite primary key
CREATE TABLE IF NOT EXISTS event_locations (
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
    daily_spin_distributing_rate DOUBLE DEFAULT 0,
    version BIGINT DEFAULT 0,
    PRIMARY KEY (event_id, region_id),
    FOREIGN KEY (event_id) REFERENCES events(id),
    FOREIGN KEY (region_id) REFERENCES regions(id)
);

-- Golden Hour table
CREATE TABLE IF NOT EXISTS golden_hours (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    event_location_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    multiplier DECIMAL(5,2) NOT NULL,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_golden_hours_event_location FOREIGN KEY (event_location_id) REFERENCES event_locations(id)
);

-- Reward table
CREATE TABLE IF NOT EXISTS rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    event_location_id BIGINT NOT NULL,
    prize_value DECIMAL(10,2),
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_rewards_event_location FOREIGN KEY (event_location_id) REFERENCES event_locations(id)
);

-- Participant table
CREATE TABLE IF NOT EXISTS "participants" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(255),
    status VARCHAR(50),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Participant Event table with composite primary key
CREATE TABLE IF NOT EXISTS participant_events (
    participant_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    spins_remaining INT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    PRIMARY KEY (participant_id, event_id, region_id),
    FOREIGN KEY (participant_id) REFERENCES participants(id),
    FOREIGN KEY (event_id, region_id) REFERENCES event_locations(event_id, region_id)
);

-- Spin History table
CREATE TABLE IF NOT EXISTS "spin_histories" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    participant_id BIGINT NOT NULL,
    participant_event_id BIGINT NOT NULL,
    participant_region_id BIGINT NOT NULL,
    spin_time TIMESTAMP NOT NULL,
    reward_id BIGINT,
    reward_event_id BIGINT,
    reward_region_id BIGINT,
    golden_hour_id BIGINT,
    win BOOLEAN DEFAULT FALSE,
    wheel_position DOUBLE,
    multiplier DECIMAL(5,2) DEFAULT 1.0,
    server_seed VARCHAR(100),
    client_seed VARCHAR(100),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (participant_id, participant_event_id, participant_region_id) REFERENCES "participant_events"(participant_id, event_id, region_id),
    FOREIGN KEY (reward_id, reward_event_id, reward_region_id) REFERENCES "reward_events"(reward_id, event_id, region_id),
    FOREIGN KEY (golden_hour_id) REFERENCES "golden_hours"(id)
);

-- Audit Log table
CREATE TABLE IF NOT EXISTS "audit_logs" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100),
    entity VARCHAR(100),
    entity_id BIGINT,
    details TEXT,
    timestamp TIMESTAMP,
    status VARCHAR(50),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Configuration table
CREATE TABLE IF NOT EXISTS "configurations" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    config_value TEXT,
    description TEXT,
    status VARCHAR(50),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Blacklisted token table
CREATE TABLE IF NOT EXISTS "blacklisted_tokens" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token TEXT NOT NULL,
    token_type VARCHAR(50),
    expiration_time TIMESTAMP,
    user_id BIGINT,
    status VARCHAR(50),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES "users"(id)
);

-- Create sequences for ID generation
CREATE SEQUENCE IF NOT EXISTS region_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS province_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS users_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS role_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS permission_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS event_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS event_location_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS golden_hour_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS reward_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS participant_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS participant_event_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS spin_history_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS audit_log_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS configuration_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS blacklisted_token_id_seq START WITH 1;
