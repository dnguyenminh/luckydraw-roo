-- Test data for Lucky Draw application

-- Roles table
INSERT INTO roles (id, created_by, created_at, updated_by, updated_at, status, role_type, description, display_order, version) 
VALUES 
    (1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_ADMIN', 'System administrator with all privileges', 1, 0),
    (2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_MANAGER', 'Event manager with event management privileges', 2, 0),
    (3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_STAFF', 'Staff member with operational privileges', 3, 0);

-- Permissions table
INSERT INTO permissions (id, created_by, created_at, updated_by, updated_at, status, name, type, description, version)
VALUES
    (1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'EVENT_VIEW', 'READ', 'Can view events', 0),
    (2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'EVENT_CREATE', 'WRITE', 'Can create events', 0),
    (3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'EVENT_UPDATE', 'WRITE', 'Can update events', 0),
    (4, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'EVENT_DELETE', 'WRITE', 'Can delete events', 0),
    (5, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'USER_VIEW', 'READ', 'Can view users', 0);

-- Users table
INSERT INTO users (id, created_by, created_at, updated_by, updated_at, status, username, password, email, full_name, role_id, version)
VALUES
    (1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'admin', '$2a$10$aXxPWTOJ8QZfCFY9QZIcv.v9jLcDO88k3HQF1ntaD3z2DTg5hNYT6', 'admin@example.com', 'System Administrator', 1, 0),
    (2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'manager', '$2a$10$aXxPWTOJ8QZfCFY9QZIcv.v9jLcDO88k3HQF1ntaD3z2DTg5hNYT6', 'manager@example.com', 'Event Manager', 2, 0),
    (3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'staff', '$2a$10$aXxPWTOJ8QZfCFY9QZIcv.v9jLcDO88k3HQF1ntaD3z2DTg5hNYT6', 'staff@example.com', 'Staff Member', 3, 0);

-- Role-Permission assignments
INSERT INTO role_permissions (role_id, permission_id)
VALUES
    -- Admin has all permissions
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
    -- Manager has event and participant permissions
    (2, 1), (2, 2), (2, 3),
    -- Staff has view permissions
    (3, 1), (3, 5);

-- Regions
INSERT INTO regions (id, created_by, created_at, updated_by, updated_at, status, code, name, description, version) 
VALUES 
    (1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'NORTH', 'Northern Region', 'The northern part of the country', 0),
    (2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'SOUTH', 'Southern Region', 'The southern part of the country', 0),
    (3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'CENTRAL', 'Central Region', 'The central part of the country', 0);

-- Provinces
INSERT INTO provinces (id, created_by, created_at, updated_by, updated_at, status, code, name, description, region_id, version)
VALUES
    (1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'N01', 'Northern Province 1', 'First province in north', 1, 0),
    (2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'N02', 'Northern Province 2', 'Second province in north', 1, 0),
    (3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'S01', 'Southern Province 1', 'First province in south', 2, 0),
    (4, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'C01', 'Central Province 1', 'First province in central', 3, 0);

-- Events
INSERT INTO events (id, created_by, created_at, updated_by, updated_at, status, code, name, description, start_time, end_time, version)
VALUES
    (1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'SUMMER2023', 'Summer Lucky Draw 2023', 'Summer promotional event with prizes', '2023-06-01 00:00:00', '2023-08-31 23:59:59', 0),
    (2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'FALL2023', 'Fall Lucky Draw 2023', 'Fall promotional event with prizes', '2023-09-01 00:00:00', '2023-11-30 23:59:59', 0);

-- Event Locations - composite primary key (event_id, region_id)
INSERT INTO event_locations (event_id, region_id, created_by, created_at, updated_by, updated_at, status, description, max_spin, today_spin, daily_spin_distributing_rate, version)
VALUES
    (1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Summer event location in the north', 1000, 100, 0.1, 0),
    (1, 2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Summer event location in the south', 800, 80, 0.1, 0),
    (2, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Fall event location in the north', 1200, 120, 0.1, 0),
    (2, 2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Fall event location in the south', 900, 90, 0.1, 0);

-- Participants
INSERT INTO participants (id, created_by, created_at, updated_by, updated_at, status, name, code, phone, address, last_adding_spin, province_id, version)
VALUES
    (1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'John Doe', 'P001', '1234567890', '123 Main St', 0, 1, 0),
    (2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Jane Smith', 'P002', '2345678901', '456 Oak Ave', 0, 1, 0),
    (3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Bob Johnson', 'P003', '3456789012', '789 Pine Rd', 0, 3, 0);

-- Participant Events - composite primary key (participant_id, event_id, region_id)
INSERT INTO participant_events (participant_id, event_id, region_id, created_by, created_at, updated_by, updated_at, status, spins_remaining, version)
VALUES
    (1, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 5, 0),
    (2, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 3, 0),
    (3, 1, 2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 4, 0),
    (1, 2, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 2, 0);

-- Rewards
INSERT INTO rewards (id, created_by, created_at, updated_by, updated_at, status, name, code, description, prize_value, version)
VALUES
    (1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Cash Prize $100', 'CASH100', '$100 cash prize', 100.00, 0),
    (2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Cash Prize $50', 'CASH50', '$50 cash prize', 50.00, 0),
    (3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Cash Prize $20', 'CASH20', '$20 cash prize', 20.00, 0);

-- Reward Events - composite primary key (event_id, region_id, reward_id)
INSERT INTO reward_events (event_id, region_id, reward_id, created_by, created_at, updated_by, updated_at, status, quantity, today_quantity, version)
VALUES
    (1, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 10, 2, 0),
    (1, 1, 2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 20, 4, 0),
    (1, 2, 3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 50, 5, 0),
    (2, 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 15, 3, 0);

-- Golden Hours
INSERT INTO golden_hours (id, created_by, created_at, updated_by, updated_at, status, event_id, region_id, start_time, end_time, multiplier, max_rewards, claimed_rewards, version)
VALUES
    (1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 1, 1, '2023-06-15 12:00:00', '2023-06-15 14:00:00', 2.0, 50, 0, 0),
    (2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 1, 2, '2023-07-01 18:00:00', '2023-07-01 20:00:00', 1.5, 40, 0, 0);

-- Spin Histories
INSERT INTO spin_histories (id, created_by, created_at, updated_by, updated_at, status, participant_id, event_id, region_id, spin_time, reward_id, reward_event_id, reward_region_id, win, wheel_position, multiplier, version)
VALUES
    (1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 1, 1, 1, '2023-06-05 13:30:00', 1, 1, 1, TRUE, 120.5, 1.0, 0),
    (2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 1, 1, 1, '2023-06-06 14:45:00', NULL, NULL, NULL, FALSE, 45.2, 1.0, 0),
    (3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 2, 1, 1, '2023-06-07 15:20:00', 2, 1, 1, TRUE, 230.7, 1.0, 0);

-- Configurations
INSERT INTO configurations (id, created_by, created_at, updated_by, updated_at, status, config_key, config_value, description, version)
VALUES
    (1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'DAILY_SPIN_LIMIT', '5', 'Maximum number of spins allowed per day', 0),
    (2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'MIN_VERIFICATION_AGE', '18', 'Minimum age for identity verification', 0);

-- Audit Logs
INSERT INTO audit_logs (id, created_by, created_at, updated_by, updated_at, status, object_type, object_id, property_path, old_value, new_value, value_type, update_time, context, action_type, version)
VALUES
    (1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Event', '1', 'status', 'DRAFT', 'ACTIVE', 'CommonStatus', CURRENT_TIMESTAMP, 'EVENT_ACTIVATION', 'UPDATE', 0),
    (2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Participant', '3', 'status', 'ACTIVE', 'INACTIVE', 'CommonStatus', CURRENT_TIMESTAMP, 'PARTICIPANT_DEACTIVATION', 'UPDATE', 0);
