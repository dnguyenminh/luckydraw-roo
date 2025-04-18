-- H2-specific schema file that's compatible with PostgreSQL syntax but works with H2

-- Drop all indexes first - using IF EXISTS to prevent errors
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

-- Drop all tables - use CASCADE to handle dependencies
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
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS configurations CASCADE;

-- Create tables with H2-compatible syntax
-- Using GENERATED BY DEFAULT AS IDENTITY for H2
-- Adding ON DELETE CASCADE to foreign keys for easier cleanup in tests

CREATE TABLE regions (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE provinces (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    region_id BIGINT NOT NULL,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
);

CREATE TABLE events (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE event_locations (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    status VARCHAR(50),
    max_spin INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
);

CREATE TABLE rewards (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    event_location_id BIGINT NOT NULL,
    "value" DECIMAL(19,2) NOT NULL DEFAULT 0,
    quantity INTEGER NOT NULL DEFAULT 0,
    win_probability DECIMAL(5,4) NOT NULL DEFAULT 0,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (event_location_id) REFERENCES event_locations(id) ON DELETE CASCADE
);

CREATE TABLE golden_hours (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    event_location_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    multiplier DECIMAL(5,2) NOT NULL DEFAULT 1.0,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (event_location_id) REFERENCES event_locations(id) ON DELETE CASCADE
);

CREATE TABLE participants (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    province_id BIGINT NOT NULL,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (province_id) REFERENCES provinces(id) ON DELETE CASCADE
);

CREATE TABLE participant_events (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    event_id BIGINT NOT NULL,
    event_location_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    spins_remaining INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (event_location_id) REFERENCES event_locations(id) ON DELETE CASCADE,
    FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE
);

CREATE TABLE spin_histories (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    participant_event_id BIGINT NOT NULL,
    reward_id BIGINT,
    golden_hour_id BIGINT,
    spin_time TIMESTAMP NOT NULL,
    win BOOLEAN NOT NULL DEFAULT FALSE,
    multiplier DECIMAL(5,2) NOT NULL DEFAULT 1.0,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (participant_event_id) REFERENCES participant_events(id) ON DELETE CASCADE,
    FOREIGN KEY (reward_id) REFERENCES rewards(id) ON DELETE CASCADE,
    FOREIGN KEY (golden_hour_id) REFERENCES golden_hours(id) ON DELETE CASCADE
);

CREATE TABLE roles (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    display_order INTEGER NOT NULL DEFAULT 0,
    description TEXT,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_expired BOOLEAN NOT NULL DEFAULT FALSE,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    credentials_expired BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE blacklisted_tokens (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    token TEXT NOT NULL,
    token_type VARCHAR(50) NOT NULL,
    user_id BIGINT,
    expiration_time TIMESTAMP NOT NULL,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY,
    username VARCHAR(100),
    status VARCHAR(50)
);

CREATE TABLE configurations (
    id BIGINT PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE,
    config_value VARCHAR(4000),
    status VARCHAR(50)
);

-- Create all indexes
CREATE INDEX idx_region_code ON regions(code);
CREATE INDEX idx_region_status ON regions(status);

CREATE INDEX idx_province_code ON provinces(code);
CREATE INDEX idx_province_region ON provinces(region_id);
CREATE INDEX idx_province_status ON provinces(status);

CREATE INDEX idx_event_code ON events(code);
CREATE INDEX idx_event_status ON events(status);
CREATE INDEX idx_event_dates ON events(start_time, end_time);

CREATE INDEX idx_location_code ON event_locations(code);
CREATE INDEX idx_location_event ON event_locations(event_id);
CREATE INDEX idx_location_region ON event_locations(region_id);
CREATE INDEX idx_location_status ON event_locations(status);

CREATE INDEX idx_reward_code ON rewards(code);
CREATE INDEX idx_reward_location ON rewards(event_location_id);
CREATE INDEX idx_reward_status ON rewards(status);

CREATE INDEX idx_golden_hour_location ON golden_hours(event_location_id);
CREATE INDEX idx_golden_hour_time ON golden_hours(start_time, end_time);
CREATE INDEX idx_golden_hour_status ON golden_hours(status);

CREATE INDEX idx_participant_code ON participants(code);
CREATE INDEX idx_participant_province ON participants(province_id);
CREATE INDEX idx_participants_status ON participants(status);

CREATE INDEX idx_participant_event ON participant_events(event_id);
CREATE INDEX idx_participant_location ON participant_events(event_location_id);
CREATE INDEX idx_participant ON participant_events(participant_id);
CREATE INDEX idx_participant_events_status ON participant_events(status);

CREATE INDEX idx_spin_participant ON spin_histories(participant_event_id);
CREATE INDEX idx_spin_reward ON spin_histories(reward_id);
CREATE INDEX idx_spin_golden_hour ON spin_histories(golden_hour_id);
CREATE INDEX idx_spin_time ON spin_histories(spin_time);
CREATE INDEX idx_spin_status ON spin_histories(status);

CREATE INDEX idx_role_name ON roles(role_name);
CREATE INDEX idx_role_status ON roles(status);

CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_status ON users(status);

CREATE INDEX idx_user_role_user ON user_roles(user_id);
CREATE INDEX idx_user_role_role ON user_roles(role_id);

CREATE INDEX idx_blacklisted_token_user ON blacklisted_tokens(user_id);
CREATE INDEX idx_blacklisted_token_type ON blacklisted_tokens(token_type);
CREATE INDEX idx_blacklisted_token_status ON blacklisted_tokens(status);
