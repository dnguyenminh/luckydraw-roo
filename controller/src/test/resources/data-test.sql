-- Test data for controller module tests

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
INSERT INTO permissions (id, name, type, description, status, created_by, created_at, updated_by, updated_at, version)
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
INSERT INTO event_locations (event_id, region_id, description, max_spin, today_spin, daily_spin_distributing_rate, daily_spin_dist_rate, remaining_today_spin, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 1, 'Summer event in North', 1000, 100, 0.1, 0.1, 50, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(1, 2, 'Summer event in Central', 800, 80, 0.1, 0.1, 40, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(1, 3, 'Summer event in South', 1200, 120, 0.1, 0.1, 60, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 1, 'Tet event in North', 500, 50, 0.2, 0.2, 25, 'INACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 3, 'Tet event in South', 500, 50, 0.2, 0.2, 25, 'INACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 1, 'Fall event in North', 700, 70, 0.15, 0.15, 35, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 2, 'Fall event in Central', 600, 60, 0.15, 0.15, 30, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 3, 'Winter event in South', 900, 90, 0.1, 0.1, 45, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);

-- Insert participants
INSERT INTO participants (id, code, name, phone, address, last_adding_spin, province_id, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'P001', 'John Doe', '0901234567', '123 Main St, Hanoi', 0, 1, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 'P002', 'Jane Smith', '0912345678', '456 Oak St, Ho Chi Minh City', 0, 2, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 'P003', 'Bob Johnson', '0923456789', '789 Pine St, Da Nang', 0, 3, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 'P004', 'Alice Brown', '0934567890', '321 Elm St, Hai Phong', 0, 4, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(5, 'P005', 'Charlie Wilson', '0945678901', '654 Maple St, Can Tho', 0, 5, 'INACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);

-- Insert participant_events with composite key
INSERT INTO participant_events (participant_id, event_id, region_id, spins_remaining, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 1, 1, 5, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(1, 3, 1, 3, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 1, 3, 4, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 3, 2, 2, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 1, 2, 6, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 4, 3, 3, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(5, 1, 3, 0, 'INACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);

-- Insert rewards
INSERT INTO rewards (id, code, name, description, prize_value, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'CASH100', '$100 Cash Prize', 'Cash prize of $100', 100.00, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 'CASH50', '$50 Cash Prize', 'Cash prize of $50', 50.00, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 'VOUCHER25', '$25 Gift Voucher', 'Gift voucher worth $25', 25.00, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 'SMARTPHONE', 'Smartphone', 'Latest model smartphone', 500.00, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(5, 'TABLET', 'Tablet', 'High-end tablet device', 300.00, 'INACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);

-- Insert reward_events with composite key
INSERT INTO reward_events (reward_id, event_id, region_id, quantity, today_quantity, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 1, 1, 10, 2, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 1, 1, 20, 4, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 1, 1, 30, 6, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(1, 1, 2, 8, 2, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 1, 2, 15, 3, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 1, 3, 5, 1, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(1, 3, 1, 12, 3, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 3, 2, 25, 5, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(5, 2, 1, 3, 1, 'INACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);

-- Insert golden hours
INSERT INTO golden_hours (id, event_id, region_id, start_time, end_time, multiplier, max_rewards, claimed_rewards, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 1, 1, '2023-06-15 12:00:00', '2023-06-15 14:00:00', 2.0, 50, 10, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 1, 2, '2023-07-01 18:00:00', '2023-07-01 20:00:00', 1.5, 40, 5, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 1, 3, '2023-08-15 15:00:00', '2023-08-15 17:00:00', 2.5, 30, 0, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 3, 1, '2023-10-01 10:00:00', '2023-10-01 12:00:00', 1.8, 35, 0, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(5, 2, 1, '2023-01-23 08:00:00', '2023-01-23 10:00:00', 3.0, 25, 25, 'INACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);

-- Insert spin histories
INSERT INTO spin_histories (id, participant_id, event_id, region_id, spin_time, reward_id, reward_event_id, reward_region_id, win, wheel_position, multiplier, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 1, 1, 1, '2023-06-10 10:30:00', 1, 1, 1, TRUE, 120.5, 1.0, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 1, 1, 1, '2023-06-10 11:45:00', NULL, NULL, NULL, FALSE, 45.2, 1.0, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 2, 1, 3, '2023-06-11 14:22:00', 4, 1, 3, TRUE, 230.7, 2.5, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 3, 1, 2, '2023-06-12 09:15:00', 2, 1, 2, TRUE, 190.3, 1.5, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(5, 3, 1, 2, '2023-06-12 09:20:00', NULL, NULL, NULL, FALSE, 78.9, 1.0, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(6, 4, 4, 3, '2023-12-05 16:40:00', NULL, NULL, NULL, FALSE, 310.5, 1.0, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(7, 5, 1, 3, '2023-06-15 11:30:00', NULL, NULL, NULL, FALSE, 22.8, 1.0, 'INACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);

-- Insert configurations
INSERT INTO configurations (id, config_key, config_value, description, data_type, validation_regex, modifiable, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'MAX_DAILY_SPINS', '5', 'Maximum number of spins allowed per day per participant', 'INTEGER', '^[0-9]+$', TRUE, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 'MIN_VERIFICATION_AGE', '18', 'Minimum age for identity verification', 'INTEGER', '^[0-9]+$', TRUE, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 'DEFAULT_REWARD_PROBABILITY', '0.1', 'Default probability for rewards if not specified', 'DECIMAL', '^0\\.[0-9]+$', TRUE, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 'MAINTENANCE_MODE', 'false', 'Whether system is in maintenance mode', 'BOOLEAN', '^(true|false)$', TRUE, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(5, 'SUPPORT_EMAIL', 'support@example.com', 'Email address for customer support', 'STRING', '^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$', TRUE, 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);

-- Insert blacklisted tokens
INSERT INTO blacklisted_tokens (id, token, token_type, expiration_time, user_id, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2MTcyMzQ1Njd9.expired1', 'ACCESS', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2MTcyMzQ1Njd9.expired2', 'REFRESH', DATEADD('DAY', -2, CURRENT_TIMESTAMP), 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2MTcyMzQ1Njd9.revoked1', 'ACCESS', DATEADD('DAY', +1, CURRENT_TIMESTAMP), 3, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert audit logs
INSERT INTO audit_logs (id, object_type, object_id, property_path, old_value, new_value, value_type, update_time, context, action_type, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'Event', '1', 'name', NULL, 'Summer Lucky Draw 2023', 'String', '2023-05-01 09:15:00', 'EVENT_CREATION', 'CREATED', 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(2, 'User', '5', 'status', 'ACTIVE', 'INACTIVE', 'CommonStatus', '2023-05-15 14:30:00', 'USER_DEACTIVATION', 'UPDATE', 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(3, 'Participant', '1', 'spinsRemaining', '3', '5', 'Integer', '2023-06-10 10:15:00', 'SPIN_ADDITION', 'UPDATE', 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(4, 'Reward', '4', NULL, NULL, NULL, NULL, '2023-04-20 11:45:00', 'REWARD_CREATION', 'CREATED', 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0),
(5, 'Event', '2', 'status', 'ACTIVE', 'INACTIVE', 'CommonStatus', '2023-01-28 00:00:01', 'EVENT_COMPLETION', 'UPDATE', 'ACTIVE', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 0);