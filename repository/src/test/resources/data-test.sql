-- Test data for repository module tests
-- First, clear existing data to prevent primary key violations

-- Truncate tables in reverse dependency order
DELETE FROM SPIN_HISTORIES;
DELETE FROM GOLDEN_HOURS;
-- Remove reference to non-existent REWARD_EVENTS table
DELETE FROM REWARDS;
DELETE FROM PARTICIPANT_EVENTS;
DELETE FROM PARTICIPANTS;
DELETE FROM EVENT_LOCATIONS;
DELETE FROM REGION_PROVINCE;
DELETE FROM PROVINCES;
DELETE FROM EVENTS;
DELETE FROM REGIONS;
DELETE FROM ROLE_PERMISSIONS;
DELETE FROM BLACKLISTED_TOKENS;
DELETE FROM USERS;
DELETE FROM PERMISSIONS;
DELETE FROM ROLES;
DELETE FROM CONFIGURATIONS;
DELETE FROM AUDIT_LOGS;

-- Test data for regions
INSERT INTO REGIONS (id, version, code, name, status, created_by, created_at, updated_by, updated_at)
VALUES 
(1, 0, 'NORTH', 'Northern Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2, 0, 'SOUTH', 'Southern Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(3, 0, 'CENTRAL', 'Central Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Test data for provinces
INSERT INTO PROVINCES (id, version, code, name, description, region_id, status, created_by, created_at, updated_by, updated_at)
VALUES 
(1, 0, 'N01', 'Northern Province 1', 'First province in north', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2, 0, 'S01', 'Southern Province 1', 'First province in south', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(3, 0, 'C01', 'Central Province 1', 'First province in central', 3, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Test data for events
INSERT INTO EVENTS (id, version, code, name, description, start_time, end_time, status, created_by, created_at, updated_by, updated_at)
VALUES 
(1, 0, 'SUMMER2023', 'Summer Lucky Draw 2023', 'Summer promotional event with prizes', '2023-06-01 00:00:00', '2023-08-31 23:59:59', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2, 0, 'FALL2023', 'Fall Lucky Draw 2023', 'Fall promotional event with prizes', '2023-09-01 00:00:00', '2023-11-30 23:59:59', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Test data for event_locations with composite key
INSERT INTO EVENT_LOCATIONS (event_id, region_id, description, max_spin, today_spin, daily_spin_distributing_rate, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 1, 'Summer event location in the north', 1000, 100, 0.1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(1, 2, 'Summer event location in the south', 800, 80, 0.1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 1, 'Fall event location in the north', 1200, 120, 0.1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 2, 'Fall event location in the south', 900, 90, 0.1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for roles
INSERT INTO ROLES (id, role_type, description, display_order, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 'ROLE_ADMIN', 'Administrator role', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'ROLE_USER', 'User role', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'ROLE_MANAGER', 'Manager role', 3, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for permissions
INSERT INTO PERMISSIONS (id, name, permission_type, description, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'CREATE_USER', 'WRITE', 'Create user accounts', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'READ_USER', 'READ', 'View user accounts', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'UPDATE_EVENT', 'WRITE', 'Update events', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for users (removed 'enabled' column)
INSERT INTO USERS (id, username, password, email, full_name, role_id, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'admin', '$2a$10$aXxPWTOJ8QZfCFY9QZIcv.v9jLcDO88k3HQF1ntaD3z2DTg5hNYT6', 'admin@example.com', 'System Administrator', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'manager', '$2a$10$aXxPWTOJ8QZfCFY9QZIcv.v9jLcDO88k3HQF1ntaD3z2DTg5hNYT6', 'manager@example.com', 'Event Manager', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'staff', '$2a$10$aXxPWTOJ8QZfCFY9QZIcv.v9jLcDO88k3HQF1ntaD3z2DTg5hNYT6', 'staff@example.com', 'Staff Member', 3, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Role-Permission associations
INSERT INTO ROLE_PERMISSIONS (role_id, permission_id)
VALUES
(1, 1), (1, 2), (1, 3), -- Admin has all permissions
(2, 2), (2, 3),         -- Manager has some permissions
(3, 2);                  -- Staff has view reports permission

-- Test data for participants
INSERT INTO PARTICIPANTS (id, version, code, name, phone, address, last_adding_spin, province_id, status, created_by, created_at, updated_by, updated_at)
VALUES 
(1, 0, 'P001', 'John Doe', '1234567890', '123 Main St', 0, 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2, 0, 'P002', 'Jane Smith', '2345678901', '456 Oak Ave', 0, 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(3, 0, 'P003', 'Bob Johnson', '3456789012', '789 Pine Rd', 0, 3, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Test data for participant_events with composite key
INSERT INTO PARTICIPANT_EVENTS (participant_id, event_id, region_id, spins_remaining, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 1, 1, 5, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 1, 1, 3, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 1, 2, 4, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(1, 2, 1, 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for rewards with event_id and region_id fields
INSERT INTO REWARDS (id, name, code, description, prize_value, event_id, region_id, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 'Gold Prize', 'GOLD', 'Gold prize description', 1000.00, 1, 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'Silver Prize', 'SILVER', 'Silver prize description', 500.00, 1, 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'Bronze Prize', 'BRONZE', 'Bronze prize description', 250.00, 2, 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Make sure all golden_hours data has proper compound keys referenced
INSERT INTO GOLDEN_HOURS (id, event_id, region_id, start_time, end_time, multiplier, max_rewards, claimed_rewards, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 1, 1, '2023-06-15 12:00:00', '2023-06-15 14:00:00', 2.0, 50, 0, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 1, 2, '2023-07-01 18:00:00', '2023-07-01 20:00:00', 1.5, 40, 0, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Ensure spin_histories properly reference the compound keys
INSERT INTO SPIN_HISTORIES (id, participant_id, event_id, region_id, spin_time, reward_id, reward_event_id, reward_region_id, win, wheel_position, multiplier, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 1, 1, 1, '2023-06-05 13:30:00', 1, 1, 1, TRUE, 120.5, 1.0, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 1, 1, 1, '2023-06-06 14:45:00', NULL, NULL, NULL, FALSE, 45.2, 1.0, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 2, 1, 1, '2023-06-07 15:20:00', 2, 1, 1, TRUE, 230.7, 1.0, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for configurations
INSERT INTO CONFIGURATIONS (id, config_key, config_value, description, data_type, validation_regex, modifiable, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'DAILY_SPIN_LIMIT', '5', 'Maximum number of spins allowed per day', 'INTEGER', '^[0-9]+$', TRUE, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'MIN_VERIFICATION_AGE', '18', 'Minimum age for identity verification', 'INTEGER', '^[0-9]+$', TRUE, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for audit_logs
INSERT INTO AUDIT_LOGS (id, object_type, object_id, property_path, old_value, new_value, value_type, update_time, context, action_type, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'Event', '1', 'status', 'DRAFT', 'ACTIVE', 'CommonStatus', CURRENT_TIMESTAMP, 'EVENT_ACTIVATION', 'UPDATE', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'Participant', '3', 'status', 'ACTIVE', 'INACTIVE', 'CommonStatus', CURRENT_TIMESTAMP, 'PARTICIPANT_DEACTIVATION', 'UPDATE', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for blacklisted tokens
INSERT INTO BLACKLISTED_TOKENS (id, token, token_type, expiration_time, user_id, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'eyJhbGciOiJIUzI1NiJ9.expired1', 'ACCESS', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'eyJhbGciOiJIUzI1NiJ9.expired2', 'REFRESH', DATEADD('DAY', -2, CURRENT_TIMESTAMP), 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
