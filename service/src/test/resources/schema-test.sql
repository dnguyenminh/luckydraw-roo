-- Drop all tables with CASCADE to handle foreign key dependencies
DROP ALL OBJECTS;

-- Create regions table
CREATE TABLE regions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE
);

-- Create provinces table
CREATE TABLE provinces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    region_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (region_id) REFERENCES regions(id)
);

-- Create events table
CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE
);

-- Create event_locations table
CREATE TABLE event_locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    address VARCHAR(255),
    max_spin INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (event_id) REFERENCES events(id),
    FOREIGN KEY (region_id) REFERENCES regions(id)
);

-- Create rewards table
CREATE TABLE rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    event_location_id BIGINT NOT NULL,
    reward_value DECIMAL(19,2) NOT NULL DEFAULT 0,
    quantity INTEGER NOT NULL DEFAULT 0,
    win_probability DECIMAL(5,4) NOT NULL DEFAULT 0,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (event_location_id) REFERENCES event_locations(id)
);

-- Create golden_hours table
CREATE TABLE golden_hours (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    event_location_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    multiplier DECIMAL(5,2) NOT NULL DEFAULT 1.0,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (event_location_id) REFERENCES event_locations(id)
);

-- Create participants table
CREATE TABLE participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    address VARCHAR(255),
    province_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    checked_in BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (province_id) REFERENCES provinces(id)
);

-- Create participant_events table
CREATE TABLE participant_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    event_id BIGINT NOT NULL,
    event_location_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    spins_remaining INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (event_id) REFERENCES events(id),
    FOREIGN KEY (event_location_id) REFERENCES event_locations(id),
    FOREIGN KEY (participant_id) REFERENCES participants(id)
);

-- Create spin_histories table
CREATE TABLE spin_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    participant_event_id BIGINT NOT NULL,
    reward_id BIGINT,
    golden_hour_id BIGINT,
    spin_time TIMESTAMP NOT NULL,
    win BOOLEAN NOT NULL DEFAULT FALSE,
    multiplier DECIMAL(5,2) NOT NULL DEFAULT 1.0,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (participant_event_id) REFERENCES participant_events(id),
    FOREIGN KEY (reward_id) REFERENCES rewards(id),
    FOREIGN KEY (golden_hour_id) REFERENCES golden_hours(id)
);

-- Create roles table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    role_type VARCHAR(50),
    display_order INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE
);

-- Create permissions table
CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE
);

-- Create users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_expired BOOLEAN NOT NULL DEFAULT FALSE,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    credentials_expired BOOLEAN NOT NULL DEFAULT FALSE,
    role VARCHAR(50),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE
);

-- Create user_roles junction table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Create role_permissions junction table
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

-- Create audit_log table
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(50) NOT NULL,
    entity VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    details TEXT,
    timestamp TIMESTAMP NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create configuration table
CREATE TABLE configurations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

-- Create blacklisted_token table
CREATE TABLE blacklisted_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token TEXT NOT NULL,
    token_type VARCHAR(50) DEFAULT 'ACCESS',
    expiration_time TIMESTAMP NOT NULL,
    user_id BIGINT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create sequences
CREATE SEQUENCE IF NOT EXISTS region_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS province_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS users_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS role_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS permission_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS event_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS event_location_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS golden_hour_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS reward_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS participant_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS participant_event_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS spin_history_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS audit_log_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS configuration_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS blacklisted_token_id_seq START WITH 1 INCREMENT BY 1;
