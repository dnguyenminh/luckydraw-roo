-- Clear existing data to avoid conflicts
-- Note: Use with caution in production
DELETE FROM spin_histories;
DELETE FROM golden_hours;
DELETE FROM rewards;
DELETE FROM participant_events;
DELETE FROM participants;
DELETE FROM event_locations;
DELETE FROM provinces;
DELETE FROM regions;
DELETE FROM events;
DELETE FROM role_permissions;
DELETE FROM user_roles;
DELETE FROM blacklisted_tokens;
DELETE FROM users;
DELETE FROM roles;
DELETE FROM permissions;
DELETE FROM configurations;
DELETE FROM audit_logs;

-- Insert Regions
INSERT INTO regions (id, created_by, created_at, updated_by, updated_at, status, name, code, description, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'North Region', 'NORTH', 'Northern provinces of the country', 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Central Region', 'CENTRAL', 'Central provinces of the country', 0),
(3, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'South Region', 'SOUTH', 'Southern provinces of the country', 0),
(4, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'INACTIVE', 'East Region', 'EAST', 'Eastern provinces of the country', 0),
(5, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'INACTIVE', 'West Region', 'WEST', 'Western provinces of the country', 0);

-- Insert Provinces
INSERT INTO provinces (id, created_by, created_at, updated_by, updated_at, status, name, code, description, region_id, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Province A', 'PROV_A', 'First province in North Region', 1, 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Province B', 'PROV_B', 'Second province in North Region', 1, 0),
(3, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Province C', 'PROV_C', 'First province in Central Region', 2, 0),
(4, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Province D', 'PROV_D', 'Second province in Central Region', 2, 0),
(5, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Province E', 'PROV_E', 'First province in South Region', 3, 0),
(6, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Province F', 'PROV_F', 'Second province in South Region', 3, 0),
(7, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'INACTIVE', 'Province G', 'PROV_G', 'Inactive province in East Region', 4, 0),
(8, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'INACTIVE', 'Province H', 'PROV_H', 'Inactive province in West Region', 5, 0);

-- Insert Events
INSERT INTO events (id, created_by, created_at, updated_by, updated_at, status, name, code, description, start_time, end_time, version) VALUES
(1, 'admin', '2023-01-15 00:00:00', 'admin', '2023-01-15 00:00:00', 'ACTIVE', 'Summer Festival', 'SUMMER_FEST', 'Annual summer lucky draw event', '2023-06-01 00:00:00', '2023-08-31 23:59:59', 0),
(2, 'admin', '2023-01-15 00:00:00', 'admin', '2023-01-15 00:00:00', 'ACTIVE', 'Winter Wonderland', 'WINTER_FEST', 'Winter promotional event', '2023-12-01 00:00:00', '2024-01-31 23:59:59', 0),
(3, 'admin', '2023-01-15 00:00:00', 'admin', '2023-01-15 00:00:00', 'ACTIVE', 'Spring Celebration', 'SPRING_FEST', 'Spring promotional event', '2023-03-01 00:00:00', '2023-05-31 23:59:59', 0),
(4, 'admin', '2023-01-15 00:00:00', 'admin', '2023-01-15 00:00:00', 'INACTIVE', 'Fall Harvest', 'FALL_FEST', 'Fall promotional event', '2023-09-01 00:00:00', '2023-11-30 23:59:59', 0),
(5, 'admin', '2023-01-15 00:00:00', 'admin', '2023-01-15 00:00:00', 'ACTIVE', 'Anniversary Special', 'ANNIV_SPEC', 'Company anniversary event', '2023-10-15 00:00:00', '2023-10-30 23:59:59', 0);

-- Insert Event Locations
INSERT INTO event_locations (id, created_by, created_at, updated_by, updated_at, status, name, code, description, max_spin, quantity, win_probability, event_id, region_id, version) VALUES
(1, 'admin', '2023-01-20 00:00:00', 'admin', '2023-01-20 00:00:00', 'ACTIVE', 'North Mall', 'NORTH_MALL', 'Shopping mall in North Region', 100, 50, 0.2, 1, 1, 0),
(2, 'admin', '2023-01-20 00:00:00', 'admin', '2023-01-20 00:00:00', 'ACTIVE', 'Central Plaza', 'CENTRAL_PLAZA', 'Shopping plaza in Central Region', 150, 75, 0.15, 1, 2, 0),
(3, 'admin', '2023-01-20 00:00:00', 'admin', '2023-01-20 00:00:00', 'ACTIVE', 'South Center', 'SOUTH_CENTER', 'Shopping center in South Region', 200, 100, 0.1, 1, 3, 0),
(4, 'admin', '2023-01-20 00:00:00', 'admin', '2023-01-20 00:00:00', 'ACTIVE', 'Winter Village', 'WINTER_VILLAGE', 'Winter event location in North Region', 120, 60, 0.25, 2, 1, 0),
(5, 'admin', '2023-01-20 00:00:00', 'admin', '2023-01-20 00:00:00', 'ACTIVE', 'Spring Garden', 'SPRING_GARDEN', 'Spring event location in Central Region', 180, 90, 0.12, 3, 2, 0),
(6, 'admin', '2023-01-20 00:00:00', 'admin', '2023-01-20 00:00:00', 'INACTIVE', 'Fall Market', 'FALL_MARKET', 'Fall event location in South Region', 160, 80, 0.18, 4, 3, 0),
(7, 'admin', '2023-01-20 00:00:00', 'admin', '2023-01-20 00:00:00', 'ACTIVE', 'Anniversary Hall', 'ANNIV_HALL', 'Anniversary event location', 250, 125, 0.3, 5, 1, 0);

-- Insert Participants
INSERT INTO participants (id, created_by, created_at, updated_by, updated_at, status, name, code, phone, address, province_id, version) VALUES
(1, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'ACTIVE', 'John Doe', 'JOHN001', '1234567890', '123 Main St', 1, 0),
(2, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'ACTIVE', 'Jane Smith', 'JANE001', '2345678901', '456 Oak Ave', 1, 0),
(3, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'ACTIVE', 'Robert Johnson', 'ROBERT001', '3456789012', '789 Pine Rd', 2, 0),
(4, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'ACTIVE', 'Emily Davis', 'EMILY001', '4567890123', '101 Maple Dr', 3, 0),
(5, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'ACTIVE', 'Michael Brown', 'MICHAEL001', '5678901234', '202 Elm St', 4, 0),
(6, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'ACTIVE', 'Sarah Wilson', 'SARAH001', '6789012345', '303 Cedar Ln', 5, 0),
(7, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'ACTIVE', 'David Taylor', 'DAVID001', '7890123456', '404 Birch Blvd', 6, 0),
(8, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'INACTIVE', 'Lisa Anderson', 'LISA001', '8901234567', '505 Spruce Way', 7, 0),
(9, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'INACTIVE', 'James Martin', 'JAMES001', '9012345678', '606 Willow Ct', 8, 0),
(10, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'ACTIVE', 'Jennifer Garcia', 'JENNIFER001', '0123456789', '707 Aspen Rd', 1, 0);

-- Insert Participant Events
INSERT INTO participant_events (id, created_by, created_at, updated_by, updated_at, status, event_id, event_location_id, participant_id, spins_remaining, version) VALUES
(1, 'system', '2023-06-05 00:00:00', 'system', '2023-06-05 00:00:00', 'ACTIVE', 1, 1, 1, 5, 0),
(2, 'system', '2023-06-05 00:00:00', 'system', '2023-06-05 00:00:00', 'ACTIVE', 1, 2, 2, 3, 0),
(3, 'system', '2023-06-05 00:00:00', 'system', '2023-06-05 00:00:00', 'ACTIVE', 1, 3, 3, 4, 0),
(4, 'system', '2023-12-05 00:00:00', 'system', '2023-12-05 00:00:00', 'ACTIVE', 2, 4, 4, 6, 0),
(5, 'system', '2023-03-05 00:00:00', 'system', '2023-03-05 00:00:00', 'ACTIVE', 3, 5, 5, 2, 0),
(6, 'system', '2023-09-05 00:00:00', 'system', '2023-09-05 00:00:00', 'INACTIVE', 4, 6, 6, 0, 0),
(7, 'system', '2023-10-16 00:00:00', 'system', '2023-10-16 00:00:00', 'ACTIVE', 5, 7, 7, 8, 0),
(8, 'system', '2023-06-05 00:00:00', 'system', '2023-06-05 00:00:00', 'ACTIVE', 1, 1, 8, 0, 0),
(9, 'system', '2023-12-05 00:00:00', 'system', '2023-12-05 00:00:00', 'INACTIVE', 2, 4, 9, 0, 0),
(10, 'system', '2023-06-05 00:00:00', 'system', '2023-06-05 00:00:00', 'ACTIVE', 1, 3, 10, 7, 0);

-- Insert Rewards
INSERT INTO rewards (id, created_by, created_at, updated_by, updated_at, status, name, code, description, event_location_id, version) VALUES
(1, 'admin', '2023-02-10 00:00:00', 'admin', '2023-02-10 00:00:00', 'ACTIVE', 'Cash Prize $100', 'CASH100', '$100 cash prize', 1, 0),
(2, 'admin', '2023-02-10 00:00:00', 'admin', '2023-02-10 00:00:00', 'ACTIVE', 'Gift Card $50', 'GIFT50', '$50 gift card', 1, 0),
(3, 'admin', '2023-02-10 00:00:00', 'admin', '2023-02-10 00:00:00', 'ACTIVE', 'Smartphone', 'PHONE', 'Latest smartphone', 2, 0),
(4, 'admin', '2023-02-10 00:00:00', 'admin', '2023-02-10 00:00:00', 'ACTIVE', 'Tablet Device', 'TABLET', 'New tablet device', 2, 0),
(5, 'admin', '2023-02-10 00:00:00', 'admin', '2023-02-10 00:00:00', 'ACTIVE', 'Smart Watch', 'WATCH', 'Smart watch', 3, 0),
(6, 'admin', '2023-02-10 00:00:00', 'admin', '2023-02-10 00:00:00', 'ACTIVE', 'Wireless Headphones', 'HEADPHONE', 'Premium wireless headphones', 3, 0),
(7, 'admin', '2023-02-10 00:00:00', 'admin', '2023-02-10 00:00:00', 'ACTIVE', 'Winter Jacket', 'JACKET', 'Warm winter jacket', 4, 0),
(8, 'admin', '2023-02-10 00:00:00', 'admin', '2023-02-10 00:00:00', 'ACTIVE', 'Snow Boots', 'BOOTS', 'Waterproof snow boots', 4, 0),
(9, 'admin', '2023-02-10 00:00:00', 'admin', '2023-02-10 00:00:00', 'ACTIVE', 'Garden Tool Set', 'GARDEN', 'Spring gardening tools', 5, 0),
(10, 'admin', '2023-02-10 00:00:00', 'admin', '2023-02-10 00:00:00', 'INACTIVE', 'Umbrella', 'UMBRELLA', 'Stylish umbrella', 6, 0),
(11, 'admin', '2023-02-10 00:00:00', 'admin', '2023-02-10 00:00:00', 'ACTIVE', 'Anniversary Cake', 'CAKE', 'Celebratory cake', 7, 0),
(12, 'admin', '2023-02-10 00:00:00', 'admin', '2023-02-10 00:00:00', 'ACTIVE', 'Champagne Bottle', 'CHAMPAGNE', 'Premium champagne', 7, 0);

-- Insert Golden Hours
INSERT INTO golden_hours (id, created_by, created_at, updated_by, updated_at, status, event_location_id, start_time, end_time, multiplier, version) VALUES
(1, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 1, '2023-06-15 12:00:00', '2023-06-15 14:00:00', 2.0, 0),
(2, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 1, '2023-07-15 18:00:00', '2023-07-15 20:00:00', 2.0, 0),
(3, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 2, '2023-06-20 13:00:00', '2023-06-20 15:00:00', 2.5, 0),
(4, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 3, '2023-07-25 14:00:00', '2023-07-25 16:00:00', 2.0, 0),
(5, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 4, '2023-12-25 16:00:00', '2023-12-25 18:00:00', 3.0, 0),
(6, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 5, '2023-05-01 15:00:00', '2023-05-01 17:00:00', 2.0, 0),
(7, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'INACTIVE', 6, '2023-10-31 18:00:00', '2023-10-31 20:00:00', 2.5, 0),
(8, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 7, '2023-10-20 19:00:00', '2023-10-20 21:00:00', 3.0, 0);

-- Insert Spin Histories (some wins, some losses)
INSERT INTO spin_histories (id, created_by, created_at, updated_by, updated_at, status, participant_event_id, spin_time, reward_id, win, version) VALUES
(1, 'system', '2023-06-15 12:30:00', 'system', '2023-06-15 12:30:00', 'ACTIVE', 1, '2023-06-15 12:30:00', 1, true, 0),
(2, 'system', '2023-06-15 13:00:00', 'system', '2023-06-15 13:00:00', 'ACTIVE', 1, '2023-06-15 13:00:00', NULL, false, 0),
(3, 'system', '2023-06-15 13:15:00', 'system', '2023-06-15 13:15:00', 'ACTIVE', 1, '2023-06-15 13:15:00', 2, true, 0),
(4, 'system', '2023-06-20 14:00:00', 'system', '2023-06-20 14:00:00', 'ACTIVE', 2, '2023-06-20 14:00:00', 3, true, 0),
(5, 'system', '2023-06-20 14:30:00', 'system', '2023-06-20 14:30:00', 'ACTIVE', 2, '2023-06-20 14:30:00', NULL, false, 0),
(6, 'system', '2023-07-25 15:00:00', 'system', '2023-07-25 15:00:00', 'ACTIVE', 3, '2023-07-25 15:00:00', 5, true, 0),
(7, 'system', '2023-12-25 17:00:00', 'system', '2023-12-25 17:00:00', 'ACTIVE', 4, '2023-12-25 17:00:00', 7, true, 0),
(8, 'system', '2023-05-01 16:00:00', 'system', '2023-05-01 16:00:00', 'ACTIVE', 5, '2023-05-01 16:00:00', NULL, false, 0),
(9, 'system', '2023-10-31 19:00:00', 'system', '2023-10-31 19:00:00', 'INACTIVE', 6, '2023-10-31 19:00:00', 10, true, 0),
(10, 'system', '2023-10-20 20:00:00', 'system', '2023-10-20 20:00:00', 'ACTIVE', 7, '2023-10-20 20:00:00', 11, true, 0),
(11, 'system', '2023-06-16 10:00:00', 'system', '2023-06-16 10:00:00', 'ACTIVE', 1, '2023-06-16 10:00:00', NULL, false, 0),
(12, 'system', '2023-06-16 11:00:00', 'system', '2023-06-16 11:00:00', 'ACTIVE', 1, '2023-06-16 11:00:00', NULL, false, 0),
(13, 'system', '2023-06-21 09:00:00', 'system', '2023-06-21 09:00:00', 'ACTIVE', 2, '2023-06-21 09:00:00', 4, true, 0),
(14, 'system', '2023-07-26 14:00:00', 'system', '2023-07-26 14:00:00', 'ACTIVE', 3, '2023-07-26 14:00:00', 6, true, 0),
(15, 'system', '2023-10-20 19:30:00', 'system', '2023-10-20 19:30:00', 'ACTIVE', 7, '2023-10-20 19:30:00', 12, true, 0);

-- Update audit log entry for SpinHistory
UPDATE audit_logs 
SET property_path = 'win', old_value = NULL, new_value = 'true'
WHERE id = 6 AND object_type = 'SpinHistory';

DELETE FROM audit_logs WHERE id = 7;

-- Insert Permissions
INSERT INTO permissions (id, created_by, created_at, updated_by, updated_at, status, name, description, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'EVENT_CREATE', 'Create new events', 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'EVENT_READ', 'View event details', 0),
(3, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'EVENT_UPDATE', 'Update existing events', 0),
(4, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'EVENT_DELETE', 'Delete events', 0),
(5, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'PARTICIPANT_CREATE', 'Create new participants', 0),
(6, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'PARTICIPANT_READ', 'View participant details', 0),
(7, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'PARTICIPANT_UPDATE', 'Update existing participants', 0),
(8, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'PARTICIPANT_DELETE', 'Delete participants', 0),
(9, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'REWARD_MANAGE', 'Manage rewards', 0),
(10, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'GOLDEN_HOUR_MANAGE', 'Manage golden hours', 0),
(11, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'USER_MANAGE', 'Manage users', 0),
(12, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'ROLE_MANAGE', 'Manage roles', 0),
(13, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'AUDIT_VIEW', 'View audit logs', 0),
(14, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'CONFIG_MANAGE', 'Manage system configuration', 0),
(15, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'SPIN_HISTORY_VIEW', 'View spin history', 0);

-- Insert Roles
INSERT INTO roles (id, created_by, created_at, updated_by, updated_at, status, role_type, description, display_order, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'ROLE_ADMIN', 'System Administrator', 1, 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'ROLE_MANAGER', 'Event Manager', 2, 0),
(3, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'ROLE_PARTICIPANT', 'Participant Manager', 3, 0),
(4, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'ROLE_USER', 'Read-only User', 4, 0),
(5, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'INACTIVE', 'ROLE_GUEST', 'Guest User', 5, 0);

-- Insert Role Permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15),
(2, 1), (2, 2), (2, 3), (2, 4), (2, 9), (2, 10), (2, 15),
(3, 5), (3, 6), (3, 7), (3, 8), (3, 15),
(4, 2), (4, 6), (4, 15),
(5, 2);

-- Insert Users (with BCrypt-encoded passwords - all passwords are "password")
INSERT INTO users (id, created_by, created_at, updated_by, updated_at, status, username, password, email, full_name, role, enabled, account_expired, account_locked, credentials_expired, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'admin', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'admin@example.com', 'Admin User', 'ROLE_ADMIN', true, false, false, false, 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'eventmgr', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'eventmgr@example.com', 'Event Manager', 'ROLE_MANAGER', true, false, false, false, 0),
(3, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'partmgr', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'partmgr@example.com', 'Participant Manager', 'ROLE_PARTICIPANT', true, false, false, false, 0),
(4, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'viewer', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'viewer@example.com', 'Viewer User', 'ROLE_USER', true, false, false, false, 0),
(5, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'INACTIVE', 'guest', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'guest@example.com', 'Guest User', 'ROLE_GUEST', false, true, true, true, 0);

-- Insert User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5);

-- Insert Blacklisted Tokens (expired tokens)
INSERT INTO blacklisted_tokens (id, created_by, created_at, updated_by, updated_at, status, token, token_type, expiration_time, user_id, version) VALUES
(1, 'system', '2023-01-15 00:00:00', 'system', '2023-01-15 00:00:00', 'ACTIVE', 'eyJhbGciOiJIUzI1NiJ9.expiredToken1', 'ACCESS', '2023-01-14 00:00:00', 1, 0),
(2, 'system', '2023-02-15 00:00:00', 'system', '2023-02-15 00:00:00', 'ACTIVE', 'eyJhbGciOiJIUzI1NiJ9.expiredToken2', 'ACCESS', '2023-02-14 00:00:00', 2, 0),
(3, 'system', '2023-03-15 00:00:00', 'system', '2023-03-15 00:00:00', 'ACTIVE', 'eyJhbGciOiJIUzI1NiJ9.expiredToken3', 'ACCESS', '2023-03-14 00:00:00', 3, 0);

-- Insert Configurations
INSERT INTO configurations (id, created_by, created_at, updated_by, updated_at, status, config_key, config_value, description, version) VALUES
(1, 'admin', '2023-01-01 00:00:00', 'admin', '2023-01-01 00:00:00', 'ACTIVE', 'EVENT_MAX_DURATION_DAYS', '90', 'Maximum event duration in days', 0),
(2, 'admin', '2023-01-01 00:00:00', 'admin', '2023-01-01 00:00:00', 'ACTIVE', 'DEFAULT_SPINS_PER_PARTICIPANT', '10', 'Default number of spins allocated per participant', 0),
(3, 'admin', '2023-01-01 00:00:00', 'admin', '2023-01-01 00:00:00', 'ACTIVE', 'MAX_DAILY_SPINS', '5', 'Maximum spins per day per participant', 0),
(4, 'admin', '2023-01-01 00:00:00', 'admin', '2023-01-01 00:00:00', 'ACTIVE', 'TOKEN_EXPIRY_MINUTES', '60', 'Authentication token expiry in minutes', 0),
(5, 'admin', '2023-01-01 00:00:00', 'admin', '2023-01-01 00:00:00', 'ACTIVE', 'REWARD_CLAIM_EXPIRY_DAYS', '7', 'Number of days to claim a reward before it expires', 0);

-- Insert Audit Logs
INSERT INTO audit_logs (id, created_by, created_at, updated_by, updated_at, status, object_type, object_id, property_path, old_value, new_value, value_type, update_time, context, action_type, version) VALUES
(1, 'admin', '2023-01-15 10:00:00', 'admin', '2023-01-15 10:00:00', 'ACTIVE', 'Event', 1, 'name', NULL, 'Summer Festival', 'String', '2023-01-15 10:00:00', 'Event creation', 'CREATED', 0),
(2, 'admin', '2023-01-15 10:30:00', 'admin', '2023-01-15 10:30:00', 'ACTIVE', 'Event', 2, 'name', NULL, 'Winter Wonderland', 'String', '2023-01-15 10:30:00', 'Event creation', 'CREATED', 0),
(3, 'admin', '2023-01-20 11:00:00', 'admin', '2023-01-20 11:00:00', 'ACTIVE', 'EventLocation', 1, 'name', NULL, 'North Mall', 'String', '2023-01-20 11:00:00', 'Location creation', 'CREATED', 0),
(4, 'admin', '2023-02-10 12:00:00', 'admin', '2023-02-10 12:00:00', 'ACTIVE', 'Reward', 1, 'name', NULL, 'Cash Prize $100', 'String', '2023-02-10 12:00:00', 'Reward creation', 'CREATED', 0),
(5, 'system', '2023-02-01 09:00:00', 'system', '2023-02-01 09:00:00', 'ACTIVE', 'Participant', 1, 'name', NULL, 'John Doe', 'String', '2023-02-01 09:00:00', 'Participant creation', 'CREATED', 0),
(6, 'admin', '2023-06-15 12:30:00', 'admin', '2023-06-15 12:30:00', 'ACTIVE', 'SpinHistory', 1, 'win', NULL, 'true', 'Boolean', '2023-06-15 12:30:00', 'Spin result', 'CREATED', 0),
(8, 'admin', '2023-01-01 08:00:00', 'admin', '2023-01-01 08:00:00', 'ACTIVE', 'User', 1, 'username', NULL, 'admin', 'String', '2023-01-01 08:00:00', 'User creation', 'CREATED', 0),
(9, 'admin', '2023-03-15 15:00:00', 'admin', '2023-03-15 15:00:00', 'ACTIVE', 'GoldenHour', 5, 'multiplier', NULL, '3.0', 'BigDecimal', '2023-03-15 15:00:00', 'Golden hour creation', 'CREATED', 0),
(10, 'admin', '2023-09-05 16:00:00', 'admin', '2023-09-05 16:00:00', 'ACTIVE', 'Event', 4, 'status', 'ACTIVE', 'INACTIVE', 'CommonStatus', '2023-09-05 16:00:00', 'Event deactivated', 'DEACTIVATED', 0);
