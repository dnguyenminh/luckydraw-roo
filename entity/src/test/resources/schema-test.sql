-- Drop tables in correct order
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
    name VARCHAR(255) NOT NULL,
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
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS region_province (
    province_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    PRIMARY KEY (province_id, region_id),
    FOREIGN KEY (province_id) REFERENCES provinces(id),
    FOREIGN KEY (region_id) REFERENCES regions(id)
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
    description VARCHAR(255),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS event_locations (
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    province_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    max_spin INT NOT NULL DEFAULT 100,
    quantity INT CHECK (quantity >= 0),
    win_probability DECIMAL(5,4),
    daily_spin_dist_rate DOUBLE DEFAULT 0,
    remaining_today_spin DOUBLE DEFAULT 0,
    version BIGINT DEFAULT 0,
    PRIMARY KEY (event_id, region_id),
    FOREIGN KEY (event_id) REFERENCES events(id),
    FOREIGN KEY (region_id) REFERENCES regions(id),
    FOREIGN KEY (province_id) REFERENCES provinces(id)
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

CREATE TABLE IF NOT EXISTS rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    prize_value DECIMAL(10,2) DEFAULT 0,
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_id, region_id) REFERENCES event_locations(event_id, region_id)
);

CREATE TABLE IF NOT EXISTS reward_events (
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    reward_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    today_quantity INT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    PRIMARY KEY (event_id, region_id, reward_id),
    FOREIGN KEY (event_id, region_id) REFERENCES event_locations(event_id, region_id),
    FOREIGN KEY (reward_id) REFERENCES rewards(id)
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
    address VARCHAR(255),
    last_adding_spin INT DEFAULT 0,
    province_id BIGINT NOT NULL,
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
    FOREIGN KEY (event_id, region_id) REFERENCES event_locations(event_id, region_id),
    FOREIGN KEY (participant_id) REFERENCES participants(id)
);

CREATE TABLE IF NOT EXISTS spin_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    spin_time TIMESTAMP NOT NULL,
    event_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    participant_event_id BIGINT NOT NULL,
    participant_region_id BIGINT NOT NULL,
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
    FOREIGN KEY (event_id) REFERENCES events(id),
    FOREIGN KEY (participant_id, participant_event_id, participant_region_id) 
        REFERENCES participant_events(participant_id, event_id, region_id),
    FOREIGN KEY (reward_id, reward_event_id, reward_region_id) 
        REFERENCES reward_events(reward_id, event_id, region_id),
    FOREIGN KEY (golden_hour_id) REFERENCES golden_hours(id)
);

-- Create indexes to exactly match entity's @Table annotation
CREATE INDEX IF NOT EXISTS idx_spin_participant_event ON spin_histories(participant_id, participant_region_id, participant_event_id);
CREATE INDEX IF NOT EXISTS idx_spin_reward_event ON spin_histories(reward_id, reward_region_id, reward_event_id);
CREATE INDEX IF NOT EXISTS idx_spin_reward ON spin_histories(reward_id);
CREATE INDEX IF NOT EXISTS idx_spin_golden_hour ON spin_histories(golden_hour_id);
CREATE INDEX IF NOT EXISTS idx_spin_time ON spin_histories(spin_time);
CREATE INDEX IF NOT EXISTS idx_spin_status ON spin_histories(status);
CREATE INDEX IF NOT EXISTS idx_spin_event ON spin_histories(event_id);

-- Create indexes for golden hours
CREATE INDEX IF NOT EXISTS idx_golden_hour_time ON golden_hours(start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_golden_hour_location ON golden_hours(event_id, region_id);

-- Create necessary tables for authentication and authorization
CREATE TABLE IF NOT EXISTS roles (
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

CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50),
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
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    full_name VARCHAR(255),
    role_id BIGINT,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE IF NOT EXISTS configurations (
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

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
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
