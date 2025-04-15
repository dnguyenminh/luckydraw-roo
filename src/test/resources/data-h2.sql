-- Basic data initialization for H2 embedded database

-- Insert admin user with password 'admin'
INSERT INTO users (id, username, email, password, full_name, enabled, status, created_at, created_by)
VALUES (1, 'admin', 'admin@example.com', '$2a$10$rTm9rUKtnYOsgXPFnQ9IcuM4sBYNHT3IOlMBUvRM5eJ9kPmKtJ3L2', 
        'System Administrator', true, 'ACTIVE', CURRENT_TIMESTAMP, 'system');

-- Insert test user with password 'password'
INSERT INTO users (id, username, email, password, full_name, enabled, status, created_at, created_by)
VALUES (2, 'user', 'user@example.com', '$2a$10$rTm9rUKtnYOsgXPFnQ9IcuM4sBYNHT3IOlMBUvRM5eJ9kPmKtJ3L2', 
        'Test User', true, 'ACTIVE', CURRENT_TIMESTAMP, 'system');

-- Insert basic roles
INSERT INTO roles (id, name, code, role_type, status, created_at, created_by)
VALUES 
(1, 'Administrator', 'ADMIN', 'ROLE_ADMIN', 'ACTIVE', CURRENT_TIMESTAMP, 'system'),
(2, 'User', 'USER', 'ROLE_USER', 'ACTIVE', CURRENT_TIMESTAMP, 'system');

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) 
VALUES (1, 1), (2, 2);

-- Insert base permissions
INSERT INTO permissions (id, name, code, status, created_at, created_by)
VALUES 
(1, 'View Dashboard', 'VIEW_DASHBOARD', 'ACTIVE', CURRENT_TIMESTAMP, 'system'),
(2, 'Manage Users', 'MANAGE_USERS', 'ACTIVE', CURRENT_TIMESTAMP, 'system'),
(3, 'Manage Events', 'MANAGE_EVENTS', 'ACTIVE', CURRENT_TIMESTAMP, 'system'),
(4, 'View Events', 'VIEW_EVENTS', 'ACTIVE', CURRENT_TIMESTAMP, 'system'),
(5, 'Participate in Events', 'PARTICIPATE_EVENTS', 'ACTIVE', CURRENT_TIMESTAMP, 'system');

-- Assign permissions to roles
INSERT INTO role_permissions (role_id, permission_id)
VALUES 
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), -- Admin has all permissions
(2, 1), (2, 4), (2, 5);                  -- User has basic permissions

-- Insert regions
INSERT INTO regions (id, name, code, status, created_at, created_by)
VALUES 
(1, 'North', 'NORTH', 'ACTIVE', CURRENT_TIMESTAMP, 'system'),
(2, 'South', 'SOUTH', 'ACTIVE', CURRENT_TIMESTAMP, 'system'),
(3, 'Central', 'CENTRAL', 'ACTIVE', CURRENT_TIMESTAMP, 'system');

-- Insert provinces
INSERT INTO provinces (id, name, code, region_id, status, created_at, created_by)
VALUES 
(1, 'Hanoi', 'HAN', 1, 'ACTIVE', CURRENT_TIMESTAMP, 'system'),
(2, 'Ho Chi Minh City', 'HCM', 2, 'ACTIVE', CURRENT_TIMESTAMP, 'system'),
(3, 'Da Nang', 'DNG', 3, 'ACTIVE', CURRENT_TIMESTAMP, 'system');

-- Insert events (past, current, and future)
INSERT INTO events (id, name, code, description, start_time, end_time, status, created_at, created_by)
VALUES 
-- Past event
(1, 'Spring Festival 2022', 'SPRING-2022', 'Spring celebration festival', 
   '2022-03-01 00:00:00', '2022-03-15 23:59:59', 'COMPLETED', '2022-01-15 10:00:00', 'admin'),
-- Current event
(2, 'Summer Promotion 2023', 'SUMMER-2023', 'Summer promotional event', 
   DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('DAY', 25, CURRENT_TIMESTAMP), 'ACTIVE', 
   DATEADD('DAY', -30, CURRENT_TIMESTAMP), 'admin'),
