-- Test data for H2 database (entity tests)

-- Clear existing data
DELETE FROM spin_histories;
DELETE FROM golden_hours;
DELETE FROM participant_events;
DELETE FROM participants;
DELETE FROM event_locations;
DELETE FROM region_province;
DELETE FROM provinces;
DELETE FROM events;
DELETE FROM regions;
DELETE FROM role_permissions;
DELETE FROM blacklisted_tokens;
DELETE FROM users;
DELETE FROM permissions;
DELETE FROM roles;
DELETE FROM configurations;
DELETE FROM audit_logs;

-- Clear existing data for event_locations if needed
DELETE FROM event_locations;

-- Insert Regions
INSERT INTO regions (id, created_by, created_at, updated_by, updated_at, status, name, code, description, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'North Region', 'NORTH', 'Northern provinces of the country', 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Central Region', 'CENTRAL', 'Central provinces of the country', 0),
(3, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'South Region', 'SOUTH', 'Southern provinces of the country', 0);

-- Insert Provinces (removed region_id)
INSERT INTO provinces (id, created_by, created_at, updated_by, updated_at, status, name, code, description, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Province A', 'PROV_A', 'First province in North Region', 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Province B', 'PROV_B', 'Second province in North Region', 0),
(3, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Province C', 'PROV_C', 'First province in Central Region', 0);

-- Insert many-to-many relationships between regions and provinces
INSERT INTO region_province (province_id, region_id) VALUES
(1, 1),  -- Province A belongs to North Region
(2, 1),  -- Province B belongs to North Region
(3, 2);  -- Province C belongs to Central Region

-- Insert Events
INSERT INTO events (id, created_by, created_at, updated_by, updated_at, status, name, code, description, start_time, end_time, version) VALUES
(1, 'admin', '2023-01-15 00:00:00', 'admin', '2023-01-15 00:00:00', 'ACTIVE', 'Summer Festival', 'SUMMER_FEST', 'Annual summer lucky draw event', '2023-06-01 00:00:00', '2023-08-31 23:59:59', 0),
(2, 'admin', '2023-01-15 00:00:00', 'admin', '2023-01-15 00:00:00', 'ACTIVE', 'Winter Wonderland', 'WINTER_FEST', 'Winter promotional event', '2023-12-01 00:00:00', '2024-01-31 23:59:59', 0);

-- Insert event locations with compound key and province_id
INSERT INTO event_locations (event_id, region_id, province_id, created_by, created_at, updated_by, updated_at, status, name, code, description, max_spin, quantity, win_probability, version)
VALUES
(1, 1, 1, 'system', NOW(), 'system', NOW(), 'ACTIVE', 'Location 1', 'LOC_1', 'Test location 1', 100, 50, 0.2, 0),
(1, 2, 3, 'system', NOW(), 'system', NOW(), 'ACTIVE', 'Location 2', 'LOC_2', 'Test location 2', 200, 100, 0.3, 0),
(2, 1, 2, 'system', NOW(), 'system', NOW(), 'INACTIVE', 'Location 3', 'LOC_3', 'Test location 3', 50, 25, 0.1, 0);

-- Insert Participants
INSERT INTO participants (id, created_by, created_at, updated_by, updated_at, status, name, code, phone, address, last_adding_spin, province_id, version) VALUES
(1, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'ACTIVE', 'John Doe', 'JOHN001', '1234567890', '123 Main St', 0, 1, 0),
(2, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'ACTIVE', 'Jane Smith', 'JANE001', '2345678901', '456 Oak Ave', 0, 1, 0),
(3, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'ACTIVE', 'Robert Johnson', 'ROBERT001', '3456789012', '789 Pine Rd', 0, 2, 0);

-- Insert Participant Events with compound key structure
INSERT INTO participant_events (event_id, region_id, participant_id, created_by, created_at, updated_by, updated_at, status, spins_remaining, version) VALUES
(1, 1, 1, 'system', '2023-06-05 00:00:00', 'system', '2023-06-05 00:00:00', 'ACTIVE', 5, 0),
(1, 2, 2, 'system', '2023-06-05 00:00:00', 'system', '2023-06-05 00:00:00', 'ACTIVE', 3, 0),
(2, 1, 3, 'system', '2023-06-05 00:00:00', 'system', '2023-06-05 00:00:00', 'ACTIVE', 4, 0);

-- Skip rewards and reward_events completely to avoid conflicts with the test
-- Let the test handle creating rewards and associations

-- Fix the golden_hours insert by adding the missing region_id in the third row
INSERT INTO golden_hours (id, created_by, created_at, updated_by, updated_at, status, event_id, region_id, start_time, end_time, multiplier, version) VALUES
(1, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 1, 1, '2023-06-15 12:00:00', '2023-06-15 14:00:00', 2.0, 0),
(2, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 1, 2, '2023-07-15 18:00:00', '2023-07-15 20:00:00', 2.0, 0),
(3, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 2, 1, '2023-06-20 13:00:00', '2023-06-20 15:00:00', 2.5, 0);

-- Update spin_histories to use NULL for reward_id to avoid associations with rewards that may not exist
INSERT INTO spin_histories (id, created_by, created_at, updated_by, updated_at, status, event_id, region_id, participant_id, spin_time, reward_id, win, version) VALUES
(1, 'system', '2023-06-15 12:30:00', 'system', '2023-06-15 12:30:00', 'ACTIVE', 1, 1, 1, '2023-06-15 12:30:00', NULL, true, 0),
(2, 'system', '2023-06-15 13:00:00', 'system', '2023-06-15 13:00:00', 'ACTIVE', 1, 1, 1, '2023-06-15 13:00:00', NULL, false, 0),
(3, 'system', '2023-06-15 13:15:00', 'system', '2023-06-15 13:15:00', 'ACTIVE', 1, 1, 1, '2023-06-15 13:15:00', NULL, true, 0);

-- Insert Roles with role_type
INSERT INTO roles (id, created_by, created_at, updated_by, updated_at, status, role_type, description, display_order, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'ROLE_ADMIN', 'System Administrator', 1, 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'ROLE_USER', 'Regular User', 2, 0);

-- Insert Permissions based on PermissionName enum
INSERT INTO permissions (id, created_by, created_at, updated_by, updated_at, status, name, permission_type, description, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'CREATE_USER', 'WRITE', 'Create user accounts', 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'READ_USER', 'READ', 'View user accounts', 0),
(3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'UPDATE_USER', 'WRITE', 'Modify user accounts', 0),
(4, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'DELETE_USER', 'WRITE', 'Delete user accounts', 0),
(5, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'CREATE_EVENT', 'WRITE', 'Create events', 0),
(6, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'READ_EVENT', 'READ', 'View events', 0);

-- Insert Users with role_id field
INSERT INTO users (id, created_by, created_at, updated_by, updated_at, status, username, password, email, full_name, role_id, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'admin', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'admin@example.com', 'Admin User', 1, 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'user', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'user@example.com', 'Regular User', 2, 0);

-- Insert User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),
(2, 2);

-- Insert Role Permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4),
(2, 2);

