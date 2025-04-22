-- Schema definition for Lucky Draw application

-- Clear existing tables to avoid conflicts
DROP TABLE IF EXISTS spin_histories CASCADE;
DROP TABLE IF EXISTS golden_hours CASCADE;
DROP TABLE IF EXISTS reward_events CASCADE;
DROP TABLE IF EXISTS rewards CASCADE;
DROP TABLE IF EXISTS participant_events CASCADE;
DROP TABLE IF EXISTS participants CASCADE;
DROP TABLE IF EXISTS event_locations CASCADE;
DROP TABLE IF EXISTS provinces CASCADE;
DROP TABLE IF EXISTS regions CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS blacklisted_tokens CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS configurations CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;

-- Regions table
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

-- Provinces table
CREATE TABLE provinces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    region_id BIGINT NOT NULL,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (region_id) REFERENCES regions(id)
);

-- Events table
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
    daily_spin_distributing_rate DOUBLE DEFAULT 0,
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

-- Permissions table
CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    name VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(20),
    description VARCHAR(255),
    version BIGINT DEFAULT 0
);

-- Users table - MUST BE CREATED AFTER roles table
CREATE TABLE users (
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
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Participants table
CREATE TABLE participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20),
    address VARCHAR(255),
    last_adding_spin INT DEFAULT 0,
    province_id BIGINT NOT NULL,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (province_id) REFERENCES provinces(id)
);

-- Participant Events join table
CREATE TABLE participant_events (
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

-- Rewards table
CREATE TABLE rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    prize_value DECIMAL(15,2),
    version BIGINT DEFAULT 0
);

-- Reward Events table
CREATE TABLE reward_events (
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    reward_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    quantity INTEGER NOT NULL DEFAULT 0,
    today_quantity INTEGER NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    PRIMARY KEY(event_id, region_id, reward_id),
    FOREIGN KEY (event_id, region_id) REFERENCES event_locations(event_id, region_id),
    FOREIGN KEY (reward_id) REFERENCES rewards(id)
);

-- Golden Hours table
CREATE TABLE golden_hours (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    multiplier DECIMAL(5,2) NOT NULL DEFAULT 1.0,
    max_rewards INT,
    claimed_rewards INT DEFAULT 0,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_id, region_id) REFERENCES event_locations(event_id, region_id)
);

-- Spin Histories table
CREATE TABLE spin_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    participant_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    spin_time TIMESTAMP NOT NULL,
    reward_id BIGINT,
    reward_event_id BIGINT,
    reward_region_id BIGINT,
    win BOOLEAN NOT NULL DEFAULT FALSE,
    wheel_position DOUBLE,
    multiplier DECIMAL(5,2) DEFAULT 1.0,
    server_seed VARCHAR(100),
    client_seed VARCHAR(100),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (participant_id, event_id, region_id) REFERENCES participant_events(participant_id, event_id, region_id),
    FOREIGN KEY (reward_id, reward_event_id, reward_region_id) REFERENCES reward_events(reward_id, event_id, region_id)
);

-- Role-Permission join table
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

-- Blacklisted Tokens table
CREATE TABLE blacklisted_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    token VARCHAR(1000) NOT NULL,
    token_type VARCHAR(20) NOT NULL,
    expiration_time TIMESTAMP NOT NULL,
    user_id BIGINT,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Configurations table
CREATE TABLE configurations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description TEXT,
    data_type VARCHAR(50),
    validation_regex VARCHAR(255),
    modifiable BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0
);

-- Audit Logs table
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    object_type VARCHAR(100) NOT NULL,
    object_id VARCHAR(255) NOT NULL,
    property_path VARCHAR(255),
    old_value TEXT,
    new_value TEXT,
    value_type VARCHAR(255),
    update_time TIMESTAMP NOT NULL,
    context VARCHAR(255),
    action_type VARCHAR(50) NOT NULL,
    version BIGINT DEFAULT 0
);

-- Create indexes for performance
CREATE INDEX idx_province_region ON provinces(region_id);
CREATE INDEX idx_participant_province ON participants(province_id);
CREATE INDEX idx_event_status_dates ON events(status, start_time, end_time);
CREATE INDEX idx_golden_hour_location ON golden_hours(event_id, region_id);
CREATE INDEX idx_reward_event_location ON reward_events(event_id, region_id);
CREATE INDEX idx_reward_id ON reward_events(reward_id);
CREATE INDEX idx_spin_history_pe ON spin_histories(participant_id, event_id, region_id);
CREATE INDEX idx_spin_history_reward ON spin_histories(reward_id);
CREATE INDEX idx_participant_event_participant ON participant_events(participant_id);
CREATE INDEX idx_participant_event_location ON participant_events(event_id, region_id);
CREATE INDEX idx_event_location_event ON event_locations(event_id);
CREATE INDEX idx_event_location_region ON event_locations(region_id);
CREATE INDEX idx_audit_log_object ON audit_logs(object_type, object_id);
CREATE INDEX idx_audit_log_action ON audit_logs(action_type);
CREATE INDEX idx_user_role ON users(role_id);
