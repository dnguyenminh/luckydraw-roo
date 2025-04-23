-- Specific schema for H2 database (test environment)

-- Drop tables if they exist to avoid conflicts
DROP TABLE IF EXISTS spin_histories;
DROP TABLE IF EXISTS golden_hours;
DROP TABLE IF EXISTS rewards;
DROP TABLE IF EXISTS participant_events;
DROP TABLE IF EXISTS participants;
DROP TABLE IF EXISTS event_locations;
DROP TABLE IF EXISTS provinces;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS regions;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS blacklisted_tokens;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS configurations;
DROP TABLE IF EXISTS audit_logs;

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
    code VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    region_id BIGINT NOT NULL,
    version BIGINT DEFAULT 0,
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

-- Create event_locations table with direct ID instead of composite key
CREATE TABLE IF NOT EXISTS event_locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    max_spin INT,
    quantity INT CHECK (quantity >= 0),
    win_probability DECIMAL(5,4),
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    daily_spin_dist_rate DOUBLE DEFAULT 0,  -- Original field from entity
    remaining_today_spin DOUBLE DEFAULT 0,  -- Add the missing column that Hibernate expects
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_event_locations_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_event_locations_region FOREIGN KEY (region_id) REFERENCES regions(id)
);

CREATE TABLE participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20),
    address VARCHAR(255),
    lastAddingSpin INT DEFAULT 0,
    province_id BIGINT NOT NULL,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (province_id) REFERENCES provinces(id)
);

CREATE TABLE participant_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    event_id BIGINT NOT NULL,
    event_location_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    spins_remaining INT DEFAULT 0,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_location_id) REFERENCES event_locations(id),
    FOREIGN KEY (participant_id) REFERENCES participants(id),
    FOREIGN KEY (event_id) REFERENCES events(id)
);

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
    prizeValue DECIMAL(10,2) DEFAULT 0,
    probability DECIMAL(5,4) DEFAULT 0,
    quantity INT DEFAULT 0,
    event_location_id BIGINT NOT NULL,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_location_id) REFERENCES event_locations(id)
);

CREATE TABLE golden_hours (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    event_location_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    multiplier DECIMAL(5,2) DEFAULT 1.0,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_location_id) REFERENCES event_locations(id)
);

CREATE TABLE spin_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    participant_event_id BIGINT NOT NULL,
    spin_time TIMESTAMP NOT NULL,
    reward_id BIGINT,
    win BOOLEAN DEFAULT false,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (participant_event_id) REFERENCES participant_events(id),
    FOREIGN KEY (reward_id) REFERENCES rewards(id)
);

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    role_type VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    display_order INT DEFAULT 0,
    version BIGINT DEFAULT 0
);

CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    account_expired BOOLEAN DEFAULT false,
    account_locked BOOLEAN DEFAULT false,
    credentials_expired BOOLEAN DEFAULT false,
    version BIGINT DEFAULT 0
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

CREATE TABLE blacklisted_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    token VARCHAR(1000) NOT NULL,
    token_type VARCHAR(50) NOT NULL,
    expiration_time TIMESTAMP NOT NULL,
    user_id BIGINT,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE configurations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value VARCHAR(1000) NOT NULL,
    description VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    object_type VARCHAR(100) NOT NULL,
    object_id VARCHAR(255),
    property_path VARCHAR(255),
    old_value VARCHAR(1000),
    new_value VARCHAR(1000),
    value_type VARCHAR(100),
    update_time TIMESTAMP NOT NULL,
    context VARCHAR(255),
    action_type VARCHAR(50) NOT NULL,
    version BIGINT DEFAULT 0
);

-- Create indexes for better performance
CREATE INDEX idx_event_location_event ON event_locations(event_id);
CREATE INDEX idx_event_location_region ON event_locations(region_id);
CREATE INDEX idx_participant_province ON participants(province_id);
CREATE INDEX idx_participant_event_participant ON participant_events(participant_id);
CREATE INDEX idx_participant_event_location ON participant_events(event_location_id);
CREATE INDEX idx_participant_event_event ON participant_events(event_id);
CREATE INDEX idx_reward_event_location ON rewards(event_location_id);
CREATE INDEX idx_golden_hour_event_location ON golden_hours(event_location_id);
CREATE INDEX idx_spin_history_participant_event ON spin_histories(participant_event_id);
CREATE INDEX idx_spin_history_reward ON spin_histories(reward_id);
CREATE INDEX idx_blacklisted_token_user ON blacklisted_tokens(user_id);
CREATE INDEX idx_audit_log_object_type ON audit_logs(object_type);
CREATE INDEX idx_audit_log_object_id ON audit_logs(object_id);
CREATE INDEX idx_audit_log_update_time ON audit_logs(update_time);
CREATE INDEX idx_audit_log_action_type ON audit_logs(action_type);
CREATE INDEX idx_audit_log_status ON audit_logs(status);
