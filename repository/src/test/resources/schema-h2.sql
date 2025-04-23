-- Repository-specific schema for H2 database (test environment)

-- Drop tables if they exist to avoid conflicts
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
DROP TABLE IF EXISTS user_roles; -- Still drop the table to clean up if it exists
DROP TABLE IF EXISTS blacklisted_tokens;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS configurations;
DROP TABLE IF EXISTS audit_logs;

-- Create tables with proper columns and relationships
CREATE TABLE regions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(1000),
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
    description VARCHAR(1000),
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
    description VARCHAR(1000),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0
);

-- Create event_locations table with composite primary key
CREATE TABLE event_locations (
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
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
    participant_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    spins_remaining INT DEFAULT 0,
    version BIGINT DEFAULT 0,
    PRIMARY KEY (participant_id, event_id, region_id),
    FOREIGN KEY (participant_id) REFERENCES participants(id),
    FOREIGN KEY (event_id, region_id) REFERENCES event_locations(event_id, region_id)
);

CREATE TABLE rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(1000),
    event_location_id BIGINT NOT NULL,
    prizeValue DECIMAL(15,2) NOT NULL,
    quantity INT DEFAULT 0,
    remaining_quantity INT DEFAULT 0,
    probability DOUBLE NOT NULL,
    version BIGINT DEFAULT 0
);

CREATE TABLE reward_events (
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    reward_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    today_quantity  INTEGER NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    PRIMARY KEY(event_id, region_id, reward_id),
    FOREIGN KEY (event_id) REFERENCES events(id),
    FOREIGN KEY (region_id) REFERENCES regions(id),
    FOREIGN KEY (reward_id) REFERENCES rewards(id)
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
    multiplier DECIMAL(5,2) NOT NULL DEFAULT 1.0,
    max_rewards INT,
    claimed_rewards INT DEFAULT 0,
    version BIGINT DEFAULT 0
);

CREATE TABLE spin_histories (
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
    FOREIGN KEY (participant_id, participant_event_id, participant_region_id) REFERENCES participant_events(participant_id, event_id, region_id),
    FOREIGN KEY (reward_id, reward_event_id, reward_region_id) REFERENCES reward_events(reward_id, event_id, region_id),
    FOREIGN KEY (golden_hour_id) REFERENCES golden_hours(id)
);

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    role_type VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(255),
    display_order INT,
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
    type VARCHAR(20),
    description VARCHAR(255),
    version BIGINT DEFAULT 0
);

-- Users table with direct role relationship
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    role_id BIGINT,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Remove user_roles table creation as it's not needed
-- User to role relationship is handled by role_id column in users table

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY(role_id, permission_id),
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
    token_type VARCHAR(20) NOT NULL,
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
    config_value VARCHAR(500) NOT NULL,
    description VARCHAR(500),
    data_type VARCHAR(50),
    validation_regex VARCHAR(255),
    modifiable BOOLEAN DEFAULT TRUE,
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
CREATE INDEX idx_reward_location ON reward_events(event_id, region_id);
CREATE INDEX idx_reward ON reward_events(reward_id);
CREATE INDEX idx_reward_status ON reward_events(status);
CREATE INDEX idx_golden_hour_event_location ON golden_hours(event_location_id);
CREATE INDEX idx_spin_history_participant_event ON spin_histories(participant_event_id);
CREATE INDEX idx_spin_history_reward ON spin_histories(reward_id);
CREATE INDEX idx_blacklisted_token_user ON blacklisted_tokens(user_id);
CREATE INDEX idx_blacklisted_token_type ON blacklisted_tokens(token_type);
CREATE INDEX idx_audit_log_object_type ON audit_logs(object_type);
CREATE INDEX idx_audit_log_object_id ON audit_logs(object_id);
CREATE INDEX idx_audit_log_update_time ON audit_logs(update_time);
CREATE INDEX idx_audit_log_action_type ON audit_logs(action_type);
