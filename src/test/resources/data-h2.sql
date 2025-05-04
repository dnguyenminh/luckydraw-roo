-- Clear existing data
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
DELETE FROM role_permissions;
DELETE FROM user_roles;
DELETE FROM blacklisted_tokens;
DELETE FROM users;
DELETE FROM permissions;
DELETE FROM roles;
DELETE FROM configurations;
DELETE FROM audit_logs;
-- Insert Regions
INSERT INTO regions (id, code, name, status, created_by, created_at)
VALUES (
      1,
      'NORTH',
      'Northern Region',
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      2,
      'CENTRAL',
      'Central Region',
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      3,
      'SOUTH',
      'Southern Region',
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP
   );
-- Insert Provinces
INSERT INTO provinces (id, code, name, status, created_by, created_at)
VALUES (
      1,
      'HN',
      'Hanoi',
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      2,
      'HCM',
      'Ho Chi Minh',
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      3,
      'DN',
      'Da Nang',
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP
   );
-- Link Regions and Provinces
INSERT INTO region_province (province_id, region_id)
VALUES (1, 1),
   -- Hanoi -> North
   (2, 3),
   -- HCMC -> South
   (3, 2);
-- Da Nang -> Central
-- Insert Events
INSERT INTO events (
      id,
      code,
      name,
      status,
      start_time,
      end_time,
      created_by,
      created_at
   )
VALUES (
      1,
      'SUMMER_2023',
      'Summer Festival 2023',
      'ACTIVE',
      DATEADD('DAY', -10, CURRENT_TIMESTAMP),
      DATEADD('DAY', 20, CURRENT_TIMESTAMP),
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      2,
      'WINTER_2023',
      'Winter Festival 2023',
      'ACTIVE',
      DATEADD('DAY', 30, CURRENT_TIMESTAMP),
      DATEADD('DAY', 60, CURRENT_TIMESTAMP),
      'system',
      CURRENT_TIMESTAMP
   );
-- Insert Event Locations
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
      created_at
   )
VALUES (
      1,
      1,
      1,
      'Hanoi Summer Fest',
      'HN_SUMMER',
      'ACTIVE',
      100,
      1000,
      0.1,
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      1,
      3,
      2,
      'HCMC Summer Fest',
      'HCM_SUMMER',
      'ACTIVE',
      150,
      1500,
      0.15,
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      2,
      1,
      1,
      'Hanoi Winter Fest',
      'HN_WINTER',
      'ACTIVE',
      200,
      2000,
      0.2,
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      2,
      2,
      3,
      'Da Nang Winter Fest',
      'DN_WINTER',
      'ACTIVE',
      120,
      1200,
      0.12,
      'system',
      CURRENT_TIMESTAMP
   );
-- Insert Golden Hours
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
      created_at
   )
VALUES (
      1,
      1,
      1,
      DATEADD('HOUR', 1, CURRENT_TIMESTAMP),
      DATEADD('HOUR', 3, CURRENT_TIMESTAMP),
      2.0,
      50,
      0,
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      2,
      1,
      3,
      DATEADD('HOUR', 2, CURRENT_TIMESTAMP),
      DATEADD('HOUR', 4, CURRENT_TIMESTAMP),
      1.5,
      30,
      0,
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      3,
      2,
      1,
      DATEADD('DAY', 30, CURRENT_TIMESTAMP),
      DATEADD('DAY', 31, CURRENT_TIMESTAMP),
      2.5,
      100,
      0,
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP
   );
-- Insert Rewards
INSERT INTO rewards (
      id,
      code,
      name,
      status,
      prize_value,
      event_id,
      region_id,
      created_by,
      created_at
   )
VALUES (
      1,
      'GOLD',
      'Gold Prize',
      'ACTIVE',
      1000000.00,
      1,
      1,
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      2,
      'SILVER',
      'Silver Prize',
      'ACTIVE',
      500000.00,
      1,
      1,
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      3,
      'BRONZE',
      'Bronze Prize',
      'ACTIVE',
      200000.00,
      1,
      1,
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      4,
      'HCM_SPECIAL',
      'HCMC Special',
      'ACTIVE',
      2000000.00,
      1,
      3,
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      5,
      'WINTER_GOLD',
      'Winter Gold',
      'ACTIVE',
      2000000.00,
      2,
      1,
      'system',
      CURRENT_TIMESTAMP
   );
-- Insert Reward Events
INSERT INTO reward_events (
      event_id,
      region_id,
      reward_id,
      status,
      quantity,
      today_quantity,
      created_by,
      created_at
   )
VALUES (
      1,
      1,
      1,
      'ACTIVE',
      10,
      2,
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      1,
      1,
      2,
      'ACTIVE',
      20,
      4,
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      1,
      1,
      3,
      'ACTIVE',
      30,
      6,
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      1,
      3,
      4,
      'ACTIVE',
      5,
      1,
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      2,
      1,
      5,
      'ACTIVE',
      15,
      3,
      'system',
      CURRENT_TIMESTAMP
   );
