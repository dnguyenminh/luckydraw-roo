-- Clear existing data to avoid conflicts
-- Note: Use with caution in production
DELETE FROM spin_histories;
DELETE FROM golden_hours;
DELETE FROM rewards;
DELETE FROM participant_events;
DELETE FROM participants;
DELETE FROM event_locations;
DELETE FROM region_province;
DELETE FROM provinces;
DELETE FROM events;
DELETE FROM regions;
DELETE FROM role_permissions;
DELETE FROM user_roles;
DELETE FROM blacklisted_tokens;
DELETE FROM users;
DELETE FROM permissions;
DELETE FROM roles;
DELETE FROM configurations;
DELETE FROM audit_logs;

-- Insert Regions
INSERT INTO regions (id, created_by, created_at, updated_by, updated_at, status, name, code, description, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'North Region', 'NORTH', 'Northern provinces of the country', 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Central Region', 'CENTRAL', 'Central provinces of the country', 0),
(3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'South Region', 'SOUTH', 'Southern provinces of the country', 0),
(4, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'INACTIVE', 'East Region', 'EAST', 'Eastern provinces of the country', 0),
(5, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'INACTIVE', 'West Region', 'WEST', 'Western provinces of the country', 0);

-- Insert Provinces
INSERT INTO provinces (id, created_by, created_at, updated_by, updated_at, status, name, code, description, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Province A', 'PROV_A', 'First province in North Region', 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Province B', 'PROV_B', 'Second province in North Region', 0),
(3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Province C', 'PROV_C', 'First province in Central Region', 0),
(4, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Province D', 'PROV_D', 'Second province in Central Region', 0),
(5, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Province E', 'PROV_E', 'First province in South Region', 0),
(6, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Province F', 'PROV_F', 'Second province in South Region', 0),
(7, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'INACTIVE', 'Province G', 'PROV_G', 'Inactive province in East Region', 0),
(8, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'INACTIVE', 'Province H', 'PROV_H', 'Inactive province in West Region', 0);

-- Insert Region-Province relationships
INSERT INTO region_province (province_id, region_id) VALUES
(1, 1), (2, 1),  -- Provinces A, B belong to North Region
(3, 2), (4, 2),  -- Provinces C, D belong to Central Region
(5, 3), (6, 3),  -- Provinces E, F belong to South Region
(7, 4),          -- Province G belongs to East Region
(8, 5);          -- Province H belongs to West Region

-- Insert Events
INSERT INTO events (id, created_by, created_at, updated_by, updated_at, status, name, code, description, start_time, end_time, version) VALUES
(1, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Summer Festival', 'SUMMER_FEST', 'Annual summer lucky draw event', '2023-06-01 00:00:00', '2023-08-31 23:59:59', 0),
(2, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Winter Wonderland', 'WINTER_FEST', 'Winter promotional event', '2023-12-01 00:00:00', '2024-01-31 23:59:59', 0),
(3, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Spring Celebration', 'SPRING_FEST', 'Spring promotional event', '2023-03-01 00:00:00', '2023-05-31 23:59:59', 0),
(4, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'INACTIVE', 'Fall Harvest', 'FALL_FEST', 'Fall promotional event', '2023-09-01 00:00:00', '2023-11-30 23:59:59', 0),
(5, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Anniversary Special', 'ANNIV_SPEC', 'Company anniversary event', '2023-10-15 00:00:00', '2023-10-30 23:59:59', 0);

-- Insert Event Locations
INSERT INTO event_locations (event_id, region_id, province_id, created_by, created_at, updated_by, updated_at, status, name, code, description, max_spin, quantity, win_probability, version) VALUES
(1, 1, 1, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'North Mall', 'NORTH_MALL', 'Shopping mall in North Region', 100, 50, 0.2, 0),
(1, 2, 3, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Central Plaza', 'CENTRAL_PLAZA', 'Shopping plaza in Central Region', 150, 75, 0.15, 0),
(1, 3, 5, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'South Center', 'SOUTH_CENTER', 'Shopping center in South Region', 200, 100, 0.1, 0),
(2, 1, 2, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Winter Village', 'WINTER_VILLAGE', 'Winter event location', 120, 60, 0.25, 0),
(3, 2, 4, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Spring Garden', 'SPRING_GARDEN', 'Spring event location', 180, 90, 0.12, 0),
(4, 3, 6, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'INACTIVE', 'Fall Market', 'FALL_MARKET', 'Fall event location', 160, 80, 0.18, 0),
(5, 1, 1, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Anniversary Hall', 'ANNIV_HALL', 'Anniversary event location', 250, 125, 0.3, 0);

-- Insert Participants
INSERT INTO participants (id, created_by, created_at, updated_by, updated_at, status, name, code, phone, address, last_adding_spin, province_id, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'John Doe', 'JOHN001', '1234567890', '123 Main St', 0, 1, 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Jane Smith', 'JANE001', '2345678901', '456 Oak Ave', 0, 1, 0),
(3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Robert Johnson', 'ROBERT001', '3456789012', '789 Pine Rd', 0, 2, 0),
(4, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Emily Davis', 'EMILY001', '4567890123', '101 Maple Dr', 0, 3, 0),
(5, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Michael Brown', 'MICHAEL001', '5678901234', '202 Elm St', 0, 4, 0),
(6, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Sarah Wilson', 'SARAH001', '6789012345', '303 Cedar Ln', 0, 5, 0),
(7, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'David Taylor', 'DAVID001', '7890123456', '404 Birch Blvd', 0, 6, 0),
(8, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'INACTIVE', 'Lisa Anderson', 'LISA001', '8901234567', '505 Spruce Way', 0, 7, 0),
(9, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'INACTIVE', 'James Martin', 'JAMES001', '9012345678', '606 Willow Ct', 0, 8, 0),
(10, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Jennifer Garcia', 'JENNIFER001', '0123456789', '707 Aspen Rd', 0, 1, 0);

-- Insert Participant Events
INSERT INTO participant_events (event_id, region_id, participant_id, created_by, created_at, updated_by, updated_at, status, spins_remaining, version) VALUES
(1, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 5, 0),
(1, 2, 2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 3, 0),
(1, 3, 3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 4, 0),
(2, 1, 4, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 6, 0),
(3, 2, 5, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 2, 0),
(4, 3, 6, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'INACTIVE', 0, 0),
(5, 1, 7, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 8, 0),
(1, 1, 8, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 0, 0),
(2, 1, 9, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'INACTIVE', 0, 0),
(1, 3, 10, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 7, 0);

-- Insert Rewards
INSERT INTO rewards (id, created_by, created_at, updated_by, updated_at, status, name, code, description, prize_value, event_id, region_id, version) VALUES
(1, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Cash Prize $100', 'CASH100', '$100 cash prize', 100.00, 1, 1, 0),
(2, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Gift Card $50', 'GIFT50', '$50 gift card', 50.00, 1, 1, 0),
(3, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Smartphone', 'PHONE', 'Latest smartphone', 800.00, 1, 2, 0),
(4, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Tablet Device', 'TABLET', 'New tablet device', 400.00, 1, 2, 0),
(5, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Smart Watch', 'WATCH', 'Smart watch', 250.00, 1, 3, 0),
(6, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Wireless Headphones', 'HEADPHONE', 'Premium wireless headphones', 150.00, 1, 3, 0),
(7, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Winter Jacket', 'JACKET', 'Warm winter jacket', 100.00, 2, 1, 0),
(8, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Snow Boots', 'BOOTS', 'Waterproof snow boots', 80.00, 2, 1, 0),
(9, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Garden Tool Set', 'GARDEN', 'Spring gardening tools', 90.00, 3, 2, 0),
(10, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'INACTIVE', 'Umbrella', 'UMBRELLA', 'Stylish umbrella', 25.00, 4, 3, 0),
(11, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Anniversary Cake', 'CAKE', 'Celebratory cake', 40.00, 5, 1, 0),
(12, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Champagne Bottle', 'CHAMPAGNE', 'Premium champagne', 60.00, 5, 1, 0);

-- Insert Golden Hours
INSERT INTO golden_hours (id, created_by, created_at, updated_by, updated_at, status, event_id, region_id, start_time, end_time, multiplier, version) VALUES
(1, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 1, 1, '2023-06-15 12:00:00', '2023-06-15 14:00:00', 2.0, 0),
(2, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 1, 2, '2023-07-15 18:00:00', '2023-07-15 20:00:00', 2.0, 0),
(3, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 1, 3, '2023-07-25 14:00:00', '2023-07-25 16:00:00', 2.0, 0),
(4, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 2, 1, '2023-12-25 16:00:00', '2023-12-25 18:00:00', 3.0, 0),
(5, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 3, 2, '2023-05-01 15:00:00', '2023-05-01 17:00:00', 2.0, 0),
(6, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'INACTIVE', 4, 3, '2023-10-31 18:00:00', '2023-10-31 20:00:00', 2.5, 0),
(7, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 5, 1, '2023-10-20 19:00:00', '2023-10-20 21:00:00', 3.0, 0);

-- Insert Spin Histories
INSERT INTO spin_histories (id, created_by, created_at, updated_by, updated_at, status, event_id, region_id, participant_id, spin_time, reward_id, win, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 1, 1, 1, '2023-06-15 12:30:00', 1, true, 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 1, 1, 1, '2023-06-15 13:00:00', NULL, false, 0),
(3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 1, 1, 1, '2023-06-15 13:15:00', 2, true, 0),
(4, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 1, 2, 2, '2023-06-20 14:00:00', 3, true, 0),
(5, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 1, 2, 2, '2023-06-20 14:30:00', NULL, false, 0),
(6, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 1, 3, 3, '2023-07-25 15:00:00', 5, true, 0),
(7, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 2, 1, 4, '2023-12-25 17:00:00', 7, true, 0),
(8, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 3, 2, 5, '2023-05-01 16:00:00', NULL, false, 0),
(9, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'INACTIVE', 4, 3, 6, '2023-10-31 19:00:00', 10, true, 0),
(10, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 5, 1, 7, '2023-10-20 20:00:00', 11, true, 0);

-- Insert Roles
INSERT INTO roles (id, created_by, created_at, updated_by, updated_at, status, role_type, description, display_order, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_ADMIN', 'System Administrator', 1, 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_USER', 'Regular User', 2, 0);

-- Insert Permissions
INSERT INTO permissions (id, created_by, created_at, updated_by, updated_at, status, name, permission_type, description, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'CREATE_USER', 'WRITE', 'Create user accounts', 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'READ_USER', 'READ', 'View user accounts', 0),
(3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'UPDATE_EVENT', 'WRITE', 'Update events', 0);

-- Insert Users
INSERT INTO users (id, created_by, created_at, updated_by, updated_at, status, username, password, email, full_name, role_id, version) VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'admin', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'admin@example.com', 'Admin User', 1, 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'user', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'user@example.com', 'Regular User', 2, 0);

-- Insert Role Permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), -- Admin has all permissions
(2, 2);                  -- User has read permission only

-- Insert User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),
(2, 2);

-- Insert Configurations
INSERT INTO configurations (id, created_by, created_at, updated_by, updated_at, status, config_key, config_value, description, version) VALUES
(1, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'EVENT_MAX_DURATION_DAYS', '90', 'Maximum event duration in days', 0),
(2, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'MAX_DAILY_SPINS', '5', 'Maximum spins per day per participant', 0),
(3, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'TOKEN_EXPIRY_MINUTES', '60', 'Authentication token expiry in minutes', 0);

-- Insert Audit Logs
INSERT INTO audit_logs (id, created_by, created_at, updated_by, updated_at, status, object_type, object_id, property_path, old_value, new_value, value_type, update_time, context, action_type, version) VALUES
(1, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Event', '1', 'name', NULL, 'Summer Festival', 'String', CURRENT_TIMESTAMP, 'Event creation', 'CREATED', 0),
(2, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Reward', '1', 'prize_value', NULL, '100.00', 'BigDecimal', CURRENT_TIMESTAMP, 'Reward creation', 'CREATED', 0),
(3, 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'ACTIVE', 'Event', '4', 'status', 'ACTIVE', 'INACTIVE', 'CommonStatus', CURRENT_TIMESTAMP, 'Event deactivated', 'DEACTIVATED', 0);