-- Future event
(3, 'Winter Celebration 2023', 'WINTER-2023', 'End of year celebration', 
   DATEADD('MONTH', 2, CURRENT_TIMESTAMP), DATEADD('MONTH', 3, CURRENT_TIMESTAMP), 'PENDING', 
   DATEADD('DAY', -10, CURRENT_TIMESTAMP), 'admin');

-- Insert event locations
INSERT INTO event_locations (id, name, address, province_id, event_id, status, created_at, created_by)
VALUES 
(1, 'Central Park', '123 Central St', 1, 1, 'ACTIVE', '2022-01-15 10:30:00', 'admin'),
(2, 'City Square Mall', '456 Commerce Rd', 2, 2, 'ACTIVE', DATEADD('DAY', -30, CURRENT_TIMESTAMP), 'admin'),
(3, 'Beach Resort', '789 Coastal Way', 3, 2, 'ACTIVE', DATEADD('DAY', -30, CURRENT_TIMESTAMP), 'admin'),
(4, 'Mountain Retreat', '101 Highland Ave', 1, 3, 'ACTIVE', DATEADD('DAY', -10, CURRENT_TIMESTAMP), 'admin');

-- Insert golden hours
INSERT INTO golden_hours (id, event_id, start_time, end_time, multiplier, status, created_at, created_by)
VALUES 
(1, 2, DATEADD('HOUR', 1, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, CURRENT_TIMESTAMP), 2.0, 'ACTIVE', 
   DATEADD('DAY', -30, CURRENT_TIMESTAMP), 'admin'),
(2, 2, DATEADD('DAY', 1, CURRENT_TIMESTAMP), DATEADD('DAY', 1, DATEADD('HOUR', 2, CURRENT_TIMESTAMP)), 1.5, 
   'ACTIVE', DATEADD('DAY', -30, CURRENT_TIMESTAMP), 'admin');

-- Insert rewards
INSERT INTO rewards (id, event_id, name, description, quantity, remaining, probability, status, created_at, created_by)
VALUES 
(1, 2, 'First Prize', 'Grand prize reward', 5, 5, 0.05, 'ACTIVE', DATEADD('DAY', -30, CURRENT_TIMESTAMP), 'admin'),
(2, 2, 'Second Prize', 'Runner-up reward', 10, 10, 0.10, 'ACTIVE', DATEADD('DAY', -30, CURRENT_TIMESTAMP), 'admin'),
(3, 2, 'Consolation Prize', 'Consolation reward', 100, 100, 0.50, 'ACTIVE', DATEADD('DAY', -30, CURRENT_TIMESTAMP), 'admin');

-- Insert basic system configurations
INSERT INTO configurations (id, name, config_value, description, status, created_at, created_by)
VALUES 
(1, 'MAX_DAILY_SPINS', '3', 'Maximum number of spins allowed per day per participant', 'ACTIVE', CURRENT_TIMESTAMP, 'system'),
(2, 'SYSTEM_MAINTENANCE_MODE', 'false', 'System maintenance mode flag', 'ACTIVE', CURRENT_TIMESTAMP, 'system'),
(3, 'DEFAULT_GOLDEN_HOUR_MULTIPLIER', '2', 'Default multiplier for golden hours', 'ACTIVE', CURRENT_TIMESTAMP, 'system');

-- Set sequences to continue after our inserts
ALTER SEQUENCE users_id_seq RESTART WITH 3;
ALTER SEQUENCE role_id_seq RESTART WITH 3;
ALTER SEQUENCE permission_id_seq RESTART WITH 6;
ALTER SEQUENCE region_id_seq RESTART WITH 4;
ALTER SEQUENCE province_id_seq RESTART WITH 4;
ALTER SEQUENCE event_id_seq RESTART WITH 4;
ALTER SEQUENCE event_location_id_seq RESTART WITH 5;
ALTER SEQUENCE golden_hour_id_seq RESTART WITH 3;
ALTER SEQUENCE reward_id_seq RESTART WITH 4;
ALTER SEQUENCE configuration_id_seq RESTART WITH 4;