-- Insert Blacklisted Tokens
INSERT INTO blacklisted_tokens (id, created_by, created_at, updated_by, updated_at, status, token, token_type, expiration_time, user_id, version) VALUES
(1, 'system', '2023-01-15 00:00:00', 'system', '2023-01-15 00:00:00', 'ACTIVE', 'eyJhbGciOiJIUzI1NiJ9.expiredToken1', 'ACCESS', '2023-01-14 00:00:00', 1, 0),
(2, 'system', '2023-02-15 00:00:00', 'system', '2023-02-15 00:00:00', 'ACTIVE', 'eyJhbGciOiJIUzI1NiJ9.expiredToken2', 'REFRESH', '2023-02-14 00:00:00', 2, 0);

-- Insert Configurations
INSERT INTO configurations (id, created_by, created_at, updated_by, updated_at, status, config_key, config_value, description, version) VALUES
(1, 'admin', '2023-01-01 00:00:00', 'admin', '2023-01-01 00:00:00', 'ACTIVE', 'EVENT_MAX_DURATION_DAYS', '90', 'Maximum event duration in days', 0),
(2, 'admin', '2023-01-01 00:00:00', 'admin', '2023-01-01 00:00:00', 'ACTIVE', 'DEFAULT_SPINS_PER_PARTICIPANT', '10', 'Default number of spins allocated per participant', 0);

-- Insert Audit Logs (updated object_id to be string values)
INSERT INTO audit_logs (id, created_by, created_at, updated_by, updated_at, status, object_type, object_id, property_path, old_value, new_value, value_type, update_time, context, action_type, version) VALUES
(1, 'admin', '2023-01-15 10:00:00', 'admin', '2023-01-15 10:00:00', 'ACTIVE', 'Event', '1', 'name', NULL, 'Summer Festival', 'String', '2023-01-15 10:00:00', 'Event creation', 'CREATED', 0),
(2, 'admin', '2023-02-10 12:00:00', 'admin', '2023-02-10 12:00:00', 'ACTIVE', 'Reward', '1', 'prize_value', NULL, '100.00', 'BigDecimal', '2023-02-10 12:00:00', 'Reward creation', 'CREATED', 0),
(3, 'admin', '2023-03-15 15:00:00', 'admin', '2023-03-15 15:00:00', 'ACTIVE', 'GoldenHour', '5', 'multiplier', NULL, '3.0', 'BigDecimal', '2023-03-15 15:00:00', 'Golden hour creation', 'CREATED', 0),
(4, 'admin', '2023-09-05 16:00:00', 'admin', '2023-09-05 16:00:00', 'ACTIVE', 'Event', '4', 'status', 'ACTIVE', 'INACTIVE', 'CommonStatus', '2023-09-05 16:00:00', 'Event deactivated', 'DEACTIVATED', 0),
(5, 'system', '2023-06-15 12:30:00', 'system', '2023-06-15 12:30:00', 'ACTIVE', 'SpinHistory', '1', 'win', NULL, 'true', 'Boolean', '2023-06-15 12:30:00', 'Spin result', 'CREATED', 0),
(6, 'admin', '2023-01-01 08:00:00', 'admin', '2023-01-01 08:00:00', 'ACTIVE', 'User', '1', 'username', NULL, 'admin', 'String', '2023-01-01 08:00:00', 'User creation', 'CREATED', 0);
