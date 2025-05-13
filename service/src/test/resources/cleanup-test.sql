-- Cleanup script for service tests to ensure a clean state between test runs

-- First drop all dependent tables to avoid foreign key constraint issues
DROP TABLE IF EXISTS spin_histories;
DROP TABLE IF EXISTS golden_hours;
DROP TABLE IF EXISTS reward_events;
DROP TABLE IF EXISTS participant_events;
DROP TABLE IF EXISTS blacklisted_tokens;
DROP TABLE IF EXISTS role_permissions;

-- Then drop main entity tables
DROP TABLE IF EXISTS rewards;
DROP TABLE IF EXISTS participants;
DROP TABLE IF EXISTS event_locations;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS region_province;
DROP TABLE IF EXISTS provinces;
DROP TABLE IF EXISTS regions;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS configurations;
DROP TABLE IF EXISTS audit_logs;

-- Reset sequences if needed
-- H2 will automatically reset sequence values when using DROP TABLE
