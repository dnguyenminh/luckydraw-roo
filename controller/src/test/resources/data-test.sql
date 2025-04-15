-- Insert Regions (10 regions)
INSERT INTO regions (id, name, code, description, status, created_by, created_at, updated_by, updated_at)
VALUES 
(1, 'North', 'NORTH', 'Northern region of the country', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2, 'Central', 'CENTRAL', 'Central region with major cities', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(3, 'South', 'SOUTH', 'Southern region including coastal areas', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(4, 'East', 'EAST', 'Eastern region with industrial zones', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(5, 'West', 'WEST', 'Western region with agricultural focus', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(6, 'Northeast', 'NORTHEAST', 'Northeastern mountainous region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(7, 'Northwest', 'NORTHWEST', 'Northwestern highland region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(8, 'Southeast', 'SOUTHEAST', 'Southeastern economic zone', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(9, 'Southwest', 'SOUTHWEST', 'Southwestern delta region', 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(10, 'Central Highlands', 'HIGHLANDS', 'Central highland mountainous area', 'PENDING', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Generate 100 provinces across different regions
INSERT INTO provinces (id, name, code, region_id, status, created_by, created_at, updated_by, updated_at)
SELECT 
    t.id,
    CONCAT('Province-', t.id),
    CONCAT('PRV', LPAD(CAST(t.id AS VARCHAR), 3, '0')),
    (t.id % 10) + 1, -- Region ID (1-10)
    CASE 
        WHEN t.id % 10 = 0 THEN 'INACTIVE'
        WHEN t.id % 20 = 0 THEN 'PENDING'
        ELSE 'ACTIVE'
    END,
    'system',
    DATEADD('DAY', -t.id, CURRENT_TIMESTAMP),
    'system',
    DATEADD('DAY', -t.id, CURRENT_TIMESTAMP)
FROM generate_series(1, 100) AS t(id);

-- Generate 100 users with different statuses
INSERT INTO users (id, username, password, email, full_name, enabled, account_expired, account_locked, credentials_expired, role, status, created_by, created_at, updated_by, updated_at)
SELECT 
    t.id,
    CASE
        WHEN t.id = 1 THEN 'admin'
        WHEN t.id = 2 THEN 'user1'
        WHEN t.id = 3 THEN 'manager'
        WHEN t.id = 4 THEN 'participant'
        WHEN t.id = 5 THEN 'disabled'
        WHEN t.id = 6 THEN 'pending'
        ELSE CONCAT('user', t.id)
    END,
    '$2a$10$XXXXXXXXXXXXXXXXXXXXXXXXXX',
    CASE
        WHEN t.id = 1 THEN 'admin@example.com'
        WHEN t.id = 2 THEN 'user1@example.com'
        WHEN t.id = 3 THEN 'manager@example.com'
        WHEN t.id = 4 THEN 'participant@example.com'
        WHEN t.id = 5 THEN 'disabled@example.com'
        WHEN t.id = 6 THEN 'pending@example.com'
        ELSE CONCAT('user', t.id, '@example.com')
    END,
    CONCAT(
        (ARRAY['John', 'Jane', 'Robert', 'Mary', 'William', 'Linda', 'Michael', 'Elizabeth', 'David', 'Sarah'])[1 + (t.id % 10)],
        ' ',
        (ARRAY['Smith', 'Johnson', 'Williams', 'Jones', 'Brown', 'Davis', 'Miller', 'Wilson', 'Moore', 'Taylor'])[1 + ((t.id * 3) % 10)]
    ),
    CASE WHEN t.id % 5 = 0 THEN false ELSE true END,
    false, -- account_expired
    false, -- account_locked
    false, -- credentials_expired
    CASE
        WHEN t.id = 1 THEN 'ROLE_ADMIN'
        WHEN t.id = 3 THEN 'ROLE_MANAGER'
        WHEN t.id = 4 THEN 'ROLE_PARTICIPANT'
        ELSE 'ROLE_USER'
    END,
    CASE 
        WHEN t.id % 5 = 0 THEN 'INACTIVE'
        WHEN t.id % 10 = 0 THEN 'PENDING'
        ELSE 'ACTIVE'
    END,
    'system',
    DATEADD('DAY', -t.id, CURRENT_TIMESTAMP),
    'system',
    DATEADD('DAY', -t.id, CURRENT_TIMESTAMP)
FROM generate_series(1, 100) AS t(id);

-- Insert Roles (5 roles) - Using only unique role types
INSERT INTO roles (id, role_type, description, display_order, status, created_by, created_at, updated_by, updated_at)
VALUES 
(1, 'ROLE_ADMIN', 'Administrator role', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2, 'ROLE_USER', 'User role', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(3, 'ROLE_MANAGER', 'Manager role', 3, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(4, 'ROLE_PARTICIPANT', 'Participant role', 4, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(5, 'ROLE_GUEST', 'Guest role', 5, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Generate User-Role associations (at least 1 role per user)
INSERT INTO user_roles (user_id, role_id)
SELECT 
    t.id, -- User ID
    CASE 
        WHEN t.id = 1 THEN 1  -- admin has Administrator role
        WHEN t.id = 2 THEN 2  -- user1 has User role
        WHEN t.id = 3 THEN 3  -- manager has Manager role
        WHEN t.id = 4 THEN 4  -- participant has Participant role
        WHEN t.id = 6 THEN 2  -- pending has User role
        ELSE 1 + (t.id % 5)  -- Assign role based on user ID
    END
FROM generate_series(1, 100) AS t(id);

-- Add secondary roles to some users
INSERT INTO user_roles (user_id, role_id)
SELECT 
    t.id, -- User ID
    1 + ((t.id + 3) % 5) -- A different role than primary
FROM generate_series(1, 100) AS t(id)
WHERE t.id % 3 = 0;  -- Every third user gets a second role

-- Insert Permissions (20 permissions)
INSERT INTO permissions (id, name, description, status, created_by, created_at, updated_by, updated_at)
VALUES 
(1, 'MANAGE_USERS', 'Manage user accounts', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2, 'VIEW_REPORTS', 'View system reports', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(3, 'MANAGE_EVENTS', 'Manage events', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(4, 'OPERATE_EVENTS', 'Operate events', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(5, 'EDIT_PROFILE', 'Edit user profile', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(6, 'VIEW_DASHBOARD', 'View dashboard', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(7, 'MANAGE_PARTICIPANTS', 'Manage participants', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(8, 'MANAGE_REWARDS', 'Manage rewards', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(9, 'MANAGE_LOCATIONS', 'Manage locations', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(10, 'EXPORT_DATA', 'Export data', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(11, 'IMPORT_DATA', 'Import data', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(12, 'CREATE_EVENTS', 'Create events', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(13, 'DELETE_EVENTS', 'Delete events', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(14, 'VIEW_AUDIT_LOGS', 'View audit logs', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(15, 'MANAGE_SYSTEM', 'Manage system settings', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(16, 'CONFIGURE_SETTINGS', 'Configure application settings', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(17, 'RUN_REPORTS', 'Run system reports', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(18, 'SCHEDULE_EVENTS', 'Schedule events', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(19, 'REGISTER_PARTICIPANTS', 'Register participants', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(20, 'PROCESS_REWARDS', 'Process rewards', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Generate Permission-Role associations
INSERT INTO role_permissions (permission_id, role_id)
SELECT
    p.id,
    r.id
FROM generate_series(1, 20) AS p(id)
CROSS JOIN generate_series(1, 5) AS r(id)
WHERE 
    -- Administrator (role_id=1) has all permissions
    r.id = 1
    -- Other roles have specific permissions based on patterns
    OR (r.id = 2 AND p.id IN (5, 6)) -- User permissions
    OR (r.id = 3 AND p.id IN (2, 3, 6, 7, 8, 9, 12, 17, 18, 19)) -- Manager permissions
    OR (r.id = 4 AND p.id IN (4, 6, 7, 19, 20)) -- Participant permissions
    OR (r.id = 5 AND p.id IN (2, 6, 10, 14, 17)) -- Guest permissions
;

-- Generate 100 events with different statuses and dates
INSERT INTO events (id, name, code, description, start_time, end_time, status, created_by, created_at, updated_by, updated_at)
SELECT 
    t.id,
    CASE
        WHEN t.id = 1 THEN 'Annual Conference'
        WHEN t.id = 2 THEN 'Team Building'
        WHEN t.id = 3 THEN 'Product Launch'
        WHEN t.id = 4 THEN 'Training Workshop'
        WHEN t.id = 5 THEN 'Customer Appreciation'
        WHEN t.id = 6 THEN 'Charity Gala'
        ELSE CONCAT(
            (ARRAY['Regional', 'Local', 'National', 'International', 'Corporate', 'Community', 'Internal', 'Public'])[1 + (t.id % 8)],
            ' ',
            (ARRAY['Conference', 'Workshop', 'Seminar', 'Meeting', 'Festival', 'Celebration', 'Launch', 'Summit'])[1 + ((t.id * 3) % 8)],
            ' ',
            (t.id / 8) + 1
        )
    END,
    CONCAT('EVT-', LPAD(CAST(t.id AS VARCHAR), 4, '0')),
    CONCAT('Description for event ', t.id),
    DATEADD('DAY', (t.id - 50), CURRENT_TIMESTAMP), -- Some past, some future
    DATEADD('DAY', (t.id - 49), CURRENT_TIMESTAMP), -- End 1 day after start
    CASE 
        WHEN t.id - 50 < -30 THEN 'ACTIVE' -- Changed from 'COMPLETED' to valid CommonStatus
        WHEN t.id - 50 < -10 THEN 'INACTIVE' -- Changed from 'CANCELED' to valid CommonStatus
        WHEN t.id - 50 < 0 THEN 'ACTIVE'
        WHEN t.id - 50 < 10 THEN 'ACTIVE'
        WHEN t.id - 50 < 20 THEN 'PENDING'
        ELSE 'ACTIVE' -- Changed from 'DRAFT' to valid CommonStatus
    END,
    CONCAT('user', (t.id % 100) + 1), -- Created by users 1-100
    DATEADD('DAY', -(100 - t.id), CURRENT_TIMESTAMP), -- Older events created earlier
    CONCAT('user', (t.id % 100) + 1),
    DATEADD('DAY', -(100 - t.id), CURRENT_TIMESTAMP)
FROM generate_series(1, 100) AS t(id);

-- Generate 150 event locations across provinces and events
INSERT INTO event_locations (id, name, code, description, event_id, region_id, max_spin, quantity, win_probability, status, created_by, created_at, updated_by, updated_at)
SELECT 
    t.id,
    CASE
        WHEN t.id <= 6 THEN (ARRAY['Conference Center', 'Beach Resort', 'Exhibition Hall', 'Corporate Office', 'City Park', 'Grand Hotel'])[t.id]
        ELSE CONCAT(
            (ARRAY['North', 'South', 'East', 'West', 'Central'])[1 + (t.id % 5)],
            ' ',
            (ARRAY['Hall', 'Center', 'Venue', 'Stadium', 'Theater', 'Hotel', 'Garden', 'Building'])[1 + ((t.id * 2) % 8)],
            ' ',
            (t.id / 8) + 1
        )
    END,
    CONCAT('LOC-', LPAD(CAST(t.id AS VARCHAR), 3, '0')),
    CONCAT('Description for location ', t.id),
    1 + (t.id % 100), -- Event ID (1-100) - many locations can point to the same event
    1 + (t.id % 10),  -- Region ID (1-10)
    100, -- Default max_spin value
    t.id * 10, -- Added quantity with a value (10 * location id)
    (t.id % 100) / 100.0, -- Added win_probability as a decimal between 0.01 and 0.99
    CASE 
        WHEN t.id % 10 = 0 THEN 'INACTIVE'
        WHEN t.id % 20 = 0 THEN 'PENDING'
        ELSE 'ACTIVE'
    END,
    CONCAT('user', 1 + (t.id % 4)), -- Created by first 4 users
    DATEADD('DAY', -(150 - t.id), CURRENT_TIMESTAMP),
    CONCAT('user', 1 + (t.id % 4)),
    DATEADD('DAY', -(150 - t.id), CURRENT_TIMESTAMP)
FROM generate_series(1, 150) AS t(id);

-- Generate 100 golden hours tied to events
INSERT INTO golden_hours (id, event_location_id, start_time, end_time, multiplier, status, created_by, created_at, updated_by, updated_at)
SELECT 
    t.id,
    ((t.id - 1) % 150) + 1, -- Event Location ID (1-150)
    DATEADD('DAY', (t.id - 50), CURRENT_TIMESTAMP), -- Same pattern as events
    DATEADD('HOUR', 2, DATEADD('DAY', (t.id - 50), CURRENT_TIMESTAMP)),
    (t.id % 5) + 1.0, -- Multiplier between 1 and 5
    CASE 
        WHEN t.id % 10 = 0 THEN 'INACTIVE'
        WHEN t.id % 20 = 0 THEN 'PENDING'
        ELSE 'ACTIVE'
    END,
    CONCAT('user', 1 + (t.id % 4)),
    DATEADD('DAY', -(100 - t.id), CURRENT_TIMESTAMP),
    CONCAT('user', 1 + (t.id % 4)),
    DATEADD('DAY', -(100 - t.id), CURRENT_TIMESTAMP)
FROM generate_series(1, 100) AS t(id);

-- Generate 200 rewards across events
INSERT INTO rewards (id, event_location_id, name, code, description, stock, reward_value, win_probability, status, created_by, created_at, updated_by, updated_at)
SELECT 
    t.id,
    ((t.id - 1) % 150) + 1, -- Event Location ID (1-150)
    CASE
        WHEN t.id <= 6 THEN (ARRAY['First Prize', 'Second Prize', 'Third Prize', 'Main Prize', 'Lucky Draw', 'Grand Prize'])[t.id]
        ELSE CONCAT(
            (ARRAY['Gold', 'Silver', 'Bronze', 'Platinum', 'Diamond', 'Special', 'Premium', 'Standard', 'Elite', 'Basic'])[1 + (t.id % 10)],
            ' ',
            (ARRAY['Prize', 'Award', 'Gift', 'Reward', 'Package'])[1 + ((t.id * 3) % 5)]
        )
    END,
    CONCAT('REW-', LPAD(CAST(t.id AS VARCHAR), 3, '0')),
    CONCAT('Description for reward ', t.id),
    10 * (t.id % 10) + 1, -- Using stock instead of quantity
    (t.id % 1000) * 1.0, -- Value between 0 and 999.0
    (t.id % 100) / 100.0, -- Probability between 0.01 and 0.99
    CASE 
        WHEN t.id % 10 = 0 THEN 'INACTIVE'
        WHEN t.id % 20 = 0 THEN 'PENDING'
        ELSE 'ACTIVE'
    END,
    CONCAT('user', 1 + (t.id % 4)),
    DATEADD('DAY', -(200 - t.id), CURRENT_TIMESTAMP),
    CONCAT('user', 1 + (t.id % 4)),
    DATEADD('DAY', -(200 - t.id), CURRENT_TIMESTAMP)
FROM generate_series(1, 200) AS t(id);

-- Generate 100 participants
INSERT INTO participants (id, name, code, phone, province_id, address, status, created_by, created_at, updated_by, updated_at)
SELECT 
    t.id,
    CONCAT(
        (ARRAY['John', 'Jane', 'Robert', 'Mary', 'William', 'Linda', 'Michael', 'Elizabeth', 'David', 'Sarah', 
               'Thomas', 'Patricia', 'Charles', 'Jennifer', 'Daniel', 'Barbara', 'Paul', 'Susan', 'Mark', 'Jessica'])[1 + (t.id % 20)],
        ' ',
        (ARRAY['Smith', 'Johnson', 'Williams', 'Jones', 'Brown', 'Davis', 'Miller', 'Wilson', 'Moore', 'Taylor',
               'Anderson', 'Thomas', 'Jackson', 'White', 'Harris', 'Martin', 'Thompson', 'Garcia', 'Martinez', 'Robinson'])[1 + ((t.id * 7) % 20)]
    ),
    CONCAT('PART-', LPAD(CAST(t.id AS VARCHAR), 3, '0')),
    CONCAT('090', LPAD(CAST(t.id AS VARCHAR), 7, '0')),
    1 + (t.id % 100), -- Province ID (1-100)
    CONCAT(  -- Add address data for each participant
        t.id, ' ', 
        (ARRAY['Main St', 'Broadway', 'Park Ave', 'Oak St', 'Maple Ave', 'Cedar Ln', 'Pine Rd', 'Washington Blvd', 'Lake Dr', 'River Rd'])[1 + (t.id % 10)],
        ', ',
        (ARRAY['Apt', 'Suite', 'Unit', 'Floor', 'Room'])[1 + ((t.id * 3) % 5)],
        ' ',
        ((t.id * 7) % 100) + 1
    ),
    CASE 
        WHEN t.id % 10 = 0 THEN 'INACTIVE'
        WHEN t.id % 20 = 0 THEN 'PENDING'
        ELSE 'ACTIVE'
    END,
    CONCAT('user', 1 + (t.id % 4)),
    DATEADD('DAY', -(100 - t.id), CURRENT_TIMESTAMP),
    CONCAT('user', 1 + (t.id % 4)),
    DATEADD('DAY', -(100 - t.id), CURRENT_TIMESTAMP)
FROM generate_series(1, 100) AS t(id);

-- Generate 200 participant events
INSERT INTO participant_events (id, participant_id, event_id, event_location_id, spins_remaining, status, created_by, created_at, updated_by, updated_at)
SELECT 
    t.id,
    ((t.id - 1) % 100) + 1, -- Participant ID (1-100)
    ((t.id - 1) % 100) + 1, -- Event ID (1-100)
    ((t.id - 1) % 150) + 1, -- Event Location ID (1-150)
    10, -- Default spins_remaining - matches the value in ParticipantEvent#joinEvent method
    CASE 
        WHEN t.id % 3 = 0 THEN 'ACTIVE' -- Changed from 'ATTENDED' to valid CommonStatus
        WHEN t.id % 5 = 0 THEN 'INACTIVE' -- Changed from 'CANCELED' to valid CommonStatus
        ELSE 'ACTIVE' -- Changed from 'REGISTERED' to valid CommonStatus
    END,
    CONCAT('user', 1 + (t.id % 4)),
    DATEADD('DAY', -(200 - t.id), CURRENT_TIMESTAMP),
    CONCAT('user', 1 + (t.id % 4)),
    DATEADD('DAY', -(200 - t.id), CURRENT_TIMESTAMP)
FROM generate_series(1, 200) AS t(id);

-- Generate 100 spin histories
INSERT INTO spin_histories (id, participant_event_id, reward_id, golden_hour_id, spin_time, win, status, created_by, created_at, updated_by, updated_at)
SELECT 
    t.id,
    ((t.id - 1) % 200) + 1, -- Participant Event ID (1-200)
    ((t.id - 1) % 200) + 1, -- Reward ID (1-200)
    CASE 
        WHEN t.id % 4 = 0 THEN ((t.id - 1) % 100) + 1 -- Link some spins to golden hours (1-100)
        ELSE NULL -- Some spins don't have golden hours
    END,
    DATEADD('DAY', -(100 - t.id), CURRENT_TIMESTAMP), -- Spin time
    CASE 
        WHEN t.id % 3 = 0 THEN true
        ELSE false
    END, -- Added win column
    CASE 
        WHEN t.id % 10 = 0 THEN 'INACTIVE'
        ELSE 'ACTIVE'
    END,
    CONCAT('user', 1 + (t.id % 4)),
    DATEADD('DAY', -(100 - t.id), CURRENT_TIMESTAMP),
    CONCAT('user', 1 + (t.id % 4)),
    DATEADD('DAY', -(100 - t.id), CURRENT_TIMESTAMP)
FROM generate_series(1, 100) AS t(id);

-- Generate 300 audit logs
INSERT INTO audit_logs (id, action, entity, entity_id, details, status, created_by, created_at, updated_by, updated_at)
SELECT 
    t.id,
    CASE 
        WHEN t.id % 4 = 0 THEN 'CREATE'
        WHEN t.id % 4 = 1 THEN 'UPDATE'
        WHEN t.id % 4 = 2 THEN 'DELETE'
        ELSE 'VIEW'
    END,
    CASE 
        WHEN t.id % 10 = 0 THEN 'USER'
        WHEN t.id % 10 = 1 THEN 'ROLE'
        WHEN t.id % 10 = 2 THEN 'PERMISSION'
        WHEN t.id % 10 = 3 THEN 'EVENT'
        WHEN t.id % 10 = 4 THEN 'EVENT_LOCATION'
        WHEN t.id % 10 = 5 THEN 'GOLDEN_HOUR'
        WHEN t.id % 10 = 6 THEN 'REWARD'
        WHEN t.id % 10 = 7 THEN 'PARTICIPANT'
        WHEN t.id % 10 = 8 THEN 'PARTICIPANT_EVENT'
        ELSE 'SPIN_HISTORY'
    END,
    (t.id % 100) + 1, -- Entity ID (1-100)
    CONCAT('Details for audit log ', t.id),
    'ACTIVE',
    CONCAT('user', 1 + (t.id % 4)),
    DATEADD('DAY', -(300 - t.id), CURRENT_TIMESTAMP),
    CONCAT('user', 1 + (t.id % 4)),
    DATEADD('DAY', -(300 - t.id), CURRENT_TIMESTAMP)
FROM generate_series(1, 300) AS t(id);

-- Insert 20 configurations
INSERT INTO configurations (id, config_key, config_value, description, status, created_by, created_at, updated_by, updated_at)
SELECT 
    t.id,
    CASE
        WHEN t.id = 1 THEN 'MAX_DAILY_SPINS'
        WHEN t.id = 2 THEN 'DEFAULT_GOLDEN_HOUR_MULTIPLIER'
        WHEN t.id = 3 THEN 'SYSTEM_MAINTENANCE_MODE'
        ELSE CONCAT(
            (ARRAY['SITE', 'APP', 'EMAIL', 'SMS', 'REWARD', 'EVENT', 'USER', 'SECURITY', 'NOTIFICATION', 'LOGGING'])[1 + ((t.id - 4) % 10)],
            '_',
            (ARRAY['TIMEOUT', 'MAX_COUNT', 'DEFAULT_VALUE', 'ENABLED', 'RETENTION_DAYS'])[1 + ((t.id - 4) % 5)],
            '_',
            t.id  -- Add ID to ensure uniqueness
        )
    END,
    CASE
        WHEN t.id = 1 THEN '3'
        WHEN t.id = 2 THEN '2'
        WHEN t.id = 3 THEN 'false'
        WHEN t.id % 5 = 0 THEN 'false'
        WHEN t.id % 5 = 1 THEN 'true'
        WHEN t.id % 5 = 2 THEN CAST(t.id AS VARCHAR)
        WHEN t.id % 5 = 3 THEN CAST(t.id * 10 AS VARCHAR)
        ELSE CONCAT('value-', t.id)
    END,
    CONCAT('Description for configuration ', t.id),
    CASE 
        WHEN t.id % 10 = 0 THEN 'INACTIVE'
        ELSE 'ACTIVE'
    END,
    CONCAT('user', 1 + (t.id % 4)),
    DATEADD('DAY', -(20 - t.id), CURRENT_TIMESTAMP),
    CONCAT('user', 1 + (t.id % 4)),
    DATEADD('DAY', -(20 - t.id), CURRENT_TIMESTAMP)
FROM generate_series(1, 20) AS t(id);

-- Insert 50 blacklisted tokens
INSERT INTO blacklisted_tokens (id, token, token_type, expiration_time, user_id, status, created_by, created_at, updated_by, updated_at)
SELECT 
    t.id,
    CONCAT('eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyLWlkLTEyMzQ1Njc4OTAiLCJuYW1lIjoiVXNlciAnLCB0LmlkLCAnIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV', t.id),
    'ACCESS',
    DATEADD('DAY', (t.id - 25), CURRENT_TIMESTAMP), -- Some expired, some active, some future
    (t.id % 100) + 1, -- User ID (1-100)
    CASE 
        WHEN t.id % 10 = 0 THEN 'INACTIVE'
        ELSE 'ACTIVE'
    END,
    CONCAT('user', 1 + (t.id % 4)),
    DATEADD('DAY', -(50 - t.id), CURRENT_TIMESTAMP),
    CONCAT('user', 1 + (t.id % 4)),
    DATEADD('DAY', -(50 - t.id), CURRENT_TIMESTAMP)
FROM generate_series(1, 50) AS t(id);

-- Remove all ALTER SEQUENCE statements as they're causing errors
-- H2 will automatically manage sequences based on the inserted data