-- Create schema for test database with quoted table names to preserve case

-- Users table
CREATE TABLE IF NOT EXISTS "users" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    password VARCHAR(255),
    full_name VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    account_expired BOOLEAN DEFAULT FALSE,
    account_locked BOOLEAN DEFAULT FALSE,
    credentials_expired BOOLEAN DEFAULT FALSE,
    status VARCHAR(50),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    role VARCHAR(50)
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

-- User roles join table
CREATE TABLE IF NOT EXISTS "user_roles" (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES "users"(id),
    FOREIGN KEY (role_id) REFERENCES "roles"(id)
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

-- Event Location table
CREATE TABLE IF NOT EXISTS "event_locations" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    region_id BIGINT,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100),
    address VARCHAR(255),
    max_spin INTEGER DEFAULT 0,
    status VARCHAR(50),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES "events"(id),
    FOREIGN KEY (region_id) REFERENCES "regions"(id)
);

-- Golden Hour table
CREATE TABLE IF NOT EXISTS "golden_hours" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    multiplier DECIMAL(5,2),
    status VARCHAR(50),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES "events"(id)
);

-- Reward table
CREATE TABLE IF NOT EXISTS "rewards" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    quantity INTEGER DEFAULT 0,
    remaining INTEGER DEFAULT 0,
    probability DECIMAL(5,2),
    status VARCHAR(50),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES "events"(id)
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

-- Participant Event table
CREATE TABLE IF NOT EXISTS "participant_events" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    participant_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    registration_date TIMESTAMP,
    attendance_date TIMESTAMP,
    status VARCHAR(50),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (participant_id) REFERENCES "participants"(id),
    FOREIGN KEY (event_id) REFERENCES "events"(id)
);

-- Spin History table
CREATE TABLE IF NOT EXISTS "spin_histories" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    participant_event_id BIGINT NOT NULL,
    reward_id BIGINT,
    spin_time TIMESTAMP,
    status VARCHAR(50),
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (participant_event_id) REFERENCES "participant_events"(id),
    FOREIGN KEY (reward_id) REFERENCES "rewards"(id)
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
