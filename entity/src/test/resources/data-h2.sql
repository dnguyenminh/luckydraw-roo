-- Test data for H2 database

-- Clear all existing data to avoid conflicts
DELETE FROM spin_histories;
DELETE FROM golden_hours;
DELETE FROM reward_events;
DELETE FROM rewards;
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

-- Insert Regions with updated_at and updated_by values
INSERT INTO regions (id, code, name, status, created_by, created_at, updated_by, updated_at, version)
VALUES
    (1, 'NORTH', 'Northern Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
    (2, 'CENTRAL', 'Central Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
    (3, 'SOUTH', 'Southern Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
    (4, 'EAST', 'Eastern Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
    (5, 'WEST', 'Western Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
    (6, 'NORTH_EAST', 'North Eastern Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
    (7, 'NORTH_WEST', 'North Western Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
    (8, 'SOUTH_EAST', 'South Eastern Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
    (9, 'SOUTH_WEST', 'South Western Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
    (10, 'CENTRAL_NORTH', 'Central North Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert Provinces
INSERT INTO provinces (id, version, created_at, created_by, updated_at, updated_by, name, code, status)
VALUES
    (1, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Hanoi', 'HN', 'ACTIVE'),
    (2, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Da Nang', 'DN', 'ACTIVE'),
    (3, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Ho Chi Minh', 'HCM', 'ACTIVE'),
    (4, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Hai Phong', 'HP', 'ACTIVE'),
    (5, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Can Tho', 'CT', 'ACTIVE');

-- Link Regions and Provinces
INSERT INTO region_province (region_id, province_id)
VALUES
    (1, 1), -- North - Hanoi
    (2, 2), -- Central - Da Nang
    (3, 3), -- South - Ho Chi Minh
    (1, 4), -- North - Hai Phong
    (3, 5); -- South - Can Tho

-- Insert Events
INSERT INTO events (id, version, created_at, created_by, updated_at, updated_by, name, code, description, start_time, end_time, status)
VALUES
    (1, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Summer Festival', 'SUMMER_FEST', 'Summer lucky draw event', DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('DAY', 20, CURRENT_TIMESTAMP), 'ACTIVE'),
    (2, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Winter Festival', 'WINTER_FEST', 'Winter lucky draw event', DATEADD('DAY', 30, CURRENT_TIMESTAMP), DATEADD('DAY', 60, CURRENT_TIMESTAMP), 'ACTIVE'),
    (3, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Spring Festival', 'SPRING_FEST', 'Spring lucky draw event', DATEADD('DAY', 90, CURRENT_TIMESTAMP), DATEADD('DAY', 120, CURRENT_TIMESTAMP), 'ACTIVE'),
    (4, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Autumn Festival', 'AUTUMN_FEST', 'Autumn lucky draw event', DATEADD('DAY', 150, CURRENT_TIMESTAMP), DATEADD('DAY', 180, CURRENT_TIMESTAMP), 'ACTIVE');

-- Insert Event Locations
INSERT INTO event_locations (event_id, region_id, version, created_at, created_by, updated_at, updated_by, max_spin, today_spin, daily_spin_dist_rate, description, status)
VALUES
    (1, 1, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 100, 50, 0.1, 'North summer event', 'ACTIVE'),
    (1, 3, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 150, 75, 0.1, 'South summer event', 'ACTIVE'),
    (2, 1, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 200, 100, 0.2, 'North winter event', 'ACTIVE'),
    (2, 2, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 80, 40, 0.1, 'Central winter event', 'INACTIVE'),
    (2, 3, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 120, 60, 0.15, 'South winter event', 'ACTIVE'), -- Added missing record for event 2, region 3
    (3, 1, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 120, 60, 0.15, 'North spring event', 'ACTIVE'),
    (4, 3, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 180, 90, 0.25, 'South autumn event', 'ACTIVE');

-- Insert Golden Hours
INSERT INTO golden_hours (id, version, created_at, created_by, updated_at, updated_by, start_time, end_time, multiplier, event_id, region_id, status)
VALUES
    (1, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', '2023-06-15 12:00:00', '2023-06-15 14:00:00', 2.0, 1, 1, 'ACTIVE'),
    (2, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', '2023-07-15 18:00:00', '2023-07-15 20:00:00', 2.0, 1, 3, 'ACTIVE'),
    (3, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', '2023-12-20 13:00:00', '2023-12-20 15:00:00', 2.5, 2, 1, 'ACTIVE'),
    (4, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', '2024-03-10 17:00:00', '2024-03-10 19:00:00', 1.5, 3, 1, 'ACTIVE');

-- Insert Participants
INSERT INTO participants (id, version, created_at, created_by, updated_at, updated_by, name, code, phone, email, address, province_id, last_adding_spin, status)
VALUES
    (1, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'John Doe', 'JOHN001', '1234567890', 'john@example.com', '123 Pham Van Dong St, Cau Giay District', 1, 0, 'ACTIVE'),
    (2, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Jane Smith', 'JANE001', '2345678901', 'jane@example.com', '456 Nguyen Tat Thanh St, Hai Chau District', 2, 0, 'ACTIVE'),
    (3, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Robert Johnson', 'ROBERT001', '3456789012', 'robert@example.com', '789 Le Duan St, District 1', 3, 0, 'ACTIVE'),
    (4, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Mary Williams', 'MARY001', '4567890123', 'mary@example.com', '101 Le Chan St, Ngo Quyen District', 4, 0, 'ACTIVE'),
    (5, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'David Brown', 'DAVID001', '5678901234', 'david@example.com', '202 Mau Than St, Ninh Kieu District', 5, 0, 'ACTIVE');

-- Insert Rewards
INSERT INTO rewards (id, version, created_at, created_by, updated_at, updated_by, name, code, description, status)
VALUES
    (1, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Gold Prize', 'GOLD', 'Gold prize description', 'ACTIVE'),
    (2, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Silver Prize', 'SILVER', 'Silver prize description', 'ACTIVE'),
    (3, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Bronze Prize', 'BRONZE', 'Bronze prize description', 'ACTIVE'),
    (4, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Platinum Prize', 'PLATINUM', 'Platinum prize description', 'ACTIVE'),
    (5, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Diamond Prize', 'DIAMOND', 'Diamond prize description', 'ACTIVE');

-- Insert Reward Events
INSERT INTO reward_events (version, created_at, created_by, updated_at, updated_by, reward_id, event_id, region_id, quantity, today_quantity, probability, status)
VALUES
    (0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 1, 1, 1, 10, 2, 0.1, 'ACTIVE'),
    (0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 2, 1, 1, 20, 5, 0.2, 'ACTIVE'),
    (0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 3, 1, 3, 30, 10, 0.3, 'ACTIVE'),
    (0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 4, 2, 1, 15, 3, 0.15, 'ACTIVE'),
    (0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 5, 3, 1, 25, 8, 0.25, 'ACTIVE');

-- Insert Participant Events
INSERT INTO participant_events (version, created_at, created_by, updated_at, updated_by, participant_id, event_id, region_id, spins_remaining, status)
VALUES
    (0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 1, 1, 1, 5, 'ACTIVE'),
    (0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 2, 1, 3, 3, 'ACTIVE'),
    (0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 3, 1, 1, 4, 'ACTIVE'),
    (0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 1, 2, 1, 2, 'ACTIVE'),
    (0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 4, 3, 1, 6, 'ACTIVE'),
    (0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 2, 2, 3, 2, 'ACTIVE'),
    (0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 3, 3, 1, 3, 'ACTIVE'); -- Added missing record for spin history #4

-- Insert Spin Histories
INSERT INTO spin_histories (
    id, version, created_at, created_by, updated_at, updated_by, status,
    participant_id, participant_event_id, participant_region_id,
    spin_time,
    reward_id, reward_event_id, reward_region_id,
    golden_hour_id, win, wheel_position, multiplier, server_seed, client_seed
) VALUES
    (1, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'ACTIVE', 1, 1, 1, DATEADD('HOUR', -1, CURRENT_TIMESTAMP), 1, 1, 1, 1, true, 120.5, 2.0, 'server-seed-1', 'client-seed-1'),
    (2, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'ACTIVE', 1, 1, 1, DATEADD('HOUR', -2, CURRENT_TIMESTAMP), NULL, NULL, NULL, NULL, false, 45.2, 1.0, 'server-seed-2', 'client-seed-2'),
    (3, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'ACTIVE', 2, 2, 3, DATEADD('HOUR', -3, CURRENT_TIMESTAMP), 3, 1, 3, NULL, true, 230.7, 1.0, 'server-seed-3', 'client-seed-3'),
    (4, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'ACTIVE', 3, 3, 1, DATEADD('HOUR', -4, CURRENT_TIMESTAMP), 2, 1, 1, NULL, true, 175.3, 1.0, 'server-seed-4', 'client-seed-4'),
    (5, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'ACTIVE', 1, 2, 1, DATEADD('HOUR', -5, CURRENT_TIMESTAMP), NULL, NULL, NULL, NULL, false, 85.9, 1.0, 'server-seed-5', 'client-seed-5'),
    (6, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'ACTIVE', 4, 3, 1, DATEADD('HOUR', -6, CURRENT_TIMESTAMP), 5, 3, 1, 4, true, 310.2, 1.5, 'server-seed-6', 'client-seed-6');

-- Insert Roles
INSERT INTO roles (id, version, created_at, created_by, updated_at, updated_by, role_type, description, display_order, status)
VALUES
    (1, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'ROLE_ADMIN', 'System Administrator', 1, 'ACTIVE'),
    (2, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'ROLE_USER', 'Regular User', 2, 'ACTIVE'),
    (3, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'ROLE_MANAGER', 'Manager', 3, 'ACTIVE'),
    (4, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'ROLE_GUEST', 'Guest', 4, 'ACTIVE');

-- Insert Permissions
INSERT INTO permissions (id, version, created_at, created_by, updated_at, updated_by, name, permission_type, description, status)
VALUES
    (1, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'CREATE_USER', 'WRITE', 'Create user accounts', 'ACTIVE'),
    (2, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'READ_USER', 'READ', 'View user accounts', 'ACTIVE'),
    (3, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'UPDATE_USER', 'WRITE', 'Modify user accounts', 'ACTIVE'),
    (4, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'DELETE_USER', 'WRITE', 'Delete user accounts', 'ACTIVE'),
    (5, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'CREATE_EVENT', 'WRITE', 'Create events', 'ACTIVE'),
    (6, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'READ_EVENT', 'READ', 'View events', 'ACTIVE'),
    (7, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'UPDATE_EVENT', 'WRITE', 'Update events', 'ACTIVE'),
    (8, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'DELETE_EVENT', 'WRITE', 'Delete events', 'ACTIVE');

-- Insert Role Permissions
INSERT INTO role_permissions (role_id, permission_id)
VALUES
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), -- Admin has all permissions
    (2, 2), (2, 6), -- User has only read permissions
    (3, 2), (3, 3), (3, 5), (3, 6), (3, 7); -- Manager has read/update user, create/read/update event

-- Insert Users with direct role reference
INSERT INTO users (id, version, created_at, created_by, updated_at, updated_by, username, password, email, full_name, role_id, status)
VALUES
    (1, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'admin', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'admin@example.com', 'Admin User', 1, 'ACTIVE'),
    (2, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'user', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'user@example.com', 'Regular User', 2, 'ACTIVE'),
    (3, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'manager', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'manager@example.com', 'Manager User', 3, 'ACTIVE'),
    (4, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'guest', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'guest@example.com', 'Guest User', 4, 'ACTIVE');

-- Insert Blacklisted Tokens
INSERT INTO blacklisted_tokens (id, version, created_at, created_by, updated_at, updated_by, token, token_type, expiration_time, user_id, status)
VALUES
    (1, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'eyJhbGciOiJIUzI1NiJ9.expiredToken1', 'ACCESS', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 1, 'ACTIVE'),
    (2, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'eyJhbGciOiJIUzI1NiJ9.expiredToken2', 'REFRESH', DATEADD('DAY', -2, CURRENT_TIMESTAMP), 2, 'ACTIVE'),
    (3, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'eyJhbGciOiJIUzI1NiJ9.validToken', 'ACCESS', DATEADD('DAY', 1, CURRENT_TIMESTAMP), 3, 'ACTIVE');

-- Insert Configurations
INSERT INTO configurations (id, version, created_at, created_by, updated_at, updated_by, config_key, config_value, description, data_type, validation_regex, modifiable, status)
VALUES
    (1, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'TOKEN_EXPIRY_MINUTES', '60', 'Authentication token expiry in minutes', 'INTEGER', '^[0-9]+$', true, 'ACTIVE'),
    (2, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'MAX_DAILY_SPINS', '5', 'Maximum spins per day per participant', 'INTEGER', '^[0-9]+$', true, 'ACTIVE'),
    (3, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'SYSTEM_MAINTENANCE', 'false', 'System maintenance mode', 'BOOLEAN', '^(true|false)$', true, 'ACTIVE'),
    (4, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'DEFAULT_TIMEZONE', 'Asia/Ho_Chi_Minh', 'Default timezone for application', 'STRING', '^[A-Za-z0-9/]+$', true, 'ACTIVE'),
    (5, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'MAX_LOGIN_ATTEMPTS', '3', 'Maximum failed login attempts before lockout', 'INTEGER', '^[0-9]+$', true, 'ACTIVE');

-- Insert Audit Logs
INSERT INTO audit_logs (id, version, created_at, created_by, updated_at, updated_by, object_type, object_id, property_path, old_value, new_value, value_type, update_time, action_type, status)
VALUES
    (1, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'User', '1', 'username', NULL, 'admin', 'String', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 'CREATED', 'ACTIVE'),
    (2, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Event', '1', 'name', 'Old Event Name', 'Summer Festival', 'String', DATEADD('HOUR', -12, CURRENT_TIMESTAMP), 'MODIFIED', 'ACTIVE'),
    (3, 0, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 'Reward', '2', 'status', 'ACTIVE', 'INACTIVE', 'CommonStatus', DATEADD('HOUR', -6, CURRENT_TIMESTAMP), 'MODIFIED', 'ACTIVE');

-- Reset all sequences to values higher than the existing IDs
-- This ensures that new entities created during tests will get non-conflicting IDs
ALTER TABLE regions ALTER COLUMN id RESTART WITH 100;
ALTER TABLE provinces ALTER COLUMN id RESTART WITH 100;
ALTER TABLE events ALTER COLUMN id RESTART WITH 100;
ALTER TABLE golden_hours ALTER COLUMN id RESTART WITH 100;
ALTER TABLE participants ALTER COLUMN id RESTART WITH 100;
ALTER TABLE rewards ALTER COLUMN id RESTART WITH 100;
ALTER TABLE spin_histories ALTER COLUMN id RESTART WITH 100;
ALTER TABLE roles ALTER COLUMN id RESTART WITH 100;
ALTER TABLE permissions ALTER COLUMN id RESTART WITH 100;
ALTER TABLE users ALTER COLUMN id RESTART WITH 100;
ALTER TABLE blacklisted_tokens ALTER COLUMN id RESTART WITH 100;
ALTER TABLE configurations ALTER COLUMN id RESTART WITH 100;
