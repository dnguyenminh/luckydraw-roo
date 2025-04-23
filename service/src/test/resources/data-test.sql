-- Test data for service module tests
-- First, clear existing data to prevent primary key violations

-- Truncate tables in reverse dependency order
DELETE FROM SPIN_HISTORIES;
DELETE FROM GOLDEN_HOURS;
DELETE FROM REWARD_EVENTS;
DELETE FROM REWARDS;
DELETE FROM PARTICIPANT_EVENTS;
DELETE FROM PARTICIPANTS;
DELETE FROM EVENT_LOCATIONS;
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

-- Now insert fresh test data using uppercase table names

-- Test data for regions
INSERT INTO REGIONS (id, version, code, name, status, created_by, created_at, updated_by, updated_at)
VALUES 
(1, 0, 'REGION001', 'North Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2, 0, 'REGION002', 'South Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(3, 0, 'REGION003', 'East Region', 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Test data for events
INSERT INTO EVENTS (id, version, code, name, description, start_time, end_time, status, created_by, created_at, updated_by, updated_at)
VALUES 
(1, 0, 'EVENT001', 'Test Event 1', 'Test Description 1', DATEADD('HOUR', -1, CURRENT_TIMESTAMP), DATEADD('HOUR', 23, CURRENT_TIMESTAMP), 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2, 0, 'EVENT002', 'Test Event 2', 'Test Description 2', DATEADD('HOUR', -2, CURRENT_TIMESTAMP), DATEADD('HOUR', 22, CURRENT_TIMESTAMP), 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(3, 0, 'EVENT003', 'Test Event 3', 'Test Description 3', DATEADD('HOUR', 1, CURRENT_TIMESTAMP), DATEADD('HOUR', 25, CURRENT_TIMESTAMP), 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(4, 0, 'EVENT004', 'Test Event 4', 'Test Description 4', DATEADD('DAY', -1, CURRENT_TIMESTAMP), DATEADD('DAY', 6, CURRENT_TIMESTAMP), 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Test data for provinces
INSERT INTO PROVINCES (id, version, code, name, description, region_id, status, created_by, created_at, updated_by, updated_at)
VALUES 
(1, 0, 'PROVINCE001', 'Test Province 1', 'First test province', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2, 0, 'PROVINCE002', 'Test Province 2', 'Second test province', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Test data for event_locations with composite key
INSERT INTO EVENT_LOCATIONS (event_id, region_id, description, max_spin, today_spin, daily_spin_distributing_rate, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 1, 'Event 1 in Region 1', 100, 50, 0.1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(1, 2, 'Event 1 in Region 2', 150, 75, 0.1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 1, 'Event 2 in Region 1', 200, 100, 0.2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 1, 'Event 3 in Region 1', 100, 50, 0.1, 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for participants
INSERT INTO PARTICIPANTS (id, version, code, name, phone, address, last_adding_spin, province_id, status, created_by, created_at, updated_by, updated_at)
VALUES 
(1, 0, 'PART001', 'John Doe', '1234567890', '123 Main St', 0, 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2, 0, 'PART002', 'Jane Smith', '2345678901', '456 Oak Ave', 0, 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(3, 0, 'PART003', 'Bob Johnson', '3456789012', '789 Pine Rd', 0, 2, 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(4, 0, 'PART004', 'Alice Brown', '4567890123', '101 Elm St', 0, 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Test data for roles
INSERT INTO ROLES (id, role_type, description, display_order, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'ROLE_ADMIN', 'Administrator', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'ROLE_USER', 'Standard User', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for permissions
INSERT INTO PERMISSIONS (id, name, type, description, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'CREATE_USER', 'WRITE', 'Create user accounts', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'READ_USER', 'READ', 'View user accounts', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'UPDATE_EVENT', 'WRITE', 'Update events', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for users (removed 'enabled' column that doesn't exist in schema)
INSERT INTO USERS (id, username, password, email, full_name, role_id, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'admin', '$2a$10$rQnT.Gx6lI5Sn45XRu0SsO0jz5vBNl72FXkbDI3TTVQfHK4mFjESq', 'admin@test.com', 'Admin User', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'user', '$2a$10$rQnT.Gx6lI5Sn45XRu0SsO0jz5vBNl72FXkbDI3TTVQfHK4mFjESq', 'user@test.com', 'Regular User', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for role permissions
INSERT INTO ROLE_PERMISSIONS (role_id, permission_id)
VALUES
(1, 1), (1, 2), (1, 3), -- Admin has all permissions
(2, 2); -- Regular user can only view reports

-- Test data for participant_events with composite key
INSERT INTO PARTICIPANT_EVENTS (participant_id, event_id, region_id, spins_remaining, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 1, 1, 5, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 1, 2, 3, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 2, 1, 0, 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 2, 1, 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for rewards
INSERT INTO REWARDS (id, name, code, description, prize_value, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 'Gold Prize', 'GOLD', 'Gold prize description', 1000.00, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'Silver Prize', 'SILVER', 'Silver prize description', 500.00, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'Bronze Prize', 'BRONZE', 'Bronze prize description', 250.00, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for reward_events with composite key
INSERT INTO REWARD_EVENTS (event_id, region_id, reward_id, quantity, today_quantity, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 1, 1, 10, 5, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(1, 2, 2, 20, 10, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 1, 3, 15, 7, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for golden_hours
INSERT INTO GOLDEN_HOURS (id, event_id, region_id, start_time, end_time, multiplier, max_rewards, claimed_rewards, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 1, 1, DATEADD('HOUR', 1, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, CURRENT_TIMESTAMP), 2.0, 20, 0, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 1, 2, DATEADD('HOUR', 2, CURRENT_TIMESTAMP), DATEADD('HOUR', 4, CURRENT_TIMESTAMP), 1.5, 15, 0, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for spin_histories
INSERT INTO SPIN_HISTORIES (id, participant_id, event_id, region_id, spin_time, reward_id, reward_event_id, reward_region_id, win, wheel_position, multiplier, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 1, 1, 1, DATEADD('MINUTE', -30, CURRENT_TIMESTAMP), 1, 1, 1, TRUE, 120.5, 2.0, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 2, 1, 2, DATEADD('MINUTE', -20, CURRENT_TIMESTAMP), NULL, NULL, NULL, FALSE, 45.2, 1.0, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 4, 2, 1, DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), 3, 2, 1, TRUE, 230.7, 1.0, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for blacklisted_tokens
INSERT INTO BLACKLISTED_TOKENS (id, token, token_type, expiration_time, user_id, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMSJ9', 'ACCESS', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMiJ9', 'REFRESH', DATEADD('DAY', -2, CURRENT_TIMESTAMP), 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for configurations
INSERT INTO CONFIGURATIONS (id, config_key, config_value, description, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 'MAX_DAILY_SPINS', '5', 'Maximum spins per day', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'GOLDEN_HOUR_MULTIPLIER', '2', 'Default golden hour multiplier', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for audit_logs
INSERT INTO AUDIT_LOGS (id, object_type, object_id, action_type, update_time, context, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 'USER', '1', 'CREATE', CURRENT_TIMESTAMP, 'User Registration', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'EVENT', '1', 'UPDATE', CURRENT_TIMESTAMP, 'Event Modification', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Reset sequence values using H2-compatible syntax
ALTER SEQUENCE IF EXISTS EVENTS_ID_SEQ RESTART WITH (SELECT COALESCE(MAX(id) + 1, 1) FROM EVENTS);
ALTER SEQUENCE IF EXISTS REGIONS_ID_SEQ RESTART WITH (SELECT COALESCE(MAX(id) + 1, 1) FROM REGIONS);
ALTER SEQUENCE IF EXISTS PROVINCES_ID_SEQ RESTART WITH (SELECT COALESCE(MAX(id) + 1, 1) FROM PROVINCES);
ALTER SEQUENCE IF EXISTS PARTICIPANTS_ID_SEQ RESTART WITH (SELECT COALESCE(MAX(id) + 1, 1) FROM PARTICIPANTS);
ALTER SEQUENCE IF EXISTS REWARDS_ID_SEQ RESTART WITH (SELECT COALESCE(MAX(id) + 1, 1) FROM REWARDS);
ALTER SEQUENCE IF EXISTS GOLDEN_HOURS_ID_SEQ RESTART WITH (SELECT COALESCE(MAX(id) + 1, 1) FROM GOLDEN_HOURS);
ALTER SEQUENCE IF EXISTS SPIN_HISTORIES_ID_SEQ RESTART WITH (SELECT COALESCE(MAX(id) + 1, 1) FROM SPIN_HISTORIES);
