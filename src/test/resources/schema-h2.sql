-- H2 Schema for development environment

-- Drop all tables if they exist to ensure a clean start
DROP TABLE IF EXISTS blacklisted_tokens;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS spin_histories;
DROP TABLE IF EXISTS participant_events;
DROP TABLE IF EXISTS participants;
DROP TABLE IF EXISTS rewards;
DROP TABLE IF EXISTS golden_hours;
DROP TABLE IF EXISTS event_locations;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS provinces;
DROP TABLE IF EXISTS regions;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS configurations;

-- Create users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    account_expired BOOLEAN DEFAULT FALSE,
    account_locked BOOLEAN DEFAULT FALSE,
    credentials_expired BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

-- Create region table
CREATE TABLE regions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

-- Create province table
CREATE TABLE provinces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    region_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (region_id) REFERENCES regions(id)
);

-- Create event table
CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

-- Create role table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    display_order INTEGER,
    role_type VARCHAR(50),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

-- Create user_roles junction table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Create permission table
CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

-- Create role_permissions junction table
CREATE TABLE role_permissions (
    permission_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (permission_id, role_id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Create event_locations table
CREATE TABLE event_locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    province_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (province_id) REFERENCES provinces(id),
    FOREIGN KEY (event_id) REFERENCES events(id)
);

-- Create golden_hours table
CREATE TABLE golden_hours (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    multiplier DECIMAL(5,2) DEFAULT 1.0,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES events(id)
);

-- Create rewards table
CREATE TABLE rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    quantity INTEGER NOT NULL,
    remaining INTEGER NOT NULL,
    probability DECIMAL(5,4),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES events(id)
);

-- Create participants table
CREATE TABLE participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

-- Create participant_events junction table
CREATE TABLE participant_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    participant_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    registration_date TIMESTAMP NOT NULL,
    attendance_date TIMESTAMP,
    status VARCHAR(50) DEFAULT 'REGISTERED',
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (participant_id) REFERENCES participants(id),
    FOREIGN KEY (event_id) REFERENCES events(id)
);

-- Create spin_histories table
CREATE TABLE spin_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    participant_event_id BIGINT NOT NULL,
    reward_id BIGINT,
    spin_time TIMESTAMP NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (participant_event_id) REFERENCES participant_events(id),
    FOREIGN KEY (reward_id) REFERENCES rewards(id)
);

-- Create audit_logs table
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

-- Create configurations table
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

-- Create blacklisted_tokens table
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
