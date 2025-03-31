-- Test data for H2 database testing

-- Test data for events
INSERT INTO events (id, version, code, name, description, start_time, end_time, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 'EVENT001', 'Test Event 1', 'Test Description 1', DATEADD('HOUR', -1, CURRENT_TIMESTAMP), DATEADD('HOUR', 23, CURRENT_TIMESTAMP), 'ACTIVE', 'system', 'system', false),
(2, 0, 'EVENT002', 'Test Event 2', 'Test Description 2', DATEADD('HOUR', -2, CURRENT_TIMESTAMP), DATEADD('HOUR', 22, CURRENT_TIMESTAMP), 'ACTIVE', 'system', 'system', false),
(3, 0, 'EVENT003', 'Test Event 3', 'Test Description 3', DATEADD('HOUR', 1, CURRENT_TIMESTAMP), DATEADD('HOUR', 25, CURRENT_TIMESTAMP), 'INACTIVE', 'system', 'system', false),
(4, 0, 'EVENT004', 'Test Event 4', 'Test Description 4', DATEADD('DAY', -1, CURRENT_TIMESTAMP), DATEADD('DAY', 6, CURRENT_TIMESTAMP), 'ACTIVE', 'system', 'system', false);

-- Test data for regions
INSERT INTO regions (id, version, code, name, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 'REGION001', 'North Region', 'ACTIVE', 'system', 'system', false),
(2, 0, 'REGION002', 'South Region', 'ACTIVE', 'system', 'system', false),
(3, 0, 'REGION003', 'East Region', 'INACTIVE', 'system', 'system', false);

-- Test data for provinces
INSERT INTO provinces (id, version, code, name, region_id, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 'PROVINCE001', 'Test Province 1', 1, 'ACTIVE', 'system', 'system', false),
(2, 0, 'PROVINCE002', 'Test Province 2', 2, 'ACTIVE', 'system', 'system', false);

-- Test data for event_locations
INSERT INTO event_locations (id, version, event_id, region_id, status, max_spin, name, code, created_by, updated_by, deleted)
VALUES 
(1, 0, 1, 1, 'ACTIVE', 3, 'Location 1', 'LOC001', 'system', 'system', false),
(2, 0, 2, 2, 'ACTIVE', 3, 'Location 2', 'LOC002', 'system', 'system', false),
(3, 0, 3, 1, 'INACTIVE', 5, 'Location 3', 'LOC003', 'system', 'system', false),
(4, 0, 4, 2, 'ACTIVE', 2, 'Location 4', 'LOC004', 'system', 'system', false);

-- Test data for participants
INSERT INTO participants (id, version, code, name, province_id, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 'PART001', 'John Doe', 1, 'ACTIVE', 'system', 'system', false),
(2, 0, 'PART002', 'Jane Smith', 2, 'ACTIVE', 'system', 'system', false),
(3, 0, 'PART003', 'Bob Johnson', 1, 'INACTIVE', 'system', 'system', false),
(4, 0, 'PART004', 'Alice Brown', 2, 'ACTIVE', 'system', 'system', false);

-- Test data for participant_events
INSERT INTO participant_events (id, version, event_id, event_location_id, participant_id, spins_remaining, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 1, 1, 1, 3, 'ACTIVE', 'system', 'system', false),
(2, 0, 2, 2, 2, 3, 'ACTIVE', 'system', 'system', false),
(3, 0, 1, 1, 3, 0, 'INACTIVE', 'system', 'system', false),
(4, 0, 4, 4, 4, 2, 'ACTIVE', 'system', 'system', false);

-- Test data for rewards
INSERT INTO rewards (id, version, name, code, description, event_location_id, "value", quantity, win_probability, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 'Reward 1', 'REW001', 'First Reward', 1, 100.00, 10, 0.1, 'ACTIVE', 'system', 'system', false),
(2, 0, 'Reward 2', 'REW002', 'Second Reward', 2, 50.00, 20, 0.2, 'ACTIVE', 'system', 'system', false);

-- Test data for roles
INSERT INTO roles (id, version, role_name, display_order, description, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 'ADMIN', 1, 'Administrator', 'ACTIVE', 'system', 'system', false),
(2, 0, 'USER', 2, 'Regular User', 'ACTIVE', 'system', 'system', false);

-- Test data for users
INSERT INTO users (id, version, username, password, email, full_name, enabled, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 'admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'admin@example.com', 'Admin User', true, 'ACTIVE', 'system', 'system', false),
(2, 0, 'user', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'user@example.com', 'Regular User', true, 'ACTIVE', 'system', 'system', false);

-- Test data for user_roles
INSERT INTO user_roles (user_id, role_id)
VALUES 
(1, 1),
(2, 2);

-- Test data for audit_logs
INSERT INTO audit_logs (id, username, status) 
VALUES (1, 'testUser', 'ACTIVE');

-- Test data for configurations
INSERT INTO configurations (id, config_key, config_value, status) 
VALUES (1, 'testKey', 'testValue', 'ACTIVE');
