-- =====================================================
-- LuckyDraw Large Test Data
-- Contains 1000+ records for each entity
-- =====================================================

-- =============== REGIONS (1000 records) ================
INSERT INTO regions (id, code, name, description, status, created_by, updated_by, created_at, updated_at, version) VALUES 
(1, 'REG001', 'North Region', 'Northern regional area', 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 'REG002', 'South Region', 'Southern regional area', 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 'REG003', 'East Region', 'Eastern regional area', 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Generate 997 more regions
INSERT INTO regions (code, name, description, status, created_by, updated_by, created_at, updated_at, version)
WITH RECURSIVE region_gen AS (
    SELECT 4 AS counter
    UNION ALL
    SELECT counter + 1 FROM region_gen WHERE counter < 1000
)
SELECT 
    'REG' || LPAD(counter::TEXT, 3, '0'),
    'Region ' || counter,
    'Description for region ' || counter,
    CASE WHEN counter % 10 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    'system',
    'system',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM region_gen;

-- =============== PROVINCES (1000 records) ================
INSERT INTO provinces (id, code, name, description, region_id, status, created_by, updated_by, created_at, updated_at, version) VALUES
(1, 'PRV001', 'Province 1', 'First province', 1, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 'PRV002', 'Province 2', 'Second province', 1, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 'PRV003', 'Province 3', 'Third province', 2, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Generate 997 more provinces
INSERT INTO provinces (code, name, description, region_id, status, created_by, updated_by, created_at, updated_at, version)
WITH RECURSIVE province_gen AS (
    SELECT 4 AS counter
    UNION ALL
    SELECT counter + 1 FROM province_gen WHERE counter < 1000
)
SELECT 
    'PRV' || LPAD(counter::TEXT, 3, '0'),
    'Province ' || counter,
    'Description for province ' || counter,
    (counter % 1000) + 1, -- Reference valid region_id
    CASE WHEN counter % 5 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    'system',
    'system',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM province_gen;

-- =============== ROLES (1000 records) ================
INSERT INTO roles (id, role_type, description, display_order, status, created_by, updated_by, created_at, updated_at, version) VALUES
(1, 'ROLE_ADMIN', 'Administrator role', 1, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 'ROLE_USER', 'Standard user role', 2, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 'ROLE_MANAGER', 'Manager role', 3, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Generate 997 more roles with cycling role types
INSERT INTO roles (role_type, description, display_order, status, created_by, updated_by, created_at, updated_at, version)
WITH RECURSIVE role_gen AS (
    SELECT 4 AS counter
    UNION ALL
    SELECT counter + 1 FROM role_gen WHERE counter < 1000
)
SELECT 
    CASE counter % 5
        WHEN 0 THEN 'ROLE_ADMIN'
        WHEN 1 THEN 'ROLE_USER'
        WHEN 2 THEN 'ROLE_MANAGER'
        WHEN 3 THEN 'ROLE_PARTICIPANT'
        ELSE 'ROLE_GUEST'
    END,
    'Role description ' || counter,
    counter,
    CASE WHEN counter % 7 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    'system',
    'system',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM role_gen;

-- =============== PERMISSIONS (1000 records) ================
INSERT INTO permissions (id, name, description, status, created_by, updated_by, created_at, updated_at, version) VALUES
(1, 'READ_USER', 'Read user data', 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 'WRITE_USER', 'Create and update users', 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 'DELETE_USER', 'Delete users', 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Generate 997 more permissions
INSERT INTO permissions (name, description, status, created_by, updated_by, created_at, updated_at, version)
WITH RECURSIVE perm_gen AS (
    SELECT 4 AS counter
    UNION ALL
    SELECT counter + 1 FROM perm_gen WHERE counter < 1000
)
SELECT 
    'PERMISSION_' || counter,
    'Permission description ' || counter,
    CASE WHEN counter % 9 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    'system',
    'system',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM perm_gen;

-- =============== ROLE_PERMISSIONS (1000+ records) ================
-- Assign all permissions to admin role
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- Assign read permissions to user role
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id FROM permissions WHERE name LIKE 'READ%';

-- Generate many more role-permission associations
WITH RECURSIVE rp_gen AS (
    SELECT 1 AS role_counter, 1 AS perm_counter
    UNION ALL
    SELECT 
        CASE WHEN perm_counter >= 1000 THEN role_counter + 1 ELSE role_counter END,
        CASE WHEN perm_counter >= 1000 THEN 1 ELSE perm_counter + 1 END
    FROM rp_gen 
    WHERE role_counter < 1000 AND (role_counter < 999 OR perm_counter < 1000)
)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    (role_counter % 1000) + 1,
    (perm_counter % 1000) + 1
FROM rp_gen
WHERE (role_counter + perm_counter) % 3 = 0 -- Only insert some combinations to avoid duplicate key violations
AND role_counter <= 1000 AND perm_counter <= 1000
ON CONFLICT DO NOTHING;

-- =============== USERS (1000 records) ================
INSERT INTO users (id, username, email, password, full_name, role, enabled, account_expired, account_locked, credentials_expired, status, created_by, updated_by, created_at, updated_at, version) VALUES 
(1, 'admin', 'admin@example.com', '$2a$10$aCJqfLX8aKgffGyD/XRgFe7QCmLaU6BAM3yYqNj64Cgx0BdLyNz1m', 'Administrator', 'ROLE_ADMIN', true, false, false, false, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 'user', 'user@example.com', '$2a$10$aCJqfLX8aKgffGyD/XRgFe7QCmLaU6BAM3yYqNj64Cgx0BdLyNz1m', 'Regular User', 'ROLE_USER', true, false, false, false, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 'manager', 'manager@example.com', '$2a$10$aCJqfLX8aKgffGyD/XRgFe7QCmLaU6BAM3yYqNj64Cgx0BdLyNz1m', 'Manager User', 'ROLE_MANAGER', true, false, false, false, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Generate 997 more users
INSERT INTO users (username, email, password, full_name, role, enabled, account_expired, account_locked, credentials_expired, status, created_by, updated_by, created_at, updated_at, version)
WITH RECURSIVE user_gen AS (
    SELECT 4 AS counter
    UNION ALL
    SELECT counter + 1 FROM user_gen WHERE counter < 1000
)
SELECT 
    'user' || counter,
    'user' || counter || '@example.com',
    '$2a$10$aCJqfLX8aKgffGyD/XRgFe7QCmLaU6BAM3yYqNj64Cgx0BdLyNz1m', -- Same hashed password for all test users
    'User ' || counter,
    CASE counter % 5
        WHEN 0 THEN 'ROLE_ADMIN'
        WHEN 1 THEN 'ROLE_USER'
        WHEN 2 THEN 'ROLE_MANAGER'
        WHEN 3 THEN 'ROLE_PARTICIPANT'
        ELSE 'ROLE_GUEST'
    END,
    CASE WHEN counter % 10 = 0 THEN false ELSE true END,
    CASE WHEN counter % 11 = 0 THEN true ELSE false END,
    CASE WHEN counter % 13 = 0 THEN true ELSE false END,
    CASE WHEN counter % 17 = 0 THEN true ELSE false END,
    CASE WHEN counter % 19 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    'system',
    'system',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM user_gen;

-- =============== USER_ROLES (1000+ records) ================
-- Assign roles to users
INSERT INTO user_roles (user_id, role_id)
WITH RECURSIVE ur_gen AS (
    SELECT 1 AS user_counter, 1 AS role_counter
    UNION ALL
    SELECT 
        CASE WHEN role_counter >= 3 THEN user_counter + 1 ELSE user_counter END,
        CASE WHEN role_counter >= 3 THEN 1 ELSE role_counter + 1 END
    FROM ur_gen 
    WHERE user_counter <= 1000
)
SELECT 
    user_counter,
    role_counter
FROM ur_gen
ON CONFLICT DO NOTHING;

-- =============== EVENTS (1000 records) ================
INSERT INTO events (id, code, name, description, start_time, end_time, status, created_by, updated_by, created_at, updated_at, version) VALUES
(1, 'EVENT001', 'Summer Lucky Draw', 'Annual summer promotional event', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days', 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 'EVENT002', 'Winter Giveaway', 'Winter holiday special', CURRENT_TIMESTAMP + INTERVAL '60 days', CURRENT_TIMESTAMP + INTERVAL '90 days', 'PLANNED', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 'EVENT003', 'Spring Festival', 'Spring special event', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP + INTERVAL '10 days', 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Generate 997 more events
INSERT INTO events (code, name, description, start_time, end_time, status, created_by, updated_by, created_at, updated_at, version)
WITH RECURSIVE event_gen AS (
    SELECT 4 AS counter
    UNION ALL
    SELECT counter + 1 FROM event_gen WHERE counter < 1000
)
SELECT 
    'EVENT' || LPAD(counter::TEXT, 3, '0'),
    'Event ' || counter,
    'Description for event ' || counter,
    CURRENT_TIMESTAMP + (counter * INTERVAL '1 day'),
    CURRENT_TIMESTAMP + (counter * INTERVAL '1 day') + INTERVAL '30 days',
    CASE counter % 4
        WHEN 0 THEN 'ACTIVE'
        WHEN 1 THEN 'PLANNED'
        WHEN 2 THEN 'PENDING'
        ELSE 'ARCHIVED'
    END,
    'system',
    'system',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM event_gen;

-- =============== EVENT_LOCATIONS (1000 records) ================
INSERT INTO event_locations (id, code, name, description, event_id, region_id, quantity, max_spin, win_probability, status, created_by, updated_by, created_at, updated_at, version) VALUES
(1, 'LOC001', 'Main Center', 'Primary event location', 1, 1, 100, 3, 0.2, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 'LOC002', 'South Branch', 'Southern event location', 1, 2, 80, 2, 0.15, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 'LOC003', 'East Mall', 'Eastern shopping center', 2, 3, 120, 3, 0.25, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Generate 997 more event locations
INSERT INTO event_locations (code, name, description, event_id, region_id, quantity, max_spin, win_probability, status, created_by, updated_by, created_at, updated_at, version)
WITH RECURSIVE loc_gen AS (
    SELECT 4 AS counter
    UNION ALL
    SELECT counter + 1 FROM loc_gen WHERE counter < 1000
)
SELECT 
    'LOC' || LPAD(counter::TEXT, 3, '0'),
    'Location ' || counter,
    'Description for location ' || counter,
    (counter % 1000) + 1,  -- event_id
    (counter % 1000) + 1,  -- region_id
    50 + (counter % 100),  -- quantity
    (counter % 5) + 1,     -- max_spin
    (counter % 10) / 10.0, -- win_probability
    CASE WHEN counter % 7 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    'system',
    'system',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM loc_gen;

-- =============== REWARDS (1000 records) ================
INSERT INTO rewards (id, code, name, description, event_location_id, status, created_by, updated_by, created_at, updated_at, version) VALUES
(1, 'RWD001', 'Cash Prize', '$100 cash prize', 1, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 'RWD002', 'Gift Card', '$50 gift card', 1, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 'RWD003', 'Free Product', 'Free product sample', 2, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Generate 997 more rewards
INSERT INTO rewards (code, name, description, event_location_id, status, created_by, updated_by, created_at, updated_at, version)
WITH RECURSIVE reward_gen AS (
    SELECT 4 AS counter
    UNION ALL
    SELECT counter + 1 FROM reward_gen WHERE counter < 1000
)
SELECT 
    'RWD' || LPAD(counter::TEXT, 3, '0'),
    'Reward ' || counter,
    'Description for reward ' || counter,
    (counter % 1000) + 1, -- event_location_id
    CASE WHEN counter % 11 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    'system',
    'system',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM reward_gen;

-- =============== PARTICIPANTS (1000 records) ================
INSERT INTO participants (id, code, name, phone, address, province_id, checked_in, status, created_by, updated_by, created_at, updated_at, version) VALUES
(1, 'PAR001', 'John Doe', '555-123-4567', '123 Main St', 1, true, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 'PAR002', 'Jane Smith', '555-234-5678', '456 Oak Ave', 2, false, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 'PAR003', 'Mike Johnson', '555-345-6789', '789 Pine Blvd', 3, true, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Generate 997 more participants
INSERT INTO participants (code, name, phone, address, province_id, checked_in, status, created_by, updated_by, created_at, updated_at, version)
WITH RECURSIVE part_gen AS (
    SELECT 4 AS counter
    UNION ALL
    SELECT counter + 1 FROM part_gen WHERE counter < 1000
)
SELECT 
    'PAR' || LPAD(counter::TEXT, 3, '0'),
    'Participant ' || counter,
    '555-' || LPAD((counter / 1000)::TEXT, 3, '0') || '-' || LPAD((counter % 1000)::TEXT, 4, '0'),
    counter || ' Street Name',
    (counter % 1000) + 1, -- province_id
    counter % 2 = 0, -- checked_in
    CASE WHEN counter % 13 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    'system',
    'system',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM part_gen;

-- =============== PARTICIPANT_EVENTS (1000 records) ================
INSERT INTO participant_events (id, participant_id, event_id, event_location_id, spins_remaining, status, created_by, updated_by, created_at, updated_at, version) VALUES
(1, 1, 1, 1, 3, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 2, 1, 2, 2, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 3, 2, 3, 3, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Generate 997 more participant events
INSERT INTO participant_events (participant_id, event_id, event_location_id, spins_remaining, status, created_by, updated_by, created_at, updated_at, version)
WITH RECURSIVE pe_gen AS (
    SELECT 4 AS counter
    UNION ALL
    SELECT counter + 1 FROM pe_gen WHERE counter < 1000
)
SELECT 
    (counter % 1000) + 1, -- participant_id
    (counter % 1000) + 1, -- event_id
    (counter % 1000) + 1, -- event_location_id
    (counter % 5) + 1, -- spins_remaining
    CASE WHEN counter % 17 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    'system',
    'system',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM pe_gen;

-- =============== GOLDEN_HOURS (1000 records) ================
INSERT INTO golden_hours (id, event_location_id, start_time, end_time, multiplier, status, created_by, updated_by, created_at, updated_at, version) VALUES
(1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '2 hours', 2.0, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 2, CURRENT_TIMESTAMP + INTERVAL '3 hours', CURRENT_TIMESTAMP + INTERVAL '5 hours', 1.5, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 3, CURRENT_TIMESTAMP + INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '1 day 2 hours', 2.5, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Generate 997 more golden hours
INSERT INTO golden_hours (event_location_id, start_time, end_time, multiplier, status, created_by, updated_by, created_at, updated_at, version)
WITH RECURSIVE gh_gen AS (
    SELECT 4 AS counter
    UNION ALL
    SELECT counter + 1 FROM gh_gen WHERE counter < 1000
)
SELECT 
    (counter % 1000) + 1, -- event_location_id
    CURRENT_TIMESTAMP + (counter * INTERVAL '4 hours'),
    CURRENT_TIMESTAMP + (counter * INTERVAL '4 hours') + INTERVAL '2 hours',
    (counter % 4) + 1.0, -- multiplier
    CASE WHEN counter % 19 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    'system',
    'system',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM gh_gen;

-- =============== SPIN_HISTORIES (1000 records) ================
INSERT INTO spin_histories (id, participant_event_id, spin_time, reward_id, golden_hour_id, win, multiplier, status, created_by, updated_by, created_at, updated_at, version) VALUES
(1, 1, CURRENT_TIMESTAMP - INTERVAL '1 hour', 1, 1, true, 2.0, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 1, CURRENT_TIMESTAMP - INTERVAL '30 minutes', 2, 1, true, 2.0, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 2, CURRENT_TIMESTAMP - INTERVAL '45 minutes', NULL, NULL, false, NULL, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Generate 997 more spin histories
INSERT INTO spin_histories (participant_event_id, spin_time, reward_id, golden_hour_id, win, multiplier, status, created_by, updated_by, created_at, updated_at, version)
WITH RECURSIVE spin_gen AS (
    SELECT 4 AS counter
    UNION ALL
    SELECT counter + 1 FROM spin_gen WHERE counter < 1000
)
SELECT 
    (counter % 1000) + 1, -- participant_event_id
    CURRENT_TIMESTAMP - (counter * INTERVAL '5 minutes'),
    CASE WHEN counter % 3 = 0 THEN (counter % 1000) + 1 ELSE NULL END, -- reward_id (some NULL for losses)
    CASE WHEN counter % 5 = 0 THEN (counter % 1000) + 1 ELSE NULL END, -- golden_hour_id
    counter % 3 = 0, -- win (1/3 win rate)
    CASE WHEN counter % 5 = 0 AND counter % 3 = 0 THEN ((counter % 4) + 1)::NUMERIC ELSE NULL END, -- multiplier
    CASE WHEN counter % 23 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    'system',
    'system',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM spin_gen;

-- =============== CONFIGURATIONS (1000 records) ================
INSERT INTO configurations (id, config_key, config_value, description, status, created_by, updated_by, created_at, updated_at, version) VALUES
(1, 'DEFAULT_SPIN_COUNT', '3', 'Default number of spins for new participants', 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 'DEFAULT_WIN_PROBABILITY', '0.2', 'Default win probability for locations', 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 'ENABLE_GOLDEN_HOURS', 'true', 'Enable golden hour functionality', 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Generate 997 more configurations
INSERT INTO configurations (config_key, config_value, description, status, created_by, updated_by, created_at, updated_at, version)
WITH RECURSIVE config_gen AS (
    SELECT 4 AS counter
    UNION ALL
    SELECT counter + 1 FROM config_gen WHERE counter < 1000
)
SELECT 
    'CONFIG_KEY_' || counter,
    'value_' || counter,
    'Description for configuration ' || counter,
    CASE WHEN counter % 29 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    'system',
    'system',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM config_gen;

-- =============== AUDIT_LOGS (1000 records) ================
INSERT INTO audit_logs (id, object_type, object_id, action_type, property_path, old_value, new_value, value_type, context, update_time, status, created_by, updated_by, created_at, updated_at, version) VALUES
(1, 'User', 1, 'CREATED', NULL, NULL, '{"id":1,"username":"admin"}', 'JSON', 'User creation', CURRENT_TIMESTAMP, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 'User', 2, 'CREATED', NULL, NULL, '{"id":2,"username":"user"}', 'JSON', 'User creation', CURRENT_TIMESTAMP, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 'Event', 1, 'CREATED', NULL, NULL, '{"id":1,"name":"Summer Lucky Draw"}', 'JSON', 'Event creation', CURRENT_TIMESTAMP, 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Generate 997 more audit logs
INSERT INTO audit_logs (object_type, object_id, action_type, property_path, old_value, new_value, value_type, context, update_time, status, created_by, updated_by, created_at, updated_at, version)
WITH RECURSIVE audit_gen AS (
    SELECT 4 AS counter
    UNION ALL
    SELECT counter + 1 FROM audit_gen WHERE counter < 1000
)
SELECT 
    CASE counter % 7
        WHEN 0 THEN 'User'
        WHEN 1 THEN 'Event'
        WHEN 2 THEN 'Participant'
        WHEN 3 THEN 'Reward'
        WHEN 4 THEN 'EventLocation'
        WHEN 5 THEN 'SpinHistory'
        ELSE 'Configuration'
    END,
    (counter % 100) + 1, -- object_id
    CASE counter % 7
        WHEN 0 THEN 'CREATED'
        WHEN 1 THEN 'MODIFIED'
        WHEN 2 THEN 'DELETED'
        WHEN 3 THEN 'ACTIVATED'
        WHEN 4 THEN 'DEACTIVATED'
        WHEN 5 THEN 'VIEWED'
        ELSE 'LOGIN'
    END,
    CASE WHEN counter % 3 = 0 THEN 'name' 
         WHEN counter % 3 = 1 THEN 'status'
         ELSE NULL
    END, -- property_path
    CASE WHEN counter % 3 = 0 THEN 'Old Value ' || counter ELSE NULL END, -- old_value
    CASE WHEN counter % 7 < 5 THEN 'New Value ' || counter ELSE NULL END, -- new_value
    CASE counter % 4
        WHEN 0 THEN 'STRING'
        WHEN 1 THEN 'NUMBER'
        WHEN 2 THEN 'BOOLEAN'
        ELSE 'JSON'
    END,
    'Audit context ' || counter,
    CURRENT_TIMESTAMP - (counter * INTERVAL '1 minute'),
    CASE WHEN counter % 31 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    'system',
    'system',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM audit_gen;

-- =============== BLACKLISTED_TOKENS (1000 records) ================
INSERT INTO blacklisted_tokens (id, token, token_type, user_id, expiration_time, status, created_by, updated_by, created_at, updated_at, version) VALUES
(1, 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiaWF0IjoxNTE2MjM5MDIyfQ.DUMMY_SIG', 'ACCESS', 1, CURRENT_TIMESTAMP + INTERVAL '1 day', 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIyIiwiaWF0IjoxNTE2MjM5MDIyfQ.DUMMY_SIG', 'REFRESH', 2, CURRENT_TIMESTAMP + INTERVAL '7 days', 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIzIiwiaWF0IjoxNTE2MjM5MDIyfQ.DUMMY_SIG', 'ACCESS', 3, CURRENT_TIMESTAMP + INTERVAL '1 day', 'ACTIVE', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Generate 997 more blacklisted tokens
INSERT INTO blacklisted_tokens (token, token_type, user_id, expiration_time, status, created_by, updated_by, created_at, updated_at, version)
WITH RECURSIVE token_gen AS (
    SELECT 4 AS counter
    UNION ALL
    SELECT counter + 1 FROM token_gen WHERE counter < 1000
)
SELECT 
    'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb3VudGVyIjoiJyB8fCBjb3VudGVyIHx8ICciLCJpYXQiOjE1MTYyMzkwMjJ9.DUMMY_SIG_' || counter,
    CASE WHEN counter % 2 = 0 THEN 'ACCESS' ELSE 'REFRESH' END,
    (counter % 1000) + 1, -- user_id
    CURRENT_TIMESTAMP + (counter % 30) * INTERVAL '1 day',
    CASE WHEN counter % 37 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    'system',
    'system',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM token_gen;

-- Complete! This SQL file contains 1000+ records for each entity in the Lucky Draw system.
