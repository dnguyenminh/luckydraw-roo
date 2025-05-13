-- Test data for service tests - minimal dataset specifically for unit tests

-- Insert Regions
INSERT INTO regions (id, name, code, status, created_by, created_at, updated_by, updated_at, version) VALUES 
(1, 'North Region', 'NORTH', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'Central Region', 'CENTRAL', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'South Region', 'SOUTH', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 'Test Region', 'TEST', 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert Provinces
INSERT INTO provinces (id, name, code, status, created_by, created_at, updated_by, updated_at, version) VALUES 
(1, 'Hanoi Province', 'HANOI', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'Danang Province', 'DANANG', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'Ho Chi Minh Province', 'HCMC', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 'Test Province', 'TEST', 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Link provinces to regions
INSERT INTO region_province (region_id, province_id) VALUES 
(1, 1), -- Hanoi in North
(2, 2), -- Danang in Central
(3, 3), -- HCMC in South
(4, 4); -- Test-Test

-- Insert Events
INSERT INTO events (id, name, code, description, start_time, end_time, status, created_by, created_at, updated_by, updated_at, version) VALUES 
(1, 'New Year Event', 'NYE2023', 'New Year Celebration', '2023-12-25 00:00:00', '2024-01-10 23:59:59', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'Summer Festival', 'SUMMER2023', 'Summer Celebration', '2023-06-01 00:00:00', '2023-08-31 23:59:59', 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'Test Event', 'TEST', 'Test Event', CURRENT_TIMESTAMP, DATEADD('DAY', 30, CURRENT_TIMESTAMP), 'DRAFT', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert Event Locations
INSERT INTO event_locations (event_id, region_id, name, status, description, created_by, created_at, updated_by, updated_at, version, max_spin, today_spin, daily_spin_dist_rate) VALUES 
(1, 1, 'New Year North', 'ACTIVE', 'New Year Event in North', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0, 100, 50, 0.1),
(1, 2, 'New Year Central', 'ACTIVE', 'New Year Event in Central', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0, 100, 30, 0.1),
(1, 3, 'New Year South', 'ACTIVE', 'New Year Event in South', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0, 100, 40, 0.1),
(2, 1, 'Summer North', 'INACTIVE', 'Summer Festival in North', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0, 200, 0, 0.2),
(3, 4, 'Test Location', 'DRAFT', 'Test Event Location', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0, 50, 0, 0.5);

-- Insert Participants
INSERT INTO participants (id, name, code, phone, email, address, province_id, status, created_by, created_at, updated_by, updated_at, version, last_adding_spin) VALUES 
(1, 'John Doe', 'JOHN001', '1234567890', 'john@example.com', '123 Hanoi Street, Ba Dinh District', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0, 0),
(2, 'Jane Smith', 'JANE002', '9876543210', 'jane@example.com', '456 Da Nang Avenue, Hai Chau District', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0, 0),
(3, 'Bob Johnson', 'BOB003', '5555555555', 'bob@example.com', '789 Nguyen Hue Street, District 1, HCMC', 3, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0, 0),
(4, 'Test User', 'TEST004', '1111111111', 'test@example.com', '100 Test Street, Test District', 4, 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0, 0);

-- Insert Rewards
INSERT INTO rewards (id, name, code, description, status, created_by, created_at, updated_by, updated_at, version, prize_value) VALUES 
(1, 'Cash Voucher', 'CASH100', '100 USD Cash Voucher', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0, 100.00),
(2, 'Gift Card', 'GIFT50', '50 USD Gift Card', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0, 50.00),
(3, 'Free Ticket', 'TICKET', 'Free Movie Ticket', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0, 15.00),
(4, 'Test Reward', 'TEST', 'Test Reward', 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0, 0.00);

-- Insert Reward Events
INSERT INTO reward_events (reward_id, event_id, region_id, quantity, today_quantity, probability, status, created_by, created_at, updated_by, updated_at, version) VALUES 
(1, 1, 1, 10, 5, 0.1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 1, 1, 20, 10, 0.2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 1, 3, 50, 25, 0.5, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 3, 4, 5, 0, 0.01, 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert Participant Events
INSERT INTO participant_events (participant_id, event_id, region_id, spins_remaining, status, created_by, created_at, updated_by, updated_at, version) VALUES 
(1, 1, 1, 5, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 1, 2, 3, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 1, 3, 7, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(1, 2, 1, 0, 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 3, 4, 1, 'DRAFT', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert Golden Hours
INSERT INTO golden_hours (id, event_id, region_id, start_time, end_time, multiplier, status, max_rewards, claimed_rewards, created_by, created_at, updated_by, updated_at, version) VALUES 
(1, 1, 1, '2023-12-31 20:00:00', '2023-12-31 22:00:00', 2.0, 'ACTIVE', 50, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 1, 2, '2023-12-31 19:00:00', '2023-12-31 21:00:00', 2.0, 'ACTIVE', 40, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 1, 3, '2023-12-31 18:00:00', '2023-12-31 20:00:00', 2.0, 'ACTIVE', 60, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 3, 4, CURRENT_TIMESTAMP, DATEADD('HOUR', 2, CURRENT_TIMESTAMP), 3.0, 'DRAFT', 10, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert Spin Histories
INSERT INTO spin_histories (id, participant_id, participant_event_id, participant_region_id, spin_time, reward_id, reward_event_id, reward_region_id, golden_hour_id, win, wheel_position, multiplier, status, created_by, created_at, updated_by, updated_at, version) VALUES 
(1, 1, 1, 1, '2023-12-30 10:15:00', 1, 1, 1, 1, true, 120.5, 2.0, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 1, 1, 1, '2023-12-30 11:30:00', null, null, null, null, false, 45.2, 1.0, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 2, 1, 2, '2023-12-29 14:45:00', 2, 1, 1, null, true, 230.7, 1.0, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 4, 3, 4, CURRENT_TIMESTAMP, null, null, null, null, false, 78.3, 1.0, 'DRAFT', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert Roles
INSERT INTO roles (id, role_type, description, display_order, status, created_by, created_at, updated_by, updated_at, version) VALUES 
(1, 'ROLE_ADMIN', 'Administrator', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'ROLE_MANAGER', 'Manager', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'ROLE_USER', 'User', 3, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 'ROLE_TESTER', 'Tester', 4, 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert Permissions
INSERT INTO permissions (id, name, permission_type, description, status, created_by, created_at, updated_by, updated_at, version) VALUES 
(1, 'CREATE_USER', 'WRITE', 'Create Users', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'READ_USER', 'READ', 'Read Users', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'UPDATE_USER', 'WRITE', 'Update Users', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 'DELETE_USER', 'WRITE', 'Delete Users', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(5, 'CREATE_EVENT', 'WRITE', 'Create Events', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(6, 'READ_EVENT', 'READ', 'Read Events', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(7, 'UPDATE_EVENT', 'WRITE', 'Update Events', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(8, 'DELETE_EVENT', 'WRITE', 'Delete Events', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(9, 'TEST_PERMISSION', 'SPECIAL', 'Test Permission', 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert Role Permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES 
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), -- ADMIN has all perms
(2, 2), (2, 5), (2, 6), (2, 7), -- MANAGER has read user, create/read/update event
(3, 2), (3, 6), -- USER has read user, read event
(4, 9); -- TESTER has test permission 

-- Insert Users
INSERT INTO users (id, username, password, email, full_name, role_id, status, created_by, created_at, updated_by, updated_at, version) VALUES 
(1, 'admin', '$2a$10$rGH6UgFe.5vYIHCR7XYpqO5Ia5S.DX57Ef.uKwnVlX5zYoZy5/JcO', 'admin@example.com', 'Admin User', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'manager', '$2a$10$rGH6UgFe.5vYIHCR7XYpqO5Ia5S.DX57Ef.uKwnVlX5zYoZy5/JcO', 'manager@example.com', 'Manager User', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'user', '$2a$10$rGH6UgFe.5vYIHCR7XYpqO5Ia5S.DX57Ef.uKwnVlX5zYoZy5/JcO', 'user@example.com', 'Normal User', 3, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 'tester', '$2a$10$rGH6UgFe.5vYIHCR7XYpqO5Ia5S.DX57Ef.uKwnVlX5zYoZy5/JcO', 'test@example.com', 'Test User', 4, 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(5, 'inactive', '$2a$10$rGH6UgFe.5vYIHCR7XYpqO5Ia5S.DX57Ef.uKwnVlX5zYoZy5/JcO', 'inactive@example.com', 'Inactive User', 3, 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert Blacklisted Tokens
INSERT INTO blacklisted_tokens (id, token, token_type, user_id, expiration_time, status, created_by, created_at, updated_by, updated_at, version) VALUES 
(1, 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.KRXr0h5CnvK', 'ACCESS', 3, DATEADD('HOUR', 1, CURRENT_TIMESTAMP), 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlcjIifQ.X', 'ACCESS', 3, DATEADD('HOUR', -1, CURRENT_TIMESTAMP), 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiJ9.Y', 'ACCESS', 1, DATEADD('HOUR', 2, CURRENT_TIMESTAMP), 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert Audit Logs
INSERT INTO audit_logs (id, object_type, object_id, property_path, old_value, new_value, value_type, action_type, update_time, context, status, created_by, created_at, updated_by, updated_at, version) VALUES 
(1, 'User', '1', 'username', 'oldUser', 'admin', 'String', 'MODIFIED', CURRENT_TIMESTAMP, 'User modification', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'User', '2', 'email', 'old@example.com', 'manager@example.com', 'String', 'MODIFIED', CURRENT_TIMESTAMP, 'User modification', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'Role', '1', 'name', 'USER', 'ADMIN', 'String', 'MODIFIED', CURRENT_TIMESTAMP, 'Role modification', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 'Event', '1', 'status', 'DRAFT', 'ACTIVE', 'CommonStatus', CURRENT_TIMESTAMP, 'Event activation', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(5, 'Participant', '4', 'status', 'ACTIVE', 'INACTIVE', 'CommonStatus', CURRENT_TIMESTAMP, 'Participant deactivation', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert Configurations
INSERT INTO configurations (id, config_key, config_value, description, data_type, validation_regex, modifiable, status, created_by, created_at, updated_by, updated_at, version) VALUES 
(1, 'MAX_DAILY_SPINS', '10', 'Maximum spins allowed per day per participant', 'INTEGER', '^[0-9]+$', true, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'DEFAULT_GOLDEN_HOUR_MULTIPLIER', '2', 'Default multiplier for golden hour', 'DOUBLE', '^[0-9]+(\\.[0-9]+)?$', true, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 'SYSTEM_MAINTENANCE_MODE', 'false', 'System maintenance mode flag', 'BOOLEAN', '^(true|false)$', true, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 'TEST_CONFIG', 'test_value', 'Test configuration', 'STRING', '^[a-z_]+$', true, 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
