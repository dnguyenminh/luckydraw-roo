-- Test data for events
INSERT INTO events (id, version, code, name, description, start_time, end_time, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 'EVENT001', 'Test Event 1', 'Test Description 1', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP + INTERVAL '23 hours', 'ACTIVE', 'system', 'system', false),
(2, 0, 'EVENT002', 'Test Event 2', 'Test Description 2', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP + INTERVAL '22 hours', 'ACTIVE', 'system', 'system', false),
(3, 0, 'EVENT003', 'Test Event 3', 'Test Description 3', CURRENT_TIMESTAMP + INTERVAL '1 hour', CURRENT_TIMESTAMP + INTERVAL '25 hours', 'INACTIVE', 'system', 'system', false),
(4, 0, 'EVENT004', 'Test Event 4', 'Test Description 4', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '6 days', 'ACTIVE', 'system', 'system', false);

-- Test data for regions
INSERT INTO regions (id, version, code, name, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 'REGION001', 'North Region', 'ACTIVE', 'system', 'system', false),
(2, 0, 'REGION002', 'South Region', 'ACTIVE', 'system', 'system', false),
(3, 0, 'REGION003', 'East Region', 'INACTIVE', 'system', 'system', false);

-- Test data for event_locations
INSERT INTO event_locations (id, version, event_id, region_id, status, max_spin, created_by, updated_by, deleted)
VALUES 
(1, 0, 1, 1, 'ACTIVE', 3, 'system', 'system', false),
(2, 0, 2, 2, 'ACTIVE', 3, 'system', 'system', false),
(3, 0, 3, 1, 'INACTIVE', 5, 'system', 'system', false),
(4, 0, 4, 2, 'ACTIVE', 2, 'system', 'system', false);

-- Test data for provinces
INSERT INTO provinces (id, version, code, name, region_id, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 'PROVINCE001', 'Test Province 1', 1, 'ACTIVE', 'system', 'system', false),
(2, 0, 'PROVINCE002', 'Test Province 2', 2, 'ACTIVE', 'system', 'system', false);

-- Test data for participants
INSERT INTO participants (id, version, code, name, province_id, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 'PART001', 'John Doe', 1, 'ACTIVE', 'system', 'system', false),
(2, 0, 'PART002', 'Jane Smith', 2, 'ACTIVE', 'system', 'system', false),
(3, 0, 'PART003', 'Bob Johnson', 1, 'INACTIVE', 'system', 'system', false),
(4, 0, 'PART004', 'Alice Brown', 2, 'ACTIVE', 'system', 'system', false);

-- Test data for rewards
INSERT INTO rewards (id, version, code, name, description, event_location_id, value, quantity, win_probability, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 'REWARD001', '$100 Cash', 'Cash prize of $100', 1, 100.00, 10, 0.3, 'ACTIVE', 'system', 'system', false),
(2, 0, 'REWARD002', '$50 Voucher', 'Shopping voucher', 2, 50.00, 20, 0.5, 'ACTIVE', 'system', 'system', false),
(3, 0, 'REWARD003', 'Smartphone', 'Latest smartphone', 1, 999.99, 5, 0.2, 'ACTIVE', 'system', 'system', false);

-- Test data for participant_events
INSERT INTO participant_events (id, version, event_id, event_location_id, participant_id, spins_remaining, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 1, 1, 1, 3, 'ACTIVE', 'system', 'system', false),
(2, 0, 2, 2, 2, 3, 'ACTIVE', 'system', 'system', false),
(3, 0, 1, 1, 3, 0, 'INACTIVE', 'system', 'system', false),
(4, 0, 4, 4, 4, 2, 'ACTIVE', 'system', 'system', false);

-- Test data for audit_logs
INSERT INTO audit_logs (username, status) VALUES ('testUser', 'ACTIVE');

-- Test data for configurations
INSERT INTO configurations (config_key, config_value, status) VALUES ('testKey', 'testValue', 'ACTIVE');

-- Reset sequence values
SELECT setval('events_id_seq', (SELECT MAX(id) FROM events));
SELECT setval('regions_id_seq', (SELECT MAX(id) FROM regions));
SELECT setval('provinces_id_seq', (SELECT MAX(id) FROM provinces));
SELECT setval('event_locations_id_seq', (SELECT MAX(id) FROM event_locations));
SELECT setval('participants_id_seq', (SELECT MAX(id) FROM participants));
SELECT setval('participant_events_id_seq', (SELECT MAX(id) FROM participant_events));
SELECT setval('rewards_id_seq', (SELECT MAX(id) FROM rewards));
