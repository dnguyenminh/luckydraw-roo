-- Test data for entity module tests
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

-- Insert Regions (minimal required for tests)
INSERT INTO REGIONS (id, created_by, created_at, updated_by, updated_at, status, name, code, description, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'North Region', 'NORTH', 'Northern provinces of the country', 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'South Region', 'SOUTH', 'Southern provinces of the country', 0);

-- Insert Provinces (minimal required for tests) - removed region_id column
INSERT INTO PROVINCES (id, created_by, created_at, updated_by, updated_at, status, name, code, description, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Province A', 'PROV_A', 'First province in North Region', 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Province B', 'PROV_B', 'First province in South Region', 0);

-- Insert many-to-many relationships between regions and provinces
INSERT INTO REGION_PROVINCE (province_id, region_id) VALUES
(1, 1),  -- Province A belongs to North Region
(2, 2);  -- Province B belongs to South Region

-- Insert Events (minimal required for tests)
INSERT INTO EVENTS (id, created_by, created_at, updated_by, updated_at, status, name, code, description, start_time, end_time, version) VALUES
(1, 'admin', '2023-01-15 00:00:00', 'admin', '2023-01-15 00:00:00', 'ACTIVE', 'Summer Festival', 'SUMMER_FEST', 'Annual summer lucky draw event', '2023-06-01 00:00:00', '2023-08-31 23:59:59', 0);

-- Clear existing data for event_locations if needed
DELETE FROM EVENT_LOCATIONS;

-- Insert event locations with compound key and province_id
INSERT INTO EVENT_LOCATIONS (event_id, region_id, province_id, created_by, created_at, updated_by, updated_at, status, name, code, description, max_spin, quantity, win_probability, version)
VALUES
(1, 1, 1, 'system', NOW(), 'system', NOW(), 'ACTIVE', 'Location 1', 'LOC_1', 'Test location 1', 100, 50, 0.2, 0),
(1, 2, 2, 'system', NOW(), 'system', NOW(), 'ACTIVE', 'Location 2', 'LOC_2', 'Test location 2', 200, 100, 0.3, 0),
(2, 1, 1, 'system', NOW(), 'system', NOW(), 'INACTIVE', 'Location 3', 'LOC_3', 'Test location 3', 50, 25, 0.1, 0);

-- Insert Participants (minimal required for tests)
INSERT INTO PARTICIPANTS (id, created_by, created_at, updated_by, updated_at, status, name, code, phone, address, province_id, version) VALUES
(1, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'ACTIVE', 'John Doe', 'JOHN001', '1234567890', '123 Main St', 1, 0);

-- Insert Participant Events with compound key structure
INSERT INTO PARTICIPANT_EVENTS (event_id, region_id, participant_id, created_by, created_at, updated_by, updated_at, status, spins_remaining, version) VALUES
(1, 1, 1, 'system', '2023-06-05 00:00:00', 'system', '2023-06-05 00:00:00', 'ACTIVE', 5, 0);

-- Test data for rewards with event_id and region_id as fields
INSERT INTO REWARDS (id, name, code, description, prize_value, event_id, region_id, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 'Gold Prize', 'GOLD', 'Gold prize description', 1000.00, 1, 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'Silver Prize', 'SILVER', 'Silver prize description', 500.00, 1, 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'Bronze Prize', 'BRONZE', 'Bronze prize description', 250.00, 2, 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert golden_hours with updated event_location reference
INSERT INTO GOLDEN_HOURS (id, created_by, created_at, updated_by, updated_at, status, event_id, region_id, start_time, end_time, multiplier, version) VALUES
(1, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 1, 1, '2023-06-15 12:00:00', '2023-06-15 14:00:00', 2.0, 0);

-- Insert spin_histories with updated foreign key references
INSERT INTO SPIN_HISTORIES (id, created_by, created_at, updated_by, updated_at, status, event_id, region_id, participant_id, spin_time, reward_id, win, version) VALUES
(1, 'system', '2023-06-15 12:30:00', 'system', '2023-06-15 12:30:00', 'ACTIVE', 1, 1, 1, '2023-06-15 12:30:00', 1, true, 0);

-- Insert roles
INSERT INTO ROLES (id, role_type, description, display_order, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 'ROLE_ADMIN', 'Administrator role', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'ROLE_USER', 'User role', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for permissions
INSERT INTO PERMISSIONS (id, name, permission_type, description, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'CREATE_USER', 'WRITE', 'Create user accounts', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'READ_USER', 'READ', 'View user accounts', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'UPDATE_EVENT', 'WRITE', 'Update events', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Role-Permission associations
INSERT INTO ROLE_PERMISSIONS (role_id, permission_id)
VALUES
(1, 1), (1, 2), (1, 3), -- Admin has all permissions
(2, 2);                  -- User has read user permission

-- Insert users
INSERT INTO USERS (id, username, password, email, full_name, role_id, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'admin', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'admin@example.com', 'Admin User', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'user', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'user@example.com', 'Regular User', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert User Roles (minimal required for tests)
INSERT INTO USER_ROLES (user_id, role_id) VALUES
(1, 1),
(2, 2);
