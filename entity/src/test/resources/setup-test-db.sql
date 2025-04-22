-- Database setup script for entity tests

-- Drop database if it exists
DROP DATABASE IF EXISTS test_lucky_draw;

-- Create database
CREATE DATABASE test_lucky_draw;

-- Create schema
CREATE SCHEMA IF NOT EXISTS test_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE test_lucky_draw TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA test_db TO test_user;

-- Set H2 database mode to MySQL to mimic production environment
SET MODE MySQL;

-- Set database options
SET IGNORECASE=TRUE;
SET DB_CLOSE_DELAY=-1;

-- Run schema creation script
RUNSCRIPT FROM 'classpath:schema-h2.sql';

-- Load test data (minimal set for entity tests)
RUNSCRIPT FROM 'classpath:data-test.sql';

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
