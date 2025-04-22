-- Test data for tables with quoted names

-- Test data for regions
INSERT INTO "regions" (id, version, code, name, status, created_by, created_at, updated_by, updated_at)
VALUES 
(1, 0, 'REGION001', 'North Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2, 0, 'REGION002', 'South Region', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(3, 0, 'REGION003', 'East Region', 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Test data for events
INSERT INTO "events" (id, version, code, name, description, start_time, end_time, status, created_by, created_at, updated_by, updated_at)
VALUES 
(1, 0, 'EVENT001', 'Test Event 1', 'Test Description 1', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP + INTERVAL '23 hours', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2, 0, 'EVENT002', 'Test Event 2', 'Test Description 2', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP + INTERVAL '22 hours', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(3, 0, 'EVENT003', 'Test Event 3', 'Test Description 3', CURRENT_TIMESTAMP + INTERVAL '1 hour', CURRENT_TIMESTAMP + INTERVAL '25 hours', 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(4, 0, 'EVENT004', 'Test Event 4', 'Test Description 4', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '6 days', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Test data for provinces
INSERT INTO "provinces" (id, version, code, name, region_id, status, created_by, created_at, updated_by, updated_at)
VALUES 
(1, 0, 'PROVINCE001', 'Test Province 1', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2, 0, 'PROVINCE002', 'Test Province 2', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Test data for event_locations with composite key
INSERT INTO event_locations (event_id, region_id, status, description, max_spin, today_spin, daily_spin_distributing_rate, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 1, 'ACTIVE', 'Event location 1 in region 1', 100, 50, 0.1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 2, 'ACTIVE', 'Event location 2 in region 2', 200, 100, 0.2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 1, 'INACTIVE', 'Event location 3 in region 1', 100, 50, 0.1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 2, 'ACTIVE', 'Event location 4 in region 2', 100, 50, 0.1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for participants
INSERT INTO "participants" (id, version, code, name, phone, email, status, created_by, created_at, updated_by, updated_at)
VALUES 
(1, 0, 'PART001', 'John Doe', '1234567890', 'john@example.com', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2, 0, 'PART002', 'Jane Smith', '2345678901', 'jane@example.com', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(3, 0, 'PART003', 'Bob Johnson', '3456789012', 'bob@example.com', 'INACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(4, 0, 'PART004', 'Alice Brown', '4567890123', 'alice@example.com', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Test data for participant_events with composite key
INSERT INTO participant_events (participant_id, event_id, region_id, status, spins_remaining, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 1, 1, 'ACTIVE', 3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 2, 2, 'ACTIVE', 3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(1, 3, 1, 'INACTIVE', 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(4, 4, 2, 'ACTIVE', 2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for rewards
INSERT INTO rewards (id, created_by, created_at, updated_by, updated_at, status, name, code, description, prize_value, event_location_id, version)
VALUES 
(1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', '$100 Cash', 'REWARD001', 'Cash prize of $100', 100.00, 1, 0),
(2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', '$50 Voucher', 'REWARD002', 'Shopping voucher', 50.00, 2, 0),
(3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE', 'Smartphone', 'REWARD003', 'Latest smartphone', 999.99, 1, 0);

-- Test data for reward_events with composite key
INSERT INTO "reward_events" (event_id, region_id, reward_id, quantity, today_quantity, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 1, 1, 10, 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(1, 1, 2, 20, 5, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 2, 3, 5, 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for golden hours
INSERT INTO golden_hours (id, event_id, region_id, start_time, end_time, multiplier, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 1, 1, CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_TIMESTAMP + INTERVAL '90 minutes', 2.0, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 2, 2, CURRENT_TIMESTAMP + INTERVAL '2 hours', CURRENT_TIMESTAMP + INTERVAL '4 hours', 1.5, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for spin histories with correct foreign keys
INSERT INTO "spin_histories" (id, participant_id, event_id, region_id, spin_time, reward_id, reward_event_id, reward_region_id, win, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 1, 1, 1, CURRENT_TIMESTAMP - INTERVAL '2 hours', 1, 1, 1, true, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 1, 1, 1, CURRENT_TIMESTAMP - INTERVAL '1 hours', NULL, NULL, NULL, false, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(3, 2, 2, 2, CURRENT_TIMESTAMP - INTERVAL '3 hours', 2, 1, 1, true, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for roles
INSERT INTO "roles" (id, role_type, name, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'ROLE_ADMIN', 'Administrator', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'ROLE_USER', 'Standard User', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for users
INSERT INTO "users" (id, username, password, email, full_name, role_id, status, created_by, created_at, updated_by, updated_at, version)
VALUES
(1, 'admin', '$2a$10$rQnT.Gx6lI5Sn45XRu0SsO0jz5vBNl72FXkbDI3TTVQfHK4mFjESq', 'admin@test.com', 'Admin User', 1, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'user', '$2a$10$rQnT.Gx6lI5Sn45XRu0SsO0jz5vBNl72FXkbDI3TTVQfHK4mFjESq', 'user@test.com', 'Regular User', 2, 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Test data for audit_logs
INSERT INTO "audit_logs" (id, object_type, object_id, action_type, status, created_by, created_at, updated_by, updated_at, update_time, version)
VALUES 
(1, 'Event', '1', 'CREATE', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 'Participant', '1', 'UPDATE', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Test data for configurations
INSERT INTO "configurations" (id, config_key, config_value, description, status, created_by, created_at, updated_by, updated_at, version)
VALUES 
(1, 'DAILY_SPIN_LIMIT', '5', 'Maximum spins per day', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0),
(2, 'GOLDEN_HOUR_MULTIPLIER', '2', 'Default golden hour multiplier', 'ACTIVE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Reset sequence values
SELECT setval('events_id_seq', (SELECT MAX(id) FROM events));
SELECT setval('regions_id_seq', (SELECT MAX(id) FROM regions));
SELECT setval('provinces_id_seq', (SELECT MAX(id) FROM provinces));
SELECT setval('participants_id_seq', (SELECT MAX(id) FROM participants));
SELECT setval('rewards_id_seq', (SELECT MAX(id) FROM rewards));
SELECT setval('golden_hour_id_seq', (SELECT MAX(id) FROM golden_hours));
SELECT setval('spin_history_id_seq', (SELECT MAX(id) FROM spin_histories));
