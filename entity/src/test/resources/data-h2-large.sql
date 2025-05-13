-- Generate large test dataset
DELETE FROM spin_histories;
DELETE FROM golden_hours;
DELETE FROM reward_events;
DELETE FROM rewards;
DELETE FROM participant_events;
DELETE FROM participants;
DELETE FROM event_locations;
DELETE FROM region_province;
DELETE FROM provinces;
DELETE FROM events;
DELETE FROM regions;
-- Clear existing auth data
DELETE FROM blacklisted_tokens;
DELETE FROM role_permissions;
DELETE FROM users;
DELETE FROM permissions;
DELETE FROM roles;

-- Insert Regions with ALL required columns
INSERT INTO regions (id, code, name, status, created_by, created_at, updated_by, updated_at, version)
VALUES
    (1, 'NORTH', 'Northern Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
    (2, 'CENTRAL', 'Central Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
    (3, 'SOUTH', 'Southern Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Insert Provinces - Fixed column count to match 9 columns required
INSERT INTO provinces (id, code, name, status, created_by, created_at, updated_by, updated_at, version)
SELECT
    x,
    'PROV_' || x,
    'Province ' || x,
    'ACTIVE',
    'system',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    0
FROM SYSTEM_RANGE(1, 200);

-- Link Regions and Provinces - Fixed to only reference existing region IDs (1, 2, 3)
INSERT INTO region_province (province_id, region_id)
SELECT
    p.id,
    1 + MOD(p.id, 3) -- Only use region IDs 1, 2, 3 that we created above
FROM provinces p;

-- Add secondary region for some provinces (every other province)
INSERT INTO region_province (province_id, region_id)
SELECT
    p.id,
    1 + MOD((p.id * 2), 3) -- Only use region IDs 1, 2, 3
FROM provinces p
WHERE MOD(p.id, 2) = 0
AND NOT EXISTS (
    SELECT 1 FROM region_province rp
    WHERE rp.province_id = p.id AND rp.region_id = 1 + MOD((p.id * 2), 3)
);

-- Add tertiary region for some provinces (every third province)
INSERT INTO region_province (province_id, region_id)
SELECT
    p.id,
    CASE WHEN MOD(p.id * 13, 3) = 0 THEN 3 ELSE 1 END -- Only use region IDs 1 or 3, which are guaranteed to exist
FROM provinces p
WHERE MOD(p.id, 3) = 0
AND NOT EXISTS (
    SELECT 1 FROM region_province rp
    WHERE rp.province_id = p.id AND rp.region_id = CASE WHEN MOD(p.id * 13, 3) = 0 THEN 3 ELSE 1 END
);

-- Insert Events - Add ALL required columns
INSERT INTO events (
    id,
    name,
    code,
    description,
    start_time,
    end_time,
    status,
    created_by,
    created_at,
    updated_by,
    updated_at,
    version
)
SELECT
    x,
    'Event ' || x,
    'EVENT_' || x,
    'Description for event ' || x,
    DATEADD('DAY', -30 + MOD(x, 60), CURRENT_TIMESTAMP), -- Start time
    DATEADD('DAY', 30 + MOD(x, 60), CURRENT_TIMESTAMP), -- End time
    'ACTIVE',
    'system',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    0
FROM SYSTEM_RANGE(1, 200);

-- Insert Event Locations - Remove the name column to match the EventLocation entity
INSERT INTO event_locations (
    event_id,
    region_id,
    max_spin,
    today_spin,
    daily_spin_dist_rate,
    description,
    status,
    created_by,
    created_at,
    updated_by,
    updated_at,
    version
)
SELECT
    e.id,
    r.id,
    100 + MOD(e.id * r.id, 200), -- max_spin
    50 + MOD(e.id * r.id, 100), -- today_spin
    0.1 + (MOD(e.id * r.id, 9) / 10.0), -- daily_spin_distributing_rate
    'Event location for event ' || e.id || ' in region ' || r.id,
    'ACTIVE',
    'system',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    0
FROM events e
CROSS JOIN regions r
WHERE EXISTS (
    SELECT 1 FROM provinces p JOIN region_province rp
    WHERE rp.region_id = r.id AND MOD(p.id, 10) <> 0
);

-- Insert Golden Hours - Add ALL required columns
INSERT INTO golden_hours (
    id,
    event_id,
    region_id,
    start_time,
    end_time,
    multiplier,
    status,
    created_by,
    created_at,
    updated_by,
    updated_at,
    version
)
SELECT
    ROW_NUMBER() OVER (ORDER BY el.event_id, el.region_id, h.x),
    el.event_id,
    el.region_id,
    DATEADD('HOUR', MOD(el.event_id + h.x, 24), CURRENT_TIMESTAMP), -- Start time
    DATEADD('HOUR', MOD(el.event_id + h.x, 24) + 2, CURRENT_TIMESTAMP), -- End time (2 hours later)
    1.0 + (MOD(el.event_id * h.x, 5) / 2.0), -- multiplier between 1.0 and 3.5
    'ACTIVE',
    'system',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    0
FROM event_locations el
CROSS JOIN (SELECT x FROM SYSTEM_RANGE(1, 3)) h -- 3 golden hours per location
WHERE el.status = 'ACTIVE';

-- Insert Rewards - Add ALL required columns
INSERT INTO rewards (
    id,
    name,
    code,
    description,
    status,
    created_by,
    created_at,
    updated_by,
    updated_at,
    version
)
SELECT
    ROW_NUMBER() OVER (ORDER BY e.id, r.id, s.x),
    'Reward ' || ROW_NUMBER() OVER (ORDER BY e.id, r.id, s.x),
    'REWARD_' || ROW_NUMBER() OVER (ORDER BY e.id, r.id, s.x),
    'Description for reward ' || ROW_NUMBER() OVER (ORDER BY e.id, r.id, s.x),
    'ACTIVE',
    'system',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    0
FROM events e
CROSS JOIN regions r
CROSS JOIN (SELECT x FROM SYSTEM_RANGE(1, 10)) s -- 10 rewards per event-region combination
WHERE EXISTS (
    SELECT 1 FROM event_locations el
    WHERE el.event_id = e.id AND el.region_id = r.id
)
LIMIT 2000;

-- Insert Reward Events - Add ALL required columns
INSERT INTO reward_events (
    reward_id,
    event_id,
    region_id,
    quantity,
    today_quantity,
    status,
    created_by,
    created_at,
    updated_by,
    updated_at,
    version
)
SELECT
    r.id,
    el.event_id,
    el.region_id,
    1000 - MOD(r.id * 7, 500), -- quantity
    MOD(r.id * 11, 200), -- today_quantity
    'ACTIVE',
    'system',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    0
FROM rewards r
JOIN event_locations el ON 1=1 -- Cartesian product
WHERE el.status = 'ACTIVE'
LIMIT 5000;

-- Insert Participants - FIXED: Added address and email columns that were missing
INSERT INTO participants (
    id,
    name,
    code,
    phone,
    email,
    address,
    province_id,
    status,
    created_by,
    created_at,
    updated_by,
    updated_at,
    version
)
SELECT
    x,
    'Participant ' || x,
    'PART_' || x,
    '0' || (900000000 + MOD(x * 17, 100000000)), -- Phone
    'participant' || x || '@example.com', -- Email
    'Address ' || x || ', Street ' || MOD(x, 100) || ', District ' || MOD(x, 20), -- Address
    1 + MOD(x, 200), -- Province ID
    'ACTIVE',
    'system',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    0
FROM SYSTEM_RANGE(1, 5000);

-- Insert Participant Events - Add ALL required columns
INSERT INTO participant_events (
    participant_id,
    event_id,
    region_id,
    spins_remaining,
    status,
    created_by,
    created_at,
    updated_by,
    updated_at,
    version
)
SELECT
    p.id,
    el.event_id,
    el.region_id,
    5 + MOD(p.id * el.event_id, 10), -- spins_remaining between 5 and 14
    'ACTIVE',
    'system',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    0
FROM
    (SELECT id FROM participants WHERE status = 'ACTIVE' LIMIT 5000) p
CROSS JOIN
    (SELECT event_id, region_id FROM event_locations WHERE status = 'ACTIVE' ORDER BY event_id, region_id LIMIT 500) el
WHERE
    MOD(p.id * el.event_id, 10) < 4  -- Control density of participation (40% chance)
LIMIT 10000;

-- Insert Spin Histories - Add ALL required columns
INSERT INTO spin_histories (
    id,
    participant_id,
    participant_event_id,
    participant_region_id,
    spin_time,
    reward_id,
    reward_event_id,
    reward_region_id,
    golden_hour_id,
    win,
    status,
    created_by,
    created_at,
    updated_by,
    updated_at,
    version
)
SELECT
    ROW_NUMBER() OVER (ORDER BY pe.participant_id),
    pe.participant_id,
    pe.event_id, -- participant_event_id
    pe.region_id, -- participant_region_id
    DATEADD('MINUTE', -1 * MOD(pe.participant_id, 10000), CURRENT_TIMESTAMP), -- spin_time
    CASE WHEN MOD(pe.participant_id, 4) = 0 THEN re.reward_id ELSE NULL END, -- reward_id
    CASE WHEN MOD(pe.participant_id, 4) = 0 THEN re.event_id ELSE NULL END, -- reward_event_id
    CASE WHEN MOD(pe.participant_id, 4) = 0 THEN re.region_id ELSE NULL END, -- reward_region_id
    CASE WHEN MOD(pe.participant_id, 10) = 0 THEN MOD(pe.participant_id, 1000) + 1 ELSE NULL END, -- golden_hour_id
    CASE WHEN MOD(pe.participant_id, 4) = 0 THEN TRUE ELSE FALSE END, -- win
    'ACTIVE',
    'system',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    0
FROM participant_events pe
LEFT JOIN reward_events re ON (MOD(pe.participant_id, 4) = 0 AND re.event_id = pe.event_id AND re.region_id = pe.region_id)
-- Ensure we only include records where either:
-- 1. The participant doesn't win (MOD(pe.participant_id, 4) != 0), so reward fields are NULL, or
-- 2. The participant wins and we have a valid reward_events record with matching event_id, region_id, and reward_id
WHERE (MOD(pe.participant_id, 4) != 0) OR
      (re.reward_id IS NOT NULL AND
       EXISTS (SELECT 1 FROM reward_events re2
               WHERE re2.event_id = re.event_id
               AND re2.region_id = re.region_id
               AND re2.reward_id = re.reward_id))
LIMIT 20000;

-- Insert Roles with ALL required columns
INSERT INTO roles (id, created_by, created_at, updated_by, updated_at, status, role_type, description, display_order, version)
VALUES
(1001, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_ADMIN', 'Administrator with full system access', 10, 0),
(1002, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_USER', 'Regular user with standard access', 20, 0),
(1003, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_MANAGER', 'Manager with departmental access', 30, 0),
(1004, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_PARTICIPANT', 'Participant with specific access', 40, 0),
(1005, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_GUEST', 'Guest with limited access', 50, 0),
(1006, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_OPERATOR', 'System operator', 60, 0);

-- Insert Permissions with ALL required fields
INSERT INTO permissions (id, created_by, created_at, updated_by, updated_at, status, name, permission_type, description, version)
VALUES
-- Dashboard permissions
(1001, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'READ_CONFIGURATION', 'READ', 'View dashboard configuration', 0),
(1002, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'UPDATE_CONFIGURATION', 'WRITE', 'Configure dashboard settings', 0),

-- User management
(1003, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'READ_USER', 'READ', 'View user list', 0),
(1004, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'CREATE_USER', 'WRITE', 'Create new users', 0),
(1005, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'UPDATE_USER', 'WRITE', 'Edit existing users', 0),
(1006, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'DELETE_USER', 'WRITE', 'Delete users', 0),

-- Event management
(1007, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'READ_EVENT', 'READ', 'View events', 0),
(1008, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'CREATE_EVENT', 'WRITE', 'Create events', 0),
(1009, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'UPDATE_EVENT', 'WRITE', 'Edit events', 0),
(1010, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'DELETE_EVENT', 'WRITE', 'Delete events', 0),

-- Participant management
(1011, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'READ_PARTICIPANT', 'READ', 'View participants', 0),
(1012, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'CREATE_PARTICIPANT', 'WRITE', 'Register participants', 0),
(1013, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'UPDATE_PARTICIPANT', 'WRITE', 'Edit participants', 0),
(1014, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'DELETE_PARTICIPANT', 'WRITE', 'Delete participants', 0),

-- Configuration management
(1015, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'CREATE_CONFIGURATION', 'WRITE', 'Create configurations', 0),
(1016, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'DELETE_CONFIGURATION', 'WRITE', 'Delete configurations', 0);

-- Associate permissions with roles
-- Admin role gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1001, id FROM permissions WHERE status = 'ACTIVE';

-- Manager role gets most permissions except admin-level ones
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1003, id FROM permissions
WHERE status = 'ACTIVE' AND id NOT IN (1006, 1016);  -- Exclude admin permissions

-- Operator gets operational permissions
INSERT INTO role_permissions (role_id, permission_id)
VALUES
(1006, 1001), (1006, 1003), (1006, 1007), (1006, 1009),
(1006, 1011), (1006, 1013), (1006, 1015);

-- Regular user gets basic permissions
INSERT INTO role_permissions (role_id, permission_id)
VALUES
(1002, 1001), (1002, 1007), (1002, 1011);

-- Guest gets minimal permissions
INSERT INTO role_permissions (role_id, permission_id)
VALUES (1005, 1007);

-- Participant role gets participant-specific permissions
INSERT INTO role_permissions (role_id, permission_id)
VALUES (1004, 1007), (1004, 1011);

-- Insert Users - Create at least 1000 users with different roles
INSERT INTO users (id, created_by, created_at, updated_by, updated_at, status, username, password, email, full_name, role_id, version)
SELECT
    x,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    CASE
        WHEN MOD(x, 15) = 0 THEN 'INACTIVE'
        ELSE 'ACTIVE'
    END,
    'user' || LPAD(CAST(x AS VARCHAR), 5, '0'),
    -- BCrypt hash for 'password'
    '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy',
    'user' || LPAD(CAST(x AS VARCHAR), 5, '0') || '@example.com',
    CASE
        WHEN MOD(x, 5) = 0 THEN 'John'
        WHEN MOD(x, 5) = 1 THEN 'Mary'
        WHEN MOD(x, 5) = 2 THEN 'Robert'
        WHEN MOD(x, 5) = 3 THEN 'Linda'
        ELSE 'Michael'
    END || ' ' ||
    CASE
        WHEN MOD(x, 7) = 0 THEN 'Smith'
        WHEN MOD(x, 7) = 1 THEN 'Johnson'
        WHEN MOD(x, 7) = 2 THEN 'Williams'
        WHEN MOD(x, 7) = 3 THEN 'Jones'
        WHEN MOD(x, 7) = 4 THEN 'Brown'
        WHEN MOD(x, 7) = 5 THEN 'Miller'
        ELSE 'Davis'
    END,
    CASE -- Distribute users across roles
        WHEN x <= 10 THEN 1001 -- Admin (10)
        WHEN x <= 50 THEN 1003 -- Manager (40)
        WHEN x <= 150 THEN 1006 -- Operator (100)
        WHEN x <= 950 THEN 1002 -- Regular user (800)
        ELSE 1005 -- Guest (remaining)
    END,
    0
FROM SYSTEM_RANGE(1, 2000);

-- Insert special admin users for testing
INSERT INTO users (id, created_by, created_at, updated_by, updated_at, status, username, password, email, full_name, role_id, version)
VALUES
(10001, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'admin',
'$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'admin@example.com', 'System Administrator', 1001, 0),
(10002, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'manager',
'$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'manager@example.com', 'System Manager', 1003, 0),
(10003, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'operator',
'$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'operator@example.com', 'System Operator', 1006, 0),
(10004, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'user',
'$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'user@example.com', 'Regular User', 1002, 0),
(10005, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'guest',
'$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy', 'guest@example.com', 'Guest User', 1005, 0);

-- Insert Blacklisted Tokens for testing authentication
INSERT INTO blacklisted_tokens (id, created_by, created_at, updated_by, updated_at, status, token, user_id, expiration_time, token_type, version)
VALUES
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE',
'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMDAwMDEiLCJpYXQiOjE2MTYyMzkwMjJ9.tokenpart1',
1, DATEADD('HOUR', 24, CURRENT_TIMESTAMP), 'Bearer', 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE',
'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMDAwMDIiLCJpYXQiOjE2MTYyMzkwMjJ9.tokenpart2',
2, DATEADD('HOUR', 24, CURRENT_TIMESTAMP), 'Bearer', 0),
(3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE',
'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMDAwMDMiLCJpYXQiOjE2MTYyMzkwMjJ9.tokenpart3',
3, DATEADD('HOUR', -24, CURRENT_TIMESTAMP), 'Bearer', 0);

-- Insert more blacklisted tokens for users
INSERT INTO blacklisted_tokens (id, created_by, created_at, updated_by, updated_at, status, token, user_id, expiration_time, token_type, version)
SELECT
    100 + x,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'ACTIVE',
    'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyJyB8fCB4IHx8ICciLCJpYXQiOjE2MTYyMzkwMjJ9.' || 'token' || x,
    x,
    DATEADD('HOUR', MOD(x, 48), CURRENT_TIMESTAMP), -- Varying expiration times
    'Bearer', -- Default token type for all entries
    0
FROM SYSTEM_RANGE(1, 100)
WHERE MOD(x, 10) = 0;
