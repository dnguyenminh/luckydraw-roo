-- Drop database if it exists
DROP DATABASE IF EXISTS test_lucky_draw;

-- Create database
CREATE DATABASE test_lucky_draw;

-- Create schema
CREATE SCHEMA IF NOT EXISTS test_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE test_lucky_draw TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA test_db TO test_user;
