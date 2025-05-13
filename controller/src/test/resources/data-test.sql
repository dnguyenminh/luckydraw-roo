-- Test data insertion script for controller tests
-- Using the same data as repository tests for consistency

-- Clear existing data
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

-- Insert roles (based on RoleType enum)
INSERT INTO roles (id, role_type, description, display_order, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 'ROLE_ADMIN', 'Administrator with all permissions', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'ROLE_USER', 'Regular user with limited permissions', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'ROLE_MANAGER', 'Event manager with event management permissions', 3, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 'ROLE_PARTICIPANT', 'Participant role with minimal access', 4, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(5, 'ROLE_GUEST', 'Guest user with read-only access', 5, 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert permissions (using correct enum values)
INSERT INTO permissions (id, name, permission_type, description, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'CREATE_USER', 'WRITE', 'Create new user accounts', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'READ_USER', 'READ', 'View user accounts', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'UPDATE_USER', 'WRITE', 'Modify user accounts', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 'DELETE_USER', 'WRITE', 'Delete user accounts', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(5, 'CREATE_EVENT', 'WRITE', 'Create new events', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(6, 'READ_EVENT', 'READ', 'View events', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(7, 'UPDATE_EVENT', 'WRITE', 'Modify events', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(8, 'DELETE_EVENT', 'WRITE', 'Delete events', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(9, 'CREATE_REWARD', 'WRITE', 'Create rewards', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(10, 'READ_REWARD', 'READ', 'View rewards', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert users (bcrypt encoded password 'password')
INSERT INTO users (id, username, password, email, full_name, role_id, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'admin', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'admin@example.com', 'System Administrator', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'user', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'user@example.com', 'Regular User', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'manager', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'manager@example.com', 'Event Manager', 3, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 'participant', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'participant@example.com', 'Event Participant', 4, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(5, 'inactive', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'inactive@example.com', 'Inactive User', 2, 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert role permissions
INSERT INTO role_permissions (role_id, permission_id)
VALUES
-- Admin has all permissions
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
-- Regular user has read permissions
(2, 2), (2, 6), (2, 10),
-- Manager has event management permissions
(3, 5), (3, 6), (3, 7), (3, 8), (3, 9), (3, 10),
-- Participant has minimal read permissions
(4, 6), (4, 10);

-- Insert regions
INSERT INTO regions (id, code, name, description, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'NORTH', 'Northern Region', 'Northern provinces and cities', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'CENTRAL', 'Central Region', 'Central provinces and cities', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'SOUTH', 'Southern Region', 'Southern provinces and cities', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 'ISLAND', 'Island Region', 'Island territories', 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert provinces
INSERT INTO provinces (id, code, name, description, region_id, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'HN', 'Hanoi', 'Capital city', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'HCM', 'Ho Chi Minh City', 'Southern metropolis', 3, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'DN', 'Da Nang', 'Central coastal city', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 'HP', 'Hai Phong', 'Northern port city', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(5, 'CT', 'Can Tho', 'Mekong Delta city', 3, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(6, 'PQ', 'Phu Quoc', 'Island district', 4, 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert events
INSERT INTO events (id, code, name, description, start_time, end_time, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'SUMMER2023', 'Summer Lucky Draw 2023', 'Summer promotional event with great prizes', '2023-06-01 00:00:00', '2023-08-31 23:59:59', 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 'TET2023', 'Lunar New Year 2023', 'Special Tet holiday event', '2023-01-22 00:00:00', '2023-01-27 23:59:59', 'INACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 'FALL2023', 'Fall Festival 2023', 'Autumn season special rewards', '2023-09-15 00:00:00', '2023-11-15 23:59:59', 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 'WINTER2023', 'Winter Wonderland 2023', 'End of year celebration', '2023-12-01 00:00:00', '2023-12-31 23:59:59', 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);

-- Insert event locations with composite key
INSERT INTO event_locations (event_id, region_id, name, description, max_spin, today_spin, daily_spin_dist_rate, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 1, 'North Summer', 'Summer event in North', 1000, 50, 0.1, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(1, 2, 'Central Summer', 'Summer event in Central', 800, 40, 0.1, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(1, 3, 'South Summer', 'Summer event in South', 1200, 60, 0.1, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 1, 'North Tet', 'Tet event in North', 500, 0, 0.2, 'INACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 2, 'Central Fall', 'Fall event in Central', 600, 30, 0.15, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 3, 'South Fall', 'Fall event in South', 900, 45, 0.15, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 1, 'North Winter', 'Winter event in North', 700, 35, 0.2, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 3, 'South Winter', 'Winter event in South', 800, 40, 0.2, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);

-- Insert participants
INSERT INTO participants (id, name, code, phone, email, address, province_id, status, created_by, created_at, updated_by, updated_at, version, last_adding_spin)
VALUES
(1, 'John Doe', 'P001', '0901234567', 'john@example.com', '123 Tran Hung Dao St, Hoan Kiem District', 1, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0, 0),
(2, 'Jane Smith', 'P002', '0912345678', 'jane@example.com', '456 Nguyen Hue Blvd, District 1', 2, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0, 0),
(3, 'Robert Brown', 'P003', '0923456789', 'robert@example.com', '789 Bach Dang St, Hai Chau District', 3, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0, 0),
(4, 'Mary Johnson', 'P004', '0934567890', 'mary@example.com', '101 Dien Bien Phu St, Hong Bang District', 4, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0, 0),
(5, 'David Wilson', 'P005', '0945678901', 'david@example.com', '202 Nguyen Van Linh St, Ninh Kieu District', 5, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0, 0),
(6, 'Susan Miller', 'P006', '0956789012', 'susan@example.com', '303 Kim Ma St, Ba Dinh District', 1, 'INACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0, 0);

-- Insert participant events with composite key
INSERT INTO participant_events (participant_id, event_id, region_id, spins_remaining, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 1, 1, 5, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(1, 1, 2, 3, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(1, 3, 2, 4, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 1, 1, 2, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 1, 3, 3, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 1, 2, 1, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 1, 3, 4, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(5, 3, 3, 2, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(6, 2, 1, 0, 'INACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);

-- Insert rewards
INSERT INTO rewards (id, code, name, description, prize_value, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'R001', 'Cash Prize', 'Cash prize worth $100', 100.00, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 'R002', 'Gift Card', 'Gift card worth $50', 50.00, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 'R003', 'Smartphone', 'Latest smartphone model', 800.00, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 'R004', 'Tablet', 'Premium tablet device', 400.00, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(5, 'R005', 'Travel Voucher', 'Travel voucher worth $500', 500.00, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(6, 'R006', 'Headphones', 'Noise cancelling headphones', 200.00, 'INACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);

-- Insert reward events with composite key
INSERT INTO reward_events (reward_id, event_id, region_id, quantity, today_quantity, probability, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 1, 1, 10, 2, 0.05, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 1, 1, 20, 5, 0.10, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 1, 2, 5, 1, 0.02, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 1, 3, 8, 2, 0.04, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(5, 3, 2, 15, 3, 0.08, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(6, 3, 3, 12, 3, 0.06, 'INACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);

-- Insert golden hours
INSERT INTO golden_hours (id, event_id, region_id, start_time, end_time, multiplier, max_rewards, claimed_rewards, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 1, 1, '2023-07-01 18:00:00', '2023-07-01 20:00:00', 2.0, 50, 10, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 1, 2, '2023-07-02 19:00:00', '2023-07-02 21:00:00', 2.0, 40, 5, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 1, 3, '2023-07-03 20:00:00', '2023-07-03 22:00:00', 2.5, 30, 0, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 3, 2, '2023-10-15 18:00:00', '2023-10-15 20:00:00', 1.5, 20, 0, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);

-- Insert spin histories with composite foreign keys
INSERT INTO spin_histories (id, participant_id, participant_event_id, participant_region_id, spin_time, reward_id, reward_event_id, reward_region_id, golden_hour_id, win, wheel_position, multiplier, server_seed, client_seed, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 1, 1, 1, '2023-07-01 19:15:30', 1, 1, 1, 1, true, 120.5, 2.0, 'srv-seed-001', 'cli-seed-001', 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 1, 1, 1, '2023-07-02 14:20:15', NULL, NULL, NULL, NULL, false, 45.8, 1.0, 'srv-seed-002', 'cli-seed-002', 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 2, 1, 1, '2023-07-02 16:30:45', 2, 1, 1, NULL, true, 220.3, 1.0, 'srv-seed-003', 'cli-seed-003', 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 2, 1, 3, '2023-07-03 10:10:20', NULL, NULL, NULL, NULL, false, 75.1, 1.0, 'srv-seed-004', 'cli-seed-004', 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(5, 3, 1, 2, '2023-07-03 12:05:55', 3, 1, 2, NULL, true, 180.7, 1.0, 'srv-seed-005', 'cli-seed-005', 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(6, 4, 1, 3, '2023-07-03 21:15:10', 4, 1, 3, 3, true, 270.2, 2.5, 'srv-seed-006', 'cli-seed-006', 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);

-- Insert blacklisted tokens
INSERT INTO blacklisted_tokens (id, token, token_type, user_id, expiration_time, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.token1', 'ACCESS', 1, DATEADD('HOUR', 1, CURRENT_TIMESTAMP), 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.token2', 'ACCESS', 2, DATEADD('HOUR', -1, CURRENT_TIMESTAMP), 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYW5hZ2VyIn0.token3', 'REFRESH', 3, DATEADD('DAY', 7, CURRENT_TIMESTAMP), 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert configurations
INSERT INTO configurations (id, config_key, config_value, description, data_type, validation_regex, modifiable, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'MAX_SPINS_PER_DAY', '5', 'Maximum spins allowed per day per participant', 'INTEGER', '^[0-9]+$', true, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 'DEFAULT_MULTIPLIER', '1.0', 'Default reward multiplier', 'DOUBLE', '^[0-9]+(\\.[0-9]+)?$', true, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 'ENABLE_GOLDEN_HOURS', 'true', 'Enable golden hour feature', 'BOOLEAN', '^(true|false)$', true, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 'SYSTEM_MAINTENANCE', 'false', 'System maintenance mode', 'BOOLEAN', '^(true|false)$', true, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);

-- Insert audit logs
INSERT INTO audit_logs (id, object_type, object_id, property_path, old_value, new_value, value_type, update_time, action_type, context, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'User', '1', 'username', 'old_admin', 'admin', 'String', '2023-01-15 10:30:00', 'UPDATE', 'User management', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'Reward', '3', 'prize_value', '700.00', '800.00', 'BigDecimal', '2023-01-20 14:45:00', 'UPDATE', 'Reward adjustment', 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 'Participant', '6', 'status', 'ACTIVE', 'INACTIVE', 'CommonStatus', '2023-01-25 09:15:00', 'UPDATE', 'Participant deactivation', 'ACTIVE', 'manager', CURRENT_TIMESTAMP, 'manager', CURRENT_TIMESTAMP, 0),
(4, 'Event', '2', 'end_time', '2023-01-25 23:59:59', '2023-01-27 23:59:59', 'LocalDateTime', '2023-01-20 16:30:00', 'UPDATE', 'Event extension', 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(5, 'EventLocation', '2_1', 'max_spin', '400', '500', 'Integer', '2023-01-22 11:00:00', 'UPDATE', 'Capacity increase', 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);