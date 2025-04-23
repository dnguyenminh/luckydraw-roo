-- Drop all indexes first
DROP INDEX IF EXISTS idx_participants_status;
DROP INDEX IF EXISTS idx_region_code;
DROP INDEX IF EXISTS idx_region_status;
DROP INDEX IF EXISTS idx_province_code;
DROP INDEX IF EXISTS idx_province_region;
DROP INDEX IF EXISTS idx_province_status;
DROP INDEX IF EXISTS idx_event_code;
DROP INDEX IF EXISTS idx_event_status;
DROP INDEX IF EXISTS idx_event_dates;
DROP INDEX IF EXISTS idx_location_code;
DROP INDEX IF EXISTS idx_location_event;
DROP INDEX IF EXISTS idx_location_region;
DROP INDEX IF EXISTS idx_location_status;
DROP INDEX IF EXISTS idx_reward_code;
DROP INDEX IF EXISTS idx_reward_location;
DROP INDEX IF EXISTS idx_reward_status;
DROP INDEX IF EXISTS idx_golden_hour_location;
DROP INDEX IF EXISTS idx_golden_hour_time;
DROP INDEX IF EXISTS idx_golden_hour_status;
DROP INDEX IF EXISTS idx_participant_code;
DROP INDEX IF EXISTS idx_participant_province;
DROP INDEX IF EXISTS idx_participant_event;
DROP INDEX IF EXISTS idx_participant_location;
DROP INDEX IF EXISTS idx_participant;
DROP INDEX IF EXISTS idx_participant_events_status;
DROP INDEX IF EXISTS idx_spin_participant;
DROP INDEX IF EXISTS idx_spin_reward;
DROP INDEX IF EXISTS idx_spin_golden_hour;
DROP INDEX IF EXISTS idx_spin_time;
DROP INDEX IF EXISTS idx_spin_status;
DROP INDEX IF EXISTS idx_role_name;
DROP INDEX IF EXISTS idx_role_status;
DROP INDEX IF EXISTS idx_user_username;
DROP INDEX IF EXISTS idx_user_email;
DROP INDEX IF EXISTS idx_user_status;
DROP INDEX IF EXISTS idx_user_role_user;
DROP INDEX IF EXISTS idx_user_role_role;
DROP INDEX IF EXISTS idx_blacklisted_token_user;
DROP INDEX IF EXISTS idx_blacklisted_token_type;
DROP INDEX IF EXISTS idx_blacklisted_token_status;

-- Drop all tables
DROP TABLE IF EXISTS spin_histories CASCADE;
DROP TABLE IF EXISTS participant_events CASCADE;
DROP TABLE IF EXISTS event_locations CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS participants CASCADE;
DROP TABLE IF EXISTS provinces CASCADE;
DROP TABLE IF EXISTS regions CASCADE;
DROP TABLE IF EXISTS rewards CASCADE;
DROP TABLE IF EXISTS golden_hours CASCADE;
DROP TABLE IF EXISTS blacklisted_tokens CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

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
    description VARCHAR(1024),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0
);

CREATE TABLE event_locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    description VARCHAR(255),
    max_spin INT DEFAULT 0,
    quantity INT DEFAULT 0,
    win_probability DECIMAL(5,2) DEFAULT 0,
    event_id BIGINT,
    region_id BIGINT,
    daily_spin_dist_rate DOUBLE DEFAULT 0,  -- Original field from entity
    remaining_today_spin DOUBLE DEFAULT 0,  -- Add the missing column that Hibernate expects
    version BIGINT DEFAULT 0,
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
    province_id BIGINT,
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
    event_id BIGINT,
    event_location_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    spins_remaining INT DEFAULT 0,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_location_id) REFERENCES event_locations(id),
    FOREIGN KEY (participant_id) REFERENCES participants(id)
);

CREATE TABLE rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    description VARCHAR(255),
    prizeValue DECIMAL(10,2),
    probability DECIMAL(5,4) DEFAULT 0,
    quantity INT DEFAULT 0,
    event_location_id BIGINT,
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
    multiplier DECIMAL(5,2) NOT NULL,
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
    win BOOLEAN DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (participant_event_id) REFERENCES participant_events(id),
    FOREIGN KEY (reward_id) REFERENCES rewards(id)
);

CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    role_type VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    display_order INT DEFAULT 0,
    version BIGINT DEFAULT 0
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255),
    role_type VARCHAR(50),
    enabled BOOLEAN DEFAULT TRUE,
    account_expired BOOLEAN DEFAULT FALSE,
    account_locked BOOLEAN DEFAULT FALSE,
    credentials_expired BOOLEAN DEFAULT FALSE,
    version BIGINT DEFAULT 0
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE blacklisted_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    token TEXT NOT NULL,
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
    config_key VARCHAR(255) NOT NULL UNIQUE,
    config_value VARCHAR(1024) NOT NULL,
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
    object_type VARCHAR(255) NOT NULL,
    object_id VARCHAR(255) NOT NULL,
    property_path VARCHAR(255),
    old_value TEXT,
    new_value TEXT,
    value_type VARCHAR(255),
    update_time TIMESTAMP NOT NULL,
    context VARCHAR(255),
    action_type VARCHAR(50),
    version BIGINT DEFAULT 0
);