-- Insert Participants
INSERT INTO participants (
      id,
      code,
      name,
      phone,
      province_id,
      status,
      created_by,
      created_at
   )
VALUES (
      1,
      'JOHN001',
      'John Doe',
      '0123456789',
      1,
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      2,
      'JANE001',
      'Jane Smith',
      '0987654321',
      2,
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP
   ),
   (
      3,
      'BOB001',
      'Bob Wilson',
      '0369852147',
      1,
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP
   );
-- Insert Participant Events
INSERT INTO participant_events (
      participant_id,
      event_id,
      region_id,
      spins_remaining,
      status,
      created_by,
      created_at
   )
VALUES (
      1,
      1,
      1,
      5,
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP
   ),
   -- John in Hanoi Summer
   (
      2,
      1,
      3,
      5,
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP
   ),
   -- Jane in HCMC Summer
   (
      1,
      2,
      1,
      3,
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP
   ),
   -- John in Hanoi Winter
   (
      3,
      2,
      1,
      3,
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP
   );
-- Bob in Hanoi Winter
-- Insert Spin Histories with Golden Hour integration
INSERT INTO spin_histories (
      id,
      spin_time,
      participant_id,
      participant_event_id,
      participant_region_id,
      reward_id,
      reward_event_id,
      reward_region_id,
      golden_hour_id,
      win,
      multiplier,
      status,
      created_by,
      created_at,
      event_id -- Add the missing event_id column
   )
VALUES (
      1,
      DATEADD('HOUR', -1, CURRENT_TIMESTAMP),
      1,
      1,
      1,
      -- John's participation in Hanoi Summer
      1,
      1,
      1,
      -- Won Gold Prize
      1,
      true,
      2.0,
      -- During golden hour x2
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP,
      1
   ),
   -- Event ID for Summer Festival
   (
      2,
      DATEADD('HOUR', -2, CURRENT_TIMESTAMP),
      1,
      1,
      1,
      -- John's participation in Hanoi Summer
      NULL,
      NULL,
      NULL,
      -- No win
      NULL,
      false,
      1.0,
      -- Not during golden hour
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP,
      1
   ),
   -- Event ID for Summer Festival
   (
      3,
      DATEADD('HOUR', -3, CURRENT_TIMESTAMP),
      2,
      1,
      3,
      -- Jane's participation in HCMC Summer
      4,
      1,
      3,
      -- Won HCMC Special
      2,
      true,
      1.5,
      -- During golden hour x1.5
      'ACTIVE',
      'system',
      CURRENT_TIMESTAMP,
      1
   );
-- Event ID for Summer Festival
-- Insert Roles
INSERT INTO roles (
      id,
      created_by,
      created_at,
      status,
      role_type,
      description
   )
VALUES (
      1,
      'system',
      CURRENT_TIMESTAMP,
      'ACTIVE',
      'ADMIN',
      'Administrator'
   ),
   (
      2,
      'system',
      CURRENT_TIMESTAMP,
      'ACTIVE',
      'USER',
      'Regular User'
   );
-- Insert Permissions
INSERT INTO permissions (
      id,
      created_by,
      created_at,
      status,
      name,
      type,
      description
   )
VALUES (
      1,
      'system',
      CURRENT_TIMESTAMP,
      'ACTIVE',
      'CREATE_EVENT',
      'WRITE',
      'Create new events'
   ),
   (
      2,
      'system',
      CURRENT_TIMESTAMP,
      'ACTIVE',
      'VIEW_EVENT',
      'READ',
      'View event details'
   );
-- Insert Role Permissions
INSERT INTO role_permissions (role_id, permission_id)
VALUES (1, 1),
   -- Admin can create events
   (1, 2),
   -- Admin can view events
   (2, 2);
-- User can view events
-- Insert Users
INSERT INTO users (
      id,
      created_by,
      created_at,
      status,
      username,
      password,
      email,
      full_name,
      role_id
   )
VALUES (
      1,
      'system',
      CURRENT_TIMESTAMP,
      'ACTIVE',
      'admin',
      '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy',
      'admin@example.com',
      'System Admin',
      1
   ),
   (
      2,
      'system',
      CURRENT_TIMESTAMP,
      'ACTIVE',
      'user',
      '$2a$10$qeS0HEh7urweMojsnwNAR.vcXJeXR1UcMRZ2WcGQl9YeuspUL7qhy',
      'user@example.com',
      'Regular User',
      2
   );
-- Insert Configurations
INSERT INTO configurations (
      id,
      created_by,
      created_at,
      status,
      config_key,
      config_value,
      description
   )
VALUES (
      1,
      'system',
      CURRENT_TIMESTAMP,
      'ACTIVE',
      'MAX_DAILY_SPINS',
      '10',
      'Maximum spins per day'
   ),
   (
      2,
      'system',
      CURRENT_TIMESTAMP,
      'ACTIVE',
      'TOKEN_EXPIRY',
      '3600',
      'Token expiry in seconds'
   );