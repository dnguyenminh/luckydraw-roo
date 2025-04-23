-- Entity test data

-- Clear all existing data
DELETE FROM spin_histories;
DELETE FROM golden_hours;
DELETE FROM reward_events;
DELETE FROM rewards;
DELETE FROM participant_events;
DELETE FROM participants;
DELETE FROM event_locations;
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

-- Insert Regions (minimal required for tests)
INSERT INTO regions (id, created_by, created_at, updated_by, updated_at, status, name, code, description, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'North Region', 'NORTH', 'Northern provinces of the country', 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'South Region', 'SOUTH', 'Southern provinces of the country', 0);

-- Insert Provinces (minimal required for tests)
INSERT INTO provinces (id, created_by, created_at, updated_by, updated_at, status, name, code, description, region_id, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Province A', 'PROV_A', 'First province in North Region', 1, 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Province B', 'PROV_B', 'First province in South Region', 2, 0);

-- Insert Events (minimal required for tests)
INSERT INTO events (id, created_by, created_at, updated_by, updated_at, status, name, code, description, start_time, end_time, version) VALUES
(1, 'admin', '2023-01-15 00:00:00', 'admin', '2023-01-15 00:00:00', 'ACTIVE', 'Summer Festival', 'SUMMER_FEST', 'Annual summer lucky draw event', '2023-06-01 00:00:00', '2023-08-31 23:59:59', 0);

-- Clear existing data for event_locations if needed
DELETE FROM event_locations;

-- Insert event locations with ID column and all required audit fields
INSERT INTO event_locations (id, created_by, created_at, updated_by, updated_at, status, name, code, description, max_spin, quantity, win_probability, event_id, region_id, version)
VALUES
(1, 'system', NOW(), 'system', NOW(), 'ACTIVE', 'Location 1', 'LOC_1', 'Test location 1', 100, 50, 0.2, 1, 1, 0),
(2, 'system', NOW(), 'system', NOW(), 'ACTIVE', 'Location 2', 'LOC_2', 'Test location 2', 200, 100, 0.3, 1, 2, 0),
(3, 'system', NOW(), 'system', NOW(), 'INACTIVE', 'Location 3', 'LOC_3', 'Test location 3', 50, 25, 0.1, 2, 1, 0);

-- Insert Participants (minimal required for tests)
INSERT INTO participants (id, created_by, created_at, updated_by, updated_at, status, name, code, phone, address, province_id, version) VALUES
(1, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'ACTIVE', 'John Doe', 'JOHN001', '1234567890', '123 Main St', 1, 0);

-- Insert Participant Events (minimal required for tests)
INSERT INTO participant_events (id, created_by, created_at, updated_by, updated_at, status, event_location_id, participant_id, spins_remaining, version) VALUES
(1, 'system', '2023-06-05 00:00:00', 'system', '2023-06-05 00:00:00', 'ACTIVE', 1, 1, 5, 0);

-- Insert Rewards (minimal required for tests)
INSERT INTO rewards (id, created_by, created_at, updated_by, updated_at, status, name, code, description, prizeValue, probability, quantity, event_location_id, version) VALUES
(1, 'admin', '2023-02-10 00:00:00', 'admin', '2023-02-10 00:00:00', 'ACTIVE', 'Cash Prize $100', 'CASH100', '$100 cash prize', 100.00, 0.05, 10, 1, 0);

-- Insert Golden Hours (minimal required for tests)
INSERT INTO golden_hours (id, created_by, created_at, updated_by, updated_at, status, event_location_id, start_time, end_time, multiplier, version) VALUES
(1, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 1, '2023-06-15 12:00:00', '2023-06-15 14:00:00', 2.0, 0);

-- Insert Spin Histories (minimal required for tests)
INSERT INTO spin_histories (id, created_by, created_at, updated_by, updated_at, status, participant_event_id, spin_time, reward_id, win, version) VALUES
(1, 'system', '2023-06-15 12:30:00', 'system', '2023-06-15 12:30:00', 'ACTIVE', 1, '2023-06-15 12:30:00', 1, true, 0);

-- Insert roles
INSERT INTO roles (id, role_type, description, display_order, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 'ROLE_ADMIN', 'Administrator role', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'ROLE_USER', 'User role', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert permissions using correct enum values
INSERT INTO permissions (id, name, type, description, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 'CREATE_USER', 'WRITE', 'Create user accounts', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'READ_USER', 'READ', 'View user accounts', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'UPDATE_EVENT', 'WRITE', 'Update events', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Role-Permission associations
INSERT INTO role_permissions (role_id, permission_id)
VALUES
(1, 1), (1, 2), (1, 3), -- Admin has all permissions
(2, 2);                  -- User has read user permission

-- Insert users
INSERT INTO users (id, username, password, email, full_name, role_id, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'admin', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'admin@example.com', 'Admin User', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'user', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'user@example.com', 'Regular User', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert User Roles (minimal required for tests)
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),
(2, 2);
