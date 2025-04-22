-- Database setup script for repository tests

-- Set H2 database mode to MySQL to mimic production environment
SET MODE MySQL;

-- Set database options
SET IGNORECASE=TRUE;
SET DB_CLOSE_DELAY=-1;

-- Drop database if it exists
DROP DATABASE IF EXISTS test_lucky_draw;

-- Create database
CREATE DATABASE test_lucky_draw;

-- Create schema
CREATE SCHEMA IF NOT EXISTS test_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE test_lucky_draw TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA test_db TO test_user;

-- Drop and recreate schema if needed
-- DROP SCHEMA IF EXISTS public;
-- CREATE SCHEMA IF NOT EXISTS public;

-- Run schema creation script
RUNSCRIPT FROM 'classpath:schema-h2.sql';

-- Create event_locations table with direct ID instead of composite key
DROP TABLE IF EXISTS event_locations;

CREATE TABLE event_locations (
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
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_event_locations_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_event_locations_region FOREIGN KEY (region_id) REFERENCES regions(id)
);

-- Load test data
RUNSCRIPT FROM 'classpath:data-h2.sql';

-- Check the tables created
SHOW TABLES;

-- Display version information
SELECT H2VERSION() AS version;

-- Some basic validation counts to verify data is loaded correctly
SELECT COUNT(*) AS region_count FROM regions;
SELECT COUNT(*) AS province_count FROM provinces;
SELECT COUNT(*) AS event_count FROM events;
SELECT COUNT(*) AS event_location_count FROM event_locations;
SELECT COUNT(*) AS participant_count FROM participants;
SELECT COUNT(*) AS reward_count FROM rewards;
SELECT COUNT(*) AS user_count FROM users;
SELECT COUNT(*) AS role_count FROM roles;
