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
DROP INDEX IF EXISTS idx_region_province_region;
DROP INDEX IF EXISTS idx_region_province_province;

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
DROP TABLE IF EXISTS region_province CASCADE;

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
    -- Removed region_id foreign key
);

-- Add many-to-many relationship table between regions and provinces
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
                        description VARCHAR(1024),
                        start_time TIMESTAMP NOT NULL,
                        end_time TIMESTAMP NOT NULL,
                        version BIGINT DEFAULT 0
);

-- Modified event_locations with compound primary key
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
                                 code VARCHAR(50),
                                 description VARCHAR(255),
                                 max_spin INT DEFAULT 0,
                                 quantity INT DEFAULT 0,
                                 win_probability DECIMAL(5,2) DEFAULT 0,
                                 daily_spin_dist_rate DOUBLE DEFAULT 0,
                                 remaining_today_spin DOUBLE DEFAULT 0,
                                 version BIGINT DEFAULT 0,
                                 PRIMARY KEY (event_id, region_id),
                                 FOREIGN KEY (event_id) REFERENCES events(id),
                                 FOREIGN KEY (region_id) REFERENCES regions(id),
                                 FOREIGN KEY (province_id) REFERENCES provinces(id)
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
                              province_id BIGINT,
                              version BIGINT DEFAULT 0,
                              FOREIGN KEY (province_id) REFERENCES provinces(id)
);

-- Create participant_events table with compound key structure
CREATE TABLE IF NOT EXISTS participant_events (
                                    event_id BIGINT NOT NULL,
                                    region_id BIGINT NOT NULL,
                                    participant_id BIGINT NOT NULL,
                                    created_by VARCHAR(255),
                                    created_at TIMESTAMP,
                                    updated_by VARCHAR(255),
                                    updated_at TIMESTAMP,
                                    status VARCHAR(50) NOT NULL,
                                    spins_remaining INT DEFAULT 0,
                                    version BIGINT DEFAULT 0,
                                    PRIMARY KEY (event_id, region_id, participant_id),
                                    FOREIGN KEY (event_id, region_id) REFERENCES event_locations(event_id, region_id),
                                    FOREIGN KEY (participant_id) REFERENCES participants(id)
);

-- Update rewards table to reference the compound key of event_locations
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
                         probability DECIMAL(5,4) DEFAULT 0,
                         quantity INT DEFAULT 0,
                         event_id BIGINT NOT NULL,
                         region_id BIGINT NOT NULL,
                         version BIGINT DEFAULT 0,
                         FOREIGN KEY (event_id, region_id) REFERENCES event_locations(event_id, region_id)
);

-- Update golden_hours table to reference the compound key of event_locations
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
                              version BIGINT DEFAULT 0,
                              FOREIGN KEY (event_id, region_id) REFERENCES event_locations(event_id, region_id)
);

-- Update spin_histories table to reference compound keys
CREATE TABLE IF NOT EXISTS spin_histories (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                created_by VARCHAR(255),
                                created_at TIMESTAMP,
                                updated_by VARCHAR(255),
                                updated_at TIMESTAMP,
                                status VARCHAR(50) NOT NULL,
                                event_id BIGINT NOT NULL,
                                region_id BIGINT NOT NULL,
                                participant_id BIGINT NOT NULL,
                                spin_time TIMESTAMP NOT NULL,
                                reward_id BIGINT,
                                reward_event_id BIGINT,
                                reward_region_id BIGINT,
                                win BOOLEAN DEFAULT false,
                                version BIGINT DEFAULT 0,
                                FOREIGN KEY (event_id, region_id, participant_id) REFERENCES participant_events(event_id, region_id, participant_id),
                                FOREIGN KEY (reward_id) REFERENCES rewards(id)
);

-- Audit logging table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    object_type VARCHAR(100),
    object_id VARCHAR(100),
    property_path VARCHAR(255),
    old_value CLOB,
    new_value CLOB,
    value_type VARCHAR(100),
    update_time TIMESTAMP,
    context VARCHAR(255),
    action_type VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- User management tables
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
    type VARCHAR(50), -- Changed from 'permission_type' to 'type'
    description VARCHAR(255),
    version BIGINT DEFAULT 0
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
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE IF NOT EXISTS role_permissions (
                                  role_id BIGINT NOT NULL,
                                  permission_id BIGINT NOT NULL,
                                  PRIMARY KEY (role_id, permission_id),
                                  FOREIGN KEY (role_id) REFERENCES roles(id),
                                  FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

CREATE TABLE IF NOT EXISTS user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id),
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
                                    token_type VARCHAR(20) NOT NULL,
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
                                config_key VARCHAR(255) NOT NULL UNIQUE,
                                config_value VARCHAR(1024) NOT NULL,
                                description VARCHAR(255),
                                version BIGINT DEFAULT 0
);

-- Create indexes for better performance
CREATE INDEX idx_region_province_region ON region_province(region_id);
CREATE INDEX idx_region_province_province ON region_province(province_id);
CREATE INDEX idx_event_location_province ON event_locations(province_id);

-- Replace the old participant_event_id index with indexes on the compound key columns
CREATE INDEX idx_spin_history_event ON spin_histories(event_id);
CREATE INDEX idx_spin_history_region ON spin_histories(region_id);
CREATE INDEX idx_spin_history_participant ON spin_histories(participant_id);
CREATE INDEX idx_spin_history_reward ON spin_histories(reward_id);
