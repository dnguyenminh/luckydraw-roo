-- Test data for service tests

-- Insert Regions
INSERT INTO regions (id, created_by, created_at, updated_by, updated_at, status, name, code, description, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'North Region', 'NORTH', 'Northern provinces of the country', 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'South Region', 'SOUTH', 'Southern provinces of the country', 0),
(3, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Central Region', 'CENTRAL', 'Central provinces of the country', 0);

-- Insert Provinces
INSERT INTO provinces (id, created_by, created_at, updated_by, updated_at, status, name, code, description, region_id, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Hanoi', 'HN', 'Capital city', 1, 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Ho Chi Minh', 'HCM', 'Southern metropolitan city', 2, 0),
(3, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'Da Nang', 'DN', 'Central coast city', 3, 0);

-- Insert Events
INSERT INTO events (id, created_by, created_at, updated_by, updated_at, status, name, code, description, start_time, end_time, version) VALUES
(1, 'admin', '2023-01-15 00:00:00', 'admin', '2023-01-15 00:00:00', 'ACTIVE', 'Summer Festival', 'SUMMER_FEST', 'Annual summer lucky draw event', '2023-06-01 00:00:00', '2023-08-31 23:59:59', 0),
(2, 'admin', '2023-01-15 00:00:00', 'admin', '2023-01-15 00:00:00', 'ACTIVE', 'Winter Wonderland', 'WINTER_FEST', 'Winter promotional event', '2023-12-01 00:00:00', '2024-01-31 23:59:59', 0),
(3, 'admin', '2023-01-15 00:00:00', 'admin', '2023-01-15 00:00:00', 'ACTIVE', 'Spring Celebration', 'SPRING_FEST', 'Spring promotional event', '2023-03-01 00:00:00', '2023-05-31 23:59:59', 0);

-- Insert Event Locations
INSERT INTO event_locations (event_id, region_id, created_by, created_at, updated_by, updated_at, status, name, code, description, max_spin, quantity, win_probability, version) VALUES
(1, 1, 'admin', '2023-01-20 00:00:00', 'admin', '2023-01-20 00:00:00', 'ACTIVE', 'North Mall', 'NORTH_MALL', 'Shopping mall in North Region', 5, 50, 0.2, 0),
(1, 2, 'admin', '2023-01-20 00:00:00', 'admin', '2023-01-20 00:00:00', 'ACTIVE', 'South Plaza', 'SOUTH_PLAZA', 'Shopping plaza in South Region', 5, 60, 0.15, 0),
(2, 1, 'admin', '2023-01-20 00:00:00', 'admin', '2023-01-20 00:00:00', 'ACTIVE', 'Winter Village', 'WINTER_VILLAGE', 'Winter event in North Region', 3, 40, 0.25, 0);

-- Insert Participants
INSERT INTO participants (id, created_by, created_at, updated_by, updated_at, status, name, code, phone, address, province_id, version) VALUES
(1, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'ACTIVE', 'John Doe', 'JOHN001', '1234567890', '123 Main St', 1, 0),
(2, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'ACTIVE', 'Jane Smith', 'JANE001', '2345678901', '456 Oak Ave', 2, 0),
(3, 'system', '2023-02-01 00:00:00', 'system', '2023-02-01 00:00:00', 'ACTIVE', 'Robert Johnson', 'ROBERT001', '3456789012', '789 Pine Rd', 3, 0);

-- Insert Participant Events
INSERT INTO participant_events (id, created_by, created_at, updated_by, updated_at, status, event_id, event_location_id, participant_id, spins_remaining, version) VALUES
(1, 'system', '2023-06-05 00:00:00', 'system', '2023-06-05 00:00:00', 'ACTIVE', 1, 1, 1, 5, 0),
(2, 'system', '2023-06-05 00:00:00', 'system', '2023-06-05 00:00:00', 'ACTIVE', 1, 1, 2, 3, 0),
(3, 'system', '2023-06-05 00:00:00', 'system', '2023-06-05 00:00:00', 'ACTIVE', 1, 1, 3, 4, 0);

-- Insert Rewards
INSERT INTO rewards (id, created_by, created_at, updated_by, updated_at, status, name, code, description, event_location_id, prizeValue, quantity, remaining_quantity, probability, version) VALUES
(1, 'admin', '2023-02-10 00:00:00', 'admin', '2023-02-10 00:00:00', 'ACTIVE', 'Cash Prize $100', 'CASH100', '$100 cash prize', 1, 100.00, 10, 10, 0.1, 0),
(2, 'admin', '2023-02-10 00:00:00', 'admin', '2023-02-10 00:00:00', 'ACTIVE', 'Gift Card $50', 'GIFT50', '$50 gift card', 1, 50.00, 20, 20, 0.2, 0),
(3, 'admin', '2023-02-10 00:00:00', 'admin', '2023-02-10 00:00:00', 'ACTIVE', 'Smartphone', 'PHONE', 'Latest smartphone', 2, 800.00, 5, 5, 0.05, 0);

-- Insert Reward Events
INSERT INTO reward_events (event_id, region_id, reward_id, created_by, created_at, updated_by, updated_at, status, quantity, todayQantity, version) VALUES
(1, 1, 1, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 10, 2, 0),
(1, 1, 2, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 20, 4, 0),
(1, 2, 3, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 5, 1, 0);

-- Insert Golden Hours
INSERT INTO golden_hours (id, created_by, created_at, updated_by, updated_at, status, event_location_id, start_time, end_time, multiplier, version) VALUES
(1, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 1, '2023-06-15 12:00:00', '2023-06-15 14:00:00', 2.0, 0),
(2, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 1, '2023-07-15 18:00:00', '2023-07-15 20:00:00', 2.0, 0),
(3, 'admin', '2023-02-15 00:00:00', 'admin', '2023-02-15 00:00:00', 'ACTIVE', 2, '2023-06-20 13:00:00', '2023-06-20 15:00:00', 2.5, 0);

-- Insert Spin Histories (some wins, some losses)
INSERT INTO spin_histories (id, created_by, created_at, updated_by, updated_at, status, participant_event_id, spin_time, reward_id, win, version) VALUES
(1, 'system', '2023-06-15 12:30:00', 'system', '2023-06-15 12:30:00', 'ACTIVE', 1, '2023-06-15 12:30:00', 1, true, 0),
(2, 'system', '2023-06-15 13:00:00', 'system', '2023-06-15 13:00:00', 'ACTIVE', 1, '2023-06-15 13:00:00', NULL, false, 0),
(3, 'system', '2023-06-15 13:15:00', 'system', '2023-06-15 13:15:00', 'ACTIVE', 1, '2023-06-15 13:15:00', 2, true, 0);

-- Insert Roles based on RoleType enum
INSERT INTO roles (id, created_by, created_at, updated_by, updated_at, status, role_type, description, display_order, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'ROLE_ADMIN', 'System Administrator', 1, 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'ROLE_USER', 'Regular User', 2, 0),
(3, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'ROLE_MANAGER', 'Manager', 3, 0),
(4, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'ROLE_PARTICIPANT', 'Participant', 4, 0),
(5, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'INACTIVE', 'ROLE_GUEST', 'Guest User', 5, 0);

-- Insert Permissions based on PermissionName enum
INSERT INTO permissions (id, created_by, created_at, updated_by, updated_at, status, name, type, description, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'CREATE_USER', 'WRITE', 'Create user accounts', 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'READ_USER', 'READ', 'View user accounts', 0);

-- Insert Users (with BCrypt-encoded passwords - all passwords are "password")
INSERT INTO users (id, created_by, created_at, updated_by, updated_at, status, username, password, email, full_name, role, enabled, account_expired, account_locked, credentials_expired, version) VALUES
(1, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'admin', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'admin@example.com', 'Admin User', 'ROLE_ADMIN', true, false, false, false, 0),
(2, 'system', '2023-01-01 00:00:00', 'system', '2023-01-01 00:00:00', 'ACTIVE', 'user', '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'user@example.com', 'Regular User', 'ROLE_USER', true, false, false, false, 0);

-- Insert User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),
(2, 2);

-- Insert Blacklisted Tokens
INSERT INTO blacklisted_tokens (id, created_by, created_at, updated_by, updated_at, status, token, token_type, expiration_time, user_id, version) VALUES
(1, 'system', '2023-01-15 00:00:00', 'system', '2023-01-15 00:00:00', 'ACTIVE', 'eyJhbGciOiJIUzI1NiJ9.expiredToken1', 'ACCESS', '2023-01-14 00:00:00', 1, 0),
(2, 'system', '2023-02-15 00:00:00', 'system', '2023-02-15 00:00:00', 'ACTIVE', 'eyJhbGciOiJIUzI1NiJ9.expiredToken2', 'REFRESH', '2023-02-14 00:00:00', 2, 0);

-- Insert Configurations
INSERT INTO configurations (id, created_by, created_at, updated_by, updated_at, status, config_key, config_value, description, version) VALUES
(1, 'admin', '2023-01-01 00:00:00', 'admin', '2023-01-01 00:00:00', 'ACTIVE', 'TOKEN_EXPIRY_MINUTES', '60', 'Authentication token expiry in minutes', 0),
(2, 'admin', '2023-01-01 00:00:00', 'admin', '2023-01-01 00:00:00', 'ACTIVE', 'MAX_DAILY_SPINS', '5', 'Maximum spins per day per participant', 0);

-- Insert Audit Logs (with objectId as String)
INSERT INTO audit_logs (id, created_by, created_at, updated_by, updated_at, status, object_type, object_id, property_path, old_value, new_value, value_type, update_time, context, action_type, version) VALUES
(1, 'admin', '2023-01-15 10:00:00', 'admin', '2023-01-15 10:00:00', 'ACTIVE', 'Event', '1', 'name', NULL, 'Summer Festival', 'String', '2023-01-15 10:00:00', 'Event creation', 'CREATED', 0),
(2, 'admin', '2023-02-10 12:00:00', 'admin', '2023-02-10 12:00:00', 'ACTIVE', 'Reward', '1', 'prizeValue', NULL, '100.00', 'BigDecimal', '2023-02-10 12:00:00', 'Reward creation', 'CREATED', 0);
