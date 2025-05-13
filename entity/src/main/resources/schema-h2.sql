-- Schema creation script for service tests using H2 database

-- Drop tables if they exist to ensure clean state
DROP TABLE IF EXISTS spin_histories CASCADE;
DROP TABLE IF EXISTS golden_hours CASCADE;
DROP TABLE IF EXISTS reward_events CASCADE;
DROP TABLE IF EXISTS rewards CASCADE;
DROP TABLE IF EXISTS participant_events CASCADE;
DROP TABLE IF EXISTS participants CASCADE;
DROP TABLE IF EXISTS event_locations CASCADE;
DROP TABLE IF EXISTS region_province CASCADE;
DROP TABLE IF EXISTS provinces CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS regions CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS blacklisted_tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS configurations CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;

-- Create base tables first
CREATE TABLE IF NOT EXISTS regions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS provinces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS region_province (
    region_id BIGINT NOT NULL,
    province_id BIGINT NOT NULL,
    PRIMARY KEY (region_id, province_id),
    FOREIGN KEY (region_id) REFERENCES regions(id),
    FOREIGN KEY (province_id) REFERENCES provinces(id)
);

CREATE TABLE IF NOT EXISTS events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS event_locations (
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    description TEXT,
    max_spin INT NOT NULL DEFAULT 100,
    today_spin INT NOT NULL DEFAULT 100,
    daily_spin_dist_rate DOUBLE NOT NULL DEFAULT 0.0,
    version BIGINT DEFAULT 0,
    PRIMARY KEY (event_id, region_id),
    FOREIGN KEY (event_id) REFERENCES events(id),
    FOREIGN KEY (region_id) REFERENCES regions(id)
);

CREATE TABLE IF NOT EXISTS participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20),
    email VARCHAR(255),
    address VARCHAR(255),
    province_id BIGINT,
    last_adding_spin INTEGER,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (province_id) REFERENCES provinces(id)
);

CREATE TABLE IF NOT EXISTS participant_events (
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

CREATE TABLE IF NOT EXISTS rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    prize_value DECIMAL(10,2) DEFAULT 0,
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS reward_events (
    reward_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    quantity INT DEFAULT 0,
    today_quantity INT DEFAULT 0,
    probability DOUBLE DEFAULT 0.0,
    version BIGINT DEFAULT 0,
    PRIMARY KEY (reward_id, event_id, region_id),
    FOREIGN KEY (reward_id) REFERENCES rewards(id),
    FOREIGN KEY (event_id, region_id) REFERENCES event_locations(event_id, region_id)
);

CREATE TABLE IF NOT EXISTS golden_hours (
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
    max_rewards INT,
    claimed_rewards INT DEFAULT 0,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_id, region_id) REFERENCES event_locations(event_id, region_id)
);

CREATE TABLE IF NOT EXISTS spin_histories (
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
    multiplier DOUBLE DEFAULT 1.0,
    server_seed VARCHAR(255),
    client_seed VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (participant_id, participant_event_id, participant_region_id) REFERENCES participant_events(participant_id, event_id, region_id),
    FOREIGN KEY (reward_id, reward_event_id, reward_region_id) REFERENCES reward_events(reward_id, event_id, region_id),
    FOREIGN KEY (golden_hour_id) REFERENCES golden_hours(id)
);

-- Roles and permissions
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    role_type VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    display_order INT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    permission_type VARCHAR(50),
    description VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    full_name VARCHAR(255),
    role_id BIGINT,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE IF NOT EXISTS blacklisted_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    token TEXT NOT NULL,
    token_type VARCHAR(50) NOT NULL,
    expiration_time TIMESTAMP NOT NULL,
    user_id BIGINT,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS configurations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(255),
    data_type VARCHAR(50),
    validation_regex VARCHAR(255),
    modifiable BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    object_type VARCHAR(100) NOT NULL,
    object_id VARCHAR(100) NOT NULL,
    property_path VARCHAR(255),
    old_value TEXT,
    new_value TEXT,
    value_type VARCHAR(100),
    update_time TIMESTAMP NOT NULL,
    context VARCHAR(255),
    action_type VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_region_code ON regions(code);
CREATE INDEX IF NOT EXISTS idx_province_code ON provinces(code);
CREATE INDEX IF NOT EXISTS idx_event_code ON events(code);
CREATE INDEX IF NOT EXISTS idx_participant_code ON participants(code);
CREATE INDEX IF NOT EXISTS idx_reward_code ON rewards(code);
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_config_key ON configurations(config_key);
CREATE INDEX IF NOT EXISTS idx_participants_province ON participants(province_id);
CREATE INDEX IF NOT EXISTS idx_participant_events_participant ON participant_events(participant_id);
CREATE INDEX IF NOT EXISTS idx_spin_histories_participant ON spin_histories(participant_id);
CREATE INDEX IF NOT EXISTS idx_spin_histories_reward ON spin_histories(reward_id);
CREATE INDEX IF NOT EXISTS idx_blacklisted_tokens_user ON blacklisted_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_location_event ON event_locations(event_id);
CREATE INDEX IF NOT EXISTS idx_location_region ON event_locations(region_id);
CREATE INDEX IF NOT EXISTS idx_location_status ON event_locations(status);
