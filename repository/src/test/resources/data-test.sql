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

-- Test data for prize_types
INSERT INTO prize_types (id, version, code, name, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 'PRIZETYPE001', 'Cash Prize', 'ACTIVE', 'system', 'system', false),
(2, 0, 'PRIZETYPE002', 'Voucher', 'ACTIVE', 'system', 'system', false),
(3, 0, 'PRIZETYPE003', 'Product', 'ACTIVE', 'system', 'system', false);

-- Test data for prizes
INSERT INTO prizes (id, version, code, name, description, prize_type_id, value, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 'PRIZE001', '$100 Cash', 'Cash prize of $100', 1, 100.00, 'ACTIVE', 'system', 'system', false),
(2, 0, 'PRIZE002', '$50 Voucher', 'Shopping voucher', 2, 50.00, 'ACTIVE', 'system', 'system', false),
(3, 0, 'PRIZE003', 'Smartphone', 'Latest smartphone', 3, 999.99, 'ACTIVE', 'system', 'system', false);

-- Test data for event_locations
INSERT INTO event_locations (id, version, event_id, region_id, status, max_spin, created_by, updated_by, deleted)
VALUES 
(1, 0, 1, 1, 'ACTIVE', 3, 'system', 'system', false),
(2, 0, 2, 2, 'ACTIVE', 3, 'system', 'system', false),
(3, 0, 3, 1, 'INACTIVE', 5, 'system', 'system', false),
(4, 0, 4, 2, 'ACTIVE', 2, 'system', 'system', false);

-- Test data for participants
INSERT INTO participants (id, version, code, name, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 'PART001', 'John Doe', 'ACTIVE', 'system', 'system', false),
(2, 0, 'PART002', 'Jane Smith', 'ACTIVE', 'system', 'system', false),
(3, 0, 'PART003', 'Bob Johnson', 'INACTIVE', 'system', 'system', false),
(4, 0, 'PART004', 'Alice Brown', 'ACTIVE', 'system', 'system', false);

-- Test data for participant_events
INSERT INTO participant_events (id, version, event_id, event_location_id, participant_id, spins_remaining, status, created_by, updated_by, deleted)
VALUES 
(1, 0, 1, 1, 1, 3, 'ACTIVE', 'system', 'system', false),
(2, 0, 2, 2, 2, 3, 'ACTIVE', 'system', 'system', false),
(3, 0, 1, 1, 3, 0, 'INACTIVE', 'system', 'system', false),
(4, 0, 4, 4, 4, 2, 'ACTIVE', 'system', 'system', false);

-- Reset sequence values
SELECT setval('events_id_seq', (SELECT MAX(id) FROM events));
SELECT setval('regions_id_seq', (SELECT MAX(id) FROM regions));
SELECT setval('prize_types_id_seq', (SELECT MAX(id) FROM prize_types));
SELECT setval('prizes_id_seq', (SELECT MAX(id) FROM prizes));
SELECT setval('event_locations_id_seq', (SELECT MAX(id) FROM event_locations));
SELECT setval('participants_id_seq', (SELECT MAX(id) FROM participants));
SELECT setval('participant_events_id_seq', (SELECT MAX(id) FROM participant_events));
