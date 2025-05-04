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

-- Insert Regions
INSERT INTO regions (id, code, name, status, created_by, created_at)
VALUES 
    (1, 'NORTH', 'Northern Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP),
    (2, 'CENTRAL', 'Central Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP),
    (3, 'SOUTH', 'Southern Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP),
    (4, 'EAST', 'Eastern Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP),
    (5, 'WEST', 'Western Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP),
    (6, 'NORTH_EAST', 'North Eastern Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP),
    (7, 'NORTH_WEST', 'North Western Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP),
    (8, 'SOUTH_EAST', 'South Eastern Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP),
    (9, 'SOUTH_WEST', 'South Western Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP),
    (10, 'CENTRAL_NORTH', 'Central North Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP);

-- Insert Provinces - Increase to 200 provinces for broader geographical coverage
INSERT INTO provinces (id, code, name, status, created_by, created_at)
SELECT 
    x,
    'PROV_' || LPAD(CAST(x AS VARCHAR), 4, '0'),
    'Province ' || CAST(x AS VARCHAR),
    CASE
        WHEN MOD(x, 10) = 0 THEN 'INACTIVE'
        ELSE 'ACTIVE'
    END,
    'system',
    CURRENT_TIMESTAMP
FROM SYSTEM_RANGE(1, 200);

-- Link Regions and Provinces - Each province belongs to 1-3 regions to create complex relationships
INSERT INTO region_province (province_id, region_id)
SELECT 
    p.id,
    1 + MOD(p.id * 3, 10) -- Primary region
FROM provinces p;

-- Add secondary region for some provinces (every other province)
INSERT INTO region_province (province_id, region_id)
SELECT 
    p.id,
    1 + MOD(p.id * 7, 10) -- Secondary region
FROM provinces p
WHERE MOD(p.id, 2) = 0
AND NOT EXISTS (
    SELECT 1 FROM region_province rp 
    WHERE rp.province_id = p.id AND rp.region_id = 1 + MOD(p.id * 7, 10)
);

-- Add tertiary region for some provinces (every third province)
INSERT INTO region_province (province_id, region_id)
SELECT 
    p.id,
    1 + MOD(p.id * 13, 10) -- Tertiary region
FROM provinces p
WHERE MOD(p.id, 3) = 0
AND NOT EXISTS (
    SELECT 1 FROM region_province rp 
    WHERE rp.province_id = p.id AND rp.region_id = 1 + MOD(p.id * 13, 10)
);

-- Insert Events - Increase to 200 events
INSERT INTO events (
    id,
    code,
    name,
    status,
    start_time,
    end_time,
    created_by,
    created_at,
    updated_by,
    updated_at,
    version
)
SELECT 
    x,
    'EVENT_' || LPAD(CAST(x AS VARCHAR), 4, '0'),
    'Event ' || CAST(x AS VARCHAR),
    CASE
        WHEN MOD(x, 10) = 0 THEN 'INACTIVE'
        ELSE 'ACTIVE'
    END,
    DATEADD('DAY', x - 100, CURRENT_TIMESTAMP), -- Start some events in the past
    DATEADD('DAY', x + 100, CURRENT_TIMESTAMP), -- Events with varying durations
    'system',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    0
FROM SYSTEM_RANGE(1, 200);

-- Insert Event Locations - Create an event location for each event in every region
INSERT INTO event_locations (
    event_id,
    region_id,
    province_id,
    name,
    code,
    status,
    max_spin,
    quantity,
    win_probability,
    created_by,
    created_at,
    updated_by,
    updated_at,
    version
)
SELECT 
    e.id,
    r.id,
    (
        SELECT MIN(p.id)
        FROM provinces p
        JOIN region_province rp ON p.id = rp.province_id
        WHERE rp.region_id = r.id AND MOD(p.id, 10) <> 0 -- Ensure province is active
        LIMIT 1
    ),
    'Location ' || e.id || '-' || r.id,
    'LOC_' || LPAD(CAST(e.id AS VARCHAR), 4, '0') || '_' || r.id,
    CASE
        WHEN MOD(e.id + r.id, 20) = 0 THEN 'INACTIVE' -- Some locations are inactive
        ELSE e.status
    END,
    100 + MOD(e.id * r.id, 900), -- Max spins between 100-999
    50 + MOD(e.id * r.id, 950),  -- Quantity between 50-999
    0.1 + (MOD(e.id * r.id, 90) / 100.0), -- Win probability between 0.1-0.99
    'system',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    0
FROM events e
CROSS JOIN regions r
WHERE EXISTS (
    SELECT 1 FROM provinces p JOIN region_province rp 
    ON p.id = rp.province_id 
    WHERE rp.region_id = r.id AND MOD(p.id, 10) <> 0
);

-- Insert Golden Hours - At least 2 golden hours per active event location
INSERT INTO golden_hours (
    id,
    event_id,
    region_id,
    start_time,
    end_time,
    multiplier,
    max_rewards,
    claimed_rewards,
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
    DATEADD('HOUR', h.x, CURRENT_TIMESTAMP),
    DATEADD('HOUR', h.x + 1 + MOD(h.x, 5), CURRENT_TIMESTAMP), -- Golden hours with varying duration (1-6 hours)
    1.0 + (MOD(el.event_id + el.region_id + h.x, 40) / 10.0), -- Multiplier between 1.0 and 5.0
    10 + MOD(el.event_id * el.region_id + h.x, 90), -- Max rewards between 10-99
    MOD(el.event_id + el.region_id + h.x, 10), -- Some claimed rewards between 0-9
    CASE
        WHEN MOD(el.event_id + el.region_id + h.x, 15) = 0 THEN 'INACTIVE' -- Some golden hours are inactive
        ELSE 'ACTIVE'
    END,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    0
FROM event_locations el
CROSS JOIN (SELECT x FROM SYSTEM_RANGE(1, 3)) h -- 3 golden hours per location
WHERE el.status = 'ACTIVE';

-- Insert Rewards - Create at least 2000 rewards
INSERT INTO rewards (
    id,
    code,
    name,
    status,
    prize_value,
    event_id,
    region_id,
    description,
    created_by,
    created_at,
    updated_by,
    updated_at,
    version
)
SELECT 
    ROW_NUMBER() OVER (ORDER BY e.id, r.id, s.x),
    'RW_' || LPAD(CAST(e.id AS VARCHAR), 4, '0') || '_' || r.id || '_' || s.x,
    CASE 
        WHEN MOD(s.x, 4) = 0 THEN 'Gold Prize'
        WHEN MOD(s.x, 4) = 1 THEN 'Silver Prize'
        WHEN MOD(s.x, 4) = 2 THEN 'Bronze Prize'
        ELSE 'Special Prize'
    END || ' ' || e.id || '-' || r.id || '-' || s.x,
    CASE
        WHEN MOD(e.id + r.id + s.x, 12) = 0 THEN 'INACTIVE' -- Some rewards are inactive
        ELSE 'ACTIVE'
    END,
    CAST((10.0 * (1 + MOD(e.id * r.id * s.x, 1000))) AS DECIMAL(10, 2)), -- Prize values between 10-10,000
    e.id,
    r.id,
    'Description for reward ' || e.id || '-' || r.id || '-' || s.x,
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

-- Insert Reward Events - Link rewards to event locations
INSERT INTO reward_events (
    event_id,
    region_id,
    reward_id,
    status,
    quantity,
    today_quantity,
    created_by,
    created_at,
    updated_by,
    updated_at,
    version
)
SELECT 
    r.event_id,
    r.region_id,
    r.id,
    r.status,
    10 + MOD(r.id, 90), -- Quantity between 10-99
    1 + MOD(r.id, 9),   -- Today's quantity between 1-9
    'system',
    CURRENT_TIMESTAMP,
    'system', 
    CURRENT_TIMESTAMP,
    0
FROM rewards r
WHERE EXISTS (
    SELECT 1 
    FROM event_locations el 
    WHERE el.event_id = r.event_id 
    AND el.region_id = r.region_id
    AND el.status = 'ACTIVE'
);

-- Insert Participants - Create 5000 participants
INSERT INTO participants (
    id,
    code,
    name,
    phone,
    province_id,
    address,
    status,
    created_by,
    created_at,
    updated_by,
    updated_at,
    version
)
SELECT 
    x,
    'PART_' || LPAD(CAST(x AS VARCHAR), 5, '0'),
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
    END || ' ' || x,
    '0' || (900000000 + x),
    1 + MOD(x, 200), -- Province ID between 1-200
    'Address for participant ' || x,
    CASE
        WHEN MOD(x, 15) = 0 THEN 'INACTIVE' -- Some participants are inactive
        ELSE 'ACTIVE'
    END,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    0
FROM SYSTEM_RANGE(1, 5000);

-- Insert Participant Events - Create at least 10000 participation records
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
SELECT DISTINCT
    p.id,
    el.event_id,
    el.region_id,
    1 + MOD(p.id * el.event_id, 20), -- Spins remaining between 1-20
    CASE
        WHEN MOD(p.id + el.event_id, 25) = 0 THEN 'INACTIVE' -- Some participations are inactive
        ELSE 'ACTIVE'
    END,
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

-- Insert Spin Histories - Create at least 20000 spin records
INSERT INTO spin_histories (
    id,
    event_id,
    participant_id,
    participant_event_id,
    participant_region_id,
    reward_id,
    reward_event_id,
    reward_region_id,
    golden_hour_id,
    spin_time,
    win,
    wheel_position,
    multiplier,
    status,
    created_by,
    created_at,
    updated_by,
    updated_at,
    version
)
SELECT 
    ROW_NUMBER() OVER (ORDER BY pe.participant_id, pe.event_id),
    pe.event_id,
    pe.participant_id,
    pe.event_id,
    pe.region_id,
    CASE
        WHEN MOD(pe.participant_id + pe.event_id, 4) = 0 THEN reward.id -- 25% win rate
        ELSE NULL
    END,
    CASE
        WHEN MOD(pe.participant_id + pe.event_id, 4) = 0 THEN pe.event_id
        ELSE NULL
    END,
    CASE
        WHEN MOD(pe.participant_id + pe.event_id, 4) = 0 THEN pe.region_id
        ELSE NULL
    END,
    CASE
        WHEN MOD(pe.participant_id + pe.event_id, 10) = 0 THEN gh.id -- 10% in golden hour
        ELSE NULL
    END,
    DATEADD('SECOND', -1 * (pe.participant_id * pe.event_id % 86400), CURRENT_TIMESTAMP), -- Spread spins over last 24 hours
    MOD(pe.participant_id + pe.event_id, 4) = 0, -- 25% win rate
    MOD(pe.participant_id * pe.event_id, 360), -- Wheel position between 0-359
    CASE
        WHEN MOD(pe.participant_id + pe.event_id, 10) = 0 THEN 1.5 + (MOD(pe.participant_id, 10) / 10.0) -- Multiplier for golden hour spins
        ELSE 1.0 -- Regular multiplier
    END,
    'ACTIVE',
    'system',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    0
FROM (
    SELECT pe.participant_id, pe.event_id, pe.region_id
    FROM participant_events pe
    WHERE pe.status = 'ACTIVE'
    LIMIT 20000
) pe
LEFT JOIN (
    SELECT gh.id, gh.event_id, gh.region_id
    FROM golden_hours gh
    WHERE gh.status = 'ACTIVE'
    LIMIT 1000
) gh ON gh.event_id = pe.event_id AND gh.region_id = pe.region_id AND MOD(pe.participant_id + pe.event_id, 10) = 0
LEFT JOIN (
    SELECT r.id, r.event_id, r.region_id
    FROM rewards r
    WHERE r.status = 'ACTIVE'
    LIMIT 1000
) reward ON reward.event_id = pe.event_id AND reward.region_id = pe.region_id AND MOD(pe.participant_id + pe.event_id, 4) = 0;

-- Insert Roles
INSERT INTO roles (id, created_by, created_at, updated_by, updated_at, status, role_type, description, display_order, version)
VALUES 
(1001, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_ADMIN', 'Administrator with full system access', 10, 0),
(1002, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_USER', 'Regular user with standard access', 20, 0),
(1003, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_MANAGER', 'Manager with departmental access', 30, 0),
(1004, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_PARTICIPANT', 'Event participant with limited access', 40, 0),
(1005, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_GUEST', 'Guest with limited access', 50, 0),
(1006, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'ROLE_OPERATOR', 'System operator', 60, 0);

-- Insert Permissions
INSERT INTO permissions (id, created_by, created_at, updated_by, updated_at, status, name, type, description, version)
VALUES 
-- Dashboard permissions
(1001, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'VIEW_DASHBOARD', 'READ', 'View dashboard', 0),
(1002, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'MANAGE_DASHBOARD', 'WRITE', 'Configure dashboard', 0),

-- User management
(1003, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'VIEW_USERS', 'READ', 'View user list', 0),
(1004, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'CREATE_USER', 'WRITE', 'Create new users', 0),
(1005, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'EDIT_USER', 'WRITE', 'Edit existing users', 0),
(1006, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'DELETE_USER', 'ADMIN', 'Delete users', 0),

-- Role management
(1007, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'VIEW_ROLES', 'READ', 'View roles', 0),
(1008, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'MANAGE_ROLES', 'ADMIN', 'Manage roles', 0),

-- Event management
(1009, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'VIEW_EVENTS', 'READ', 'View events', 0),
(1010, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'CREATE_EVENT', 'WRITE', 'Create events', 0),
(1011, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'EDIT_EVENT', 'WRITE', 'Edit events', 0),
(1012, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'DELETE_EVENT', 'ADMIN', 'Delete events', 0),

-- Participant management
(1013, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'VIEW_PARTICIPANTS', 'READ', 'View participants', 0),
(1014, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'CREATE_PARTICIPANT', 'WRITE', 'Register participants', 0),
(1015, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'EDIT_PARTICIPANT', 'WRITE', 'Edit participants', 0),

-- Reward management
(1016, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'VIEW_REWARDS', 'READ', 'View rewards', 0),
(1017, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'MANAGE_REWARDS', 'WRITE', 'Manage rewards', 0),

-- Reporting
(1018, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'VIEW_REPORTS', 'READ', 'View reports', 0),
(1019, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'EXPORT_DATA', 'READ', 'Export data', 0),

-- System administration
(1020, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'VIEW_SYSTEM_CONFIG', 'READ', 'View system config', 0),
(1021, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'MANAGE_SYSTEM_CONFIG', 'ADMIN', 'Manage system config', 0),
(1022, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'VIEW_AUDIT_LOGS', 'READ', 'View audit logs', 0),
(1023, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'MANAGE_LOCATIONS', 'WRITE', 'Manage locations', 0),
(1024, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'MANAGE_GOLDEN_HOURS', 'WRITE', 'Manage golden hours', 0),
(1025, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'SPIN_WHEEL', 'EXECUTE', 'Spin the wheel', 0);

-- Associate permissions with roles
-- Admin role gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1001, id FROM permissions WHERE status = 'ACTIVE';

-- Manager role gets most permissions except admin-level ones
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1003, id FROM permissions 
WHERE status = 'ACTIVE' AND type != 'ADMIN';

-- Operator gets operational permissions
INSERT INTO role_permissions (role_id, permission_id)
VALUES
(1006, 1001), (1006, 1003), (1006, 1009), (1006, 1011),
(1006, 1013), (1006, 1014), (1006, 1015), (1006, 1016),
(1006, 1017), (1006, 1018), (1006, 1019), (1006, 1023),
(1006, 1024), (1006, 1025);

-- Regular user gets basic permissions
INSERT INTO role_permissions (role_id, permission_id)
VALUES
(1002, 1001), (1002, 1009), (1002, 1013), (1002, 1016), (1002, 1025);

-- Guest gets minimal permissions
INSERT INTO role_permissions (role_id, permission_id)
VALUES (1005, 1009), (1005, 1016);

-- Participant role gets participant-specific permissions
INSERT INTO role_permissions (role_id, permission_id)
VALUES (1004, 1009), (1004, 1016), (1004, 1025);

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