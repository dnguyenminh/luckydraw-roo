-- Clear all existing data
DELETE FROM spin_histories;
DELETE FROM golden_hours;
-- Remove reference to non-existent reward_events table
-- Instead, check for reward relationship table that actually exists in schema
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

-- Insert Roles based on RoleType enum
INSERT INTO roles (id, created_by, created_at, updated_by, updated_at, status, role_type, description, display_order, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_ADMIN', 'System Administrator', 1, 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_USER', 'Regular User', 2, 0),
(3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_MANAGER', 'Manager', 3, 0),
(4, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_PARTICIPANT', 'Participant', 4, 0);

-- Insert Permissions based on PermissionName enum
INSERT INTO permissions (id, created_by, created_at, updated_by, updated_at, status, name, type, description, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'CREATE_USER', 'WRITE', 'Create user accounts', 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'READ_USER', 'READ', 'View user accounts', 0),
(3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'UPDATE_USER', 'WRITE', 'Modify user accounts', 0),
(4, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'DELETE_USER', 'WRITE', 'Delete user accounts', 0),
(5, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'CREATE_EVENT', 'WRITE', 'Create events', 0),
(6, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'READ_EVENT', 'READ', 'View events', 0);

-- Insert Users with direct role reference (BCrypt-encoded passwords - all passwords are "password")
INSERT INTO users (id, created_by, created_at, updated_by, updated_at, status, username, password, email, full_name, role_id, version)
VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'admin', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'admin@example.com', 'Admin User', 1, 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'user', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'user@example.com', 'Regular User', 2, 0);

-- Insert Role Permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), -- Admin has all permissions
(2, 2), (2, 6), -- User has only read permissions
(3, 1), (3, 2), (3, 5), (3, 6), -- Manager has some write permissions
(4, 2), (4, 6); -- Participant has only read permissions

-- Insert Regions
INSERT INTO regions (id, created_by, created_at, updated_by, updated_at, status, name, code, description, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'North Region', 'NORTH', 'Northern provinces', 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Central Region', 'CENTRAL', 'Central provinces', 0),
(3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'South Region', 'SOUTH', 'Southern provinces', 0);

-- Insert Provinces (removed region_id column)
INSERT INTO provinces (id, created_by, created_at, updated_by, updated_at, status, name, code, description, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Hanoi', 'HN', 'Capital city', 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Ho Chi Minh', 'HCM', 'Southern metropolitan city', 0),
(3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Da Nang', 'DN', 'Central coast city', 0);

-- Insert many-to-many relationships between regions and provinces
INSERT INTO region_province (region_id, province_id) VALUES
(1, 1),  -- Hanoi belongs to North Region
(3, 2),  -- Ho Chi Minh belongs to South Region
(2, 3);  -- Da Nang belongs to Central Region

-- Insert Events
INSERT INTO events (id, created_by, created_at, updated_by, updated_at, status, name, code, description, start_time, end_time, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Summer Festival', 'SUMMER_FEST', 'Summer lucky draw event', DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('DAY', 20, CURRENT_TIMESTAMP), 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Winter Festival', 'WINTER_FEST', 'Winter lucky draw event', DATEADD('DAY', 30, CURRENT_TIMESTAMP), DATEADD('DAY', 60, CURRENT_TIMESTAMP), 0);

-- Insert Event Locations (with composite key)
INSERT INTO event_locations (event_id, region_id, province_id, created_by, created_at, updated_by, updated_at, status, name, description, max_spin, remaining_today_spin, daily_spin_dist_rate, version) VALUES
(1, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'North Summer Location', 'North summer event', 100, 50, 0.1, 0),
(1, 3, 2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'South Summer Location', 'South summer event', 150, 75, 0.1, 0),
(2, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'North Winter Location', 'North winter event', 200, 100, 0.2, 0),
(2, 2, 3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'INACTIVE', 'Central Winter Location', 'Central winter event', 80, 40, 0.1, 0);

-- Insert Participants
INSERT INTO participants (id, created_by, created_at, updated_by, updated_at, status, name, code, phone, address, province_id, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'John Doe', 'JOHN001', '1234567890', '123 Main St', 1, 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Jane Smith', 'JANE001', '2345678901', '456 Oak Ave', 2, 0),
(3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Robert Johnson', 'ROBERT001', '3456789012', '789 Pine Rd', 3, 0);

-- Insert Participant Events (with composite key)
INSERT INTO participant_events (participant_id, event_id, region_id, created_by, created_at, updated_by, updated_at, status, spins_remaining, version) VALUES
(1, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 5, 0),
(2, 1, 3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 3, 0),
(3, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 4, 0),
(1, 2, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 2, 0);

-- Insert reward data using the actual rewards table
-- Use the event_id and region_id fields from the rewards table instead
-- of a separate reward_events table
INSERT INTO rewards (id, created_by, created_at, updated_by, updated_at, status, name, code, description, prize_value, event_id, region_id, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Gold Prize', 'GOLD', 'Gold prize description', 1000.00, 1, 1, 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Silver Prize', 'SILVER', 'Silver prize description', 500.00, 1, 3, 0),
(3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Bronze Prize', 'BRONZE', 'Bronze prize description', 250.00, 2, 1, 0);

-- Insert Golden Hours
INSERT INTO golden_hours (id, created_by, created_at, updated_by, updated_at, status, event_id, region_id, start_time, end_time, multiplier, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 1, 1, DATEADD('HOUR', 1, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, CURRENT_TIMESTAMP), 2.0, 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 1, 3, DATEADD('HOUR', 2, CURRENT_TIMESTAMP), DATEADD('HOUR', 4, CURRENT_TIMESTAMP), 1.5, 0);

-- Insert Spin Histories (corrected to match the actual table schema)
INSERT INTO spin_histories (id, created_by, created_at, updated_by, updated_at, status, participant_id, event_id, region_id, spin_time, reward_id, reward_event_id, reward_region_id, win, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 1, 1, 1, DATEADD('HOUR', -1, CURRENT_TIMESTAMP), 1, 1, 1, true, 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 1, 1, 1, DATEADD('HOUR', -2, CURRENT_TIMESTAMP), NULL, NULL, NULL, false, 0),
(3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 2, 1, 3, DATEADD('HOUR', -3, CURRENT_TIMESTAMP), 3, 1, 3, true, 0);

-- Insert Blacklisted Tokens
INSERT INTO blacklisted_tokens (id, created_by, created_at, updated_by, updated_at, status, token, token_type, expiration_time, user_id, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'eyJhbGciOiJIUzI1NiJ9.expiredToken1', 'ACCESS', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 1, 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'eyJhbGciOiJIUzI1NiJ9.expiredToken2', 'REFRESH', DATEADD('DAY', -2, CURRENT_TIMESTAMP), 2, 0);

-- Insert Configurations
INSERT INTO configurations (id, created_by, created_at, updated_by, updated_at, status, config_key, config_value, description, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'TOKEN_EXPIRY_MINUTES', '60', 'Authentication token expiry in minutes', 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'MAX_DAILY_SPINS', '5', 'Maximum spins per day per participant', 0);

-- Insert Audit Logs
INSERT INTO audit_logs (id, created_by, created_at, updated_by, updated_at, status, object_type, object_id, property_path, old_value, new_value, value_type, update_time, context, action_type, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Event', '1', 'name', NULL, 'Summer Festival', 'String', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 'Event creation', 'CREATED', 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Reward', '1', 'prize_value', NULL, '1000.00', 'BigDecimal', DATEADD('HOUR', -12, CURRENT_TIMESTAMP), 'Reward creation', 'CREATED', 0);
