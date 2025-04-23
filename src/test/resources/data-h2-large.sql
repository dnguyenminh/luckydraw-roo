-- Large dataset for performance testing with H2

-- Note: This script adds additional data for stress testing and performance evaluation
-- It's designed to work whether or not data-h2.sql has been executed

-- Begin transaction for better error handling
BEGIN;

-- Ensure base regions exist (1-3) before adding new ones
MERGE INTO regions USING (VALUES
    (1, 'North Region', 'NORTH', 'Northern provinces'),
    (2, 'Central Region', 'CENTRAL', 'Central provinces'),
    (3, 'South Region', 'SOUTH', 'Southern provinces')
) AS source(id, name, code, description)
ON regions.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, created_by, created_at, updated_by, updated_at, status, name, code, description, version)
    VALUES (source.id, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE',
            source.name, source.code, source.description, 0);

-- Additional Regions (adding to existing data)
MERGE INTO regions USING (VALUES
    (4, 'Northeast Region', 'NORTHEAST', 'Northeastern provinces'),
    (5, 'Northwest Region', 'NORTHWEST', 'Northwestern provinces'),
    (6, 'Southeast Region', 'SOUTHEAST', 'Southeastern provinces'),
    (7, 'Southwest Region', 'SOUTHWEST', 'Southwestern provinces'),
    (8, 'Highland Region', 'HIGHLAND', 'Central Highland provinces')
) AS source(id, name, code, description)
ON regions.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, created_by, created_at, updated_by, updated_at, status, name, code, description, version)
    VALUES (source.id, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE',
            source.name, source.code, source.description, 0);

-- Ensure base provinces exist before adding participants
MERGE INTO provinces USING (VALUES
    (1, 'Hanoi', 'HN', 'Capital city', 1),
    (2, 'Ho Chi Minh', 'HCM', 'Southern metropolitan city', 3),
    (3, 'Da Nang', 'DN', 'Central coast city', 2)
) AS source(id, name, code, description, region_id)
ON provinces.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, created_by, created_at, updated_by, updated_at, status, name, code, description, region_id, version)
    VALUES (source.id, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE',
            source.name, source.code, source.description, source.region_id, 0);

-- Additional Provinces - Add safely with MERGE
MERGE INTO provinces USING (VALUES
    (4, 'Hai Phong', 'HP', 'Seaport city', 1),
    (5, 'Can Tho', 'CT', 'Mekong Delta city', 3),
    (6, 'Hue', 'HUE', 'Historical city', 2),
    (7, 'Nha Trang', 'NT', 'Coastal city', 6),
    (8, 'Halong', 'HL', 'Bay city', 4),
    (9, 'Dalat', 'DL', 'Mountain city', 8),
    (10, 'Sapa', 'SP', 'Highland city', 5),
    (11, 'Vung Tau', 'VT', 'Beach city', 6),
    (12, 'Bac Ninh', 'BN', 'Industrial city', 4),
    (13, 'Vinh Long', 'VL', 'Mekong province', 7),
    (14, 'Buon Ma Thuot', 'BMT', 'Coffee city', 8),
    (15, 'Nam Dinh', 'ND', 'Textile city', 1)
) AS source(id, name, code, description, region_id)
ON provinces.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, created_by, created_at, updated_by, updated_at, status, name, code, description, region_id, version)
    VALUES (source.id, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE',
            source.name, source.code, source.description, source.region_id, 0);

-- Ensure basic events exist before adding new ones
MERGE INTO events USING (VALUES
    (1, 'Summer Festival', 'SUMMER_FEST', 'Summer lucky draw event', DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('DAY', 20, CURRENT_TIMESTAMP)),
    (2, 'Winter Festival', 'WINTER_FEST', 'Winter lucky draw event', DATEADD('DAY', 30, CURRENT_TIMESTAMP), DATEADD('DAY', 60, CURRENT_TIMESTAMP))
) AS source(id, name, code, description, start_time, end_time)
ON events.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, created_by, created_at, updated_by, updated_at, status, name, code, description, start_time, end_time, version)
    VALUES (source.id, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE',
            source.name, source.code, source.description, source.start_time, source.end_time, 0);

-- Additional Events (adding to existing data)
MERGE INTO events USING (VALUES
    (3, 'Spring Festival', 'SPRING_FEST', 'Spring lucky draw event', DATEADD('DAY', 70, CURRENT_TIMESTAMP), DATEADD('DAY', 100, CURRENT_TIMESTAMP)),
    (4, 'Autumn Festival', 'AUTUMN_FEST', 'Autumn lucky draw event', DATEADD('DAY', 120, CURRENT_TIMESTAMP), DATEADD('DAY', 150, CURRENT_TIMESTAMP)),
    (5, 'Lunar New Year', 'TET_FEST', 'Tet holiday event', DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('DAY', 15, CURRENT_TIMESTAMP)),
    (6, 'Independence Day', 'INDEPENDENCE', 'National day event', DATEADD('DAY', 25, CURRENT_TIMESTAMP), DATEADD('DAY', 40, CURRENT_TIMESTAMP)),
    (7, 'Christmas Festival', 'CHRISTMAS', 'Christmas holiday event', DATEADD('DAY', 180, CURRENT_TIMESTAMP), DATEADD('DAY', 200, CURRENT_TIMESTAMP)),
    (8, 'Past Event', 'PAST_EVENT', 'Completed event', DATEADD('DAY', -60, CURRENT_TIMESTAMP), DATEADD('DAY', -30, CURRENT_TIMESTAMP))
) AS source(id, name, code, description, start_time, end_time)
ON events.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, created_by, created_at, updated_by, updated_at, status, name, code, description, start_time, end_time, version)
    VALUES (source.id, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP,
            CASE WHEN source.id = 8 THEN 'INACTIVE' ELSE 'ACTIVE' END,
            source.name, source.code, source.description, source.start_time, source.end_time, 0);

-- Ensure base event_locations exist
-- Make sure event locations exist for event-region pairs that will be referenced later
MERGE INTO event_locations USING (VALUES
    (1, 1, 'North summer event', 100, 50, 0.1),
    (1, 3, 'South summer event', 150, 75, 0.1),
    (2, 1, 'North winter event', 200, 100, 0.2),
    (2, 2, 'Central winter event', 80, 40, 0.1)
) AS source(event_id, region_id, description, max_spin, today_spin, daily_spin_dist_rate)
ON event_locations.event_id = source.event_id AND event_locations.region_id = source.region_id
WHEN NOT MATCHED THEN
    INSERT (event_id, region_id, created_by, created_at, updated_by, updated_at, status, description, max_spin, today_spin, daily_spin_dist_rate, version)
    VALUES (source.event_id, source.region_id, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE',
            source.description, source.max_spin, source.today_spin, source.daily_spin_dist_rate, 0);

-- Additional Event Locations - add safely with MERGE
MERGE INTO event_locations USING (VALUES
    -- Event 3 (Spring Festival) in multiple regions
    (3, 1, 'North spring event', 200, 100, 0.2),
    (3, 2, 'Central spring event', 180, 90, 0.2),
    (3, 3, 'South spring event', 220, 110, 0.2),
    -- Event 4 (Autumn Festival) in multiple regions
    (4, 1, 'North autumn event', 150, 75, 0.15),
    (4, 3, 'South autumn event', 160, 80, 0.15),
    (4, 4, 'Northeast autumn event', 140, 70, 0.15),
    -- Event 5 (Lunar New Year) in all regions
    (5, 1, 'North lunar new year', 300, 150, 0.3),
    (5, 2, 'Central lunar new year', 280, 140, 0.3),
    (5, 3, 'South lunar new year', 320, 160, 0.3),
    (5, 4, 'Northeast lunar new year', 260, 130, 0.3),
    (5, 5, 'Northwest lunar new year', 220, 110, 0.3),
    (5, 6, 'Southeast lunar new year', 240, 120, 0.3),
    (5, 7, 'Southwest lunar new year', 200, 100, 0.3),
    (5, 8, 'Highland lunar new year', 180, 90, 0.3),
    -- Event 6 (Independence Day) in selected regions
    (6, 1, 'North independence day', 250, 125, 0.25),
    (6, 3, 'South independence day', 270, 135, 0.25),
    -- Event 7 (Christmas) in selected regions
    (7, 1, 'North christmas', 230, 115, 0.23),
    (7, 3, 'South christmas', 250, 125, 0.23),
    -- Event 8 (Past event)
    (8, 1, 'Past event north', 100, 50, 0.1),
    (8, 3, 'Past event south', 100, 50, 0.1)
) AS source(event_id, region_id, description, max_spin, today_spin, daily_spin_dist_rate)
ON event_locations.event_id = source.event_id AND event_locations.region_id = source.region_id
WHEN NOT MATCHED THEN
    INSERT (event_id, region_id, created_by, created_at, updated_by, updated_at, status, description, max_spin, today_spin, daily_spin_dist_rate, version)
    VALUES (source.event_id, source.region_id, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP,
            CASE WHEN source.event_id = 8 THEN 'INACTIVE' ELSE 'ACTIVE' END,
            source.description, source.max_spin, source.today_spin, source.daily_spin_dist_rate, 0);

-- Ensure base rewards exist
MERGE INTO rewards USING (VALUES
    (1, 'Gold Prize', 'GOLD', 'Gold prize description', 1000.00),
    (2, 'Silver Prize', 'SILVER', 'Silver prize description', 500.00),
    (3, 'Bronze Prize', 'BRONZE', 'Bronze prize description', 250.00)
) AS source(id, name, code, description, prize_value)
ON rewards.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, created_by, created_at, updated_by, updated_at, status, name, code, description, prize_value, version)
    VALUES (source.id, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE',
            source.name, source.code, source.description, source.prize_value, 0);

-- Additional Rewards with MERGE
MERGE INTO rewards USING (VALUES
    (4, 'Platinum Prize', 'PLATINUM', 'Exclusive platinum prize', 5000.00),
    (5, 'Diamond Prize', 'DIAMOND', 'Rare diamond prize', 10000.00),
    (6, 'Gift Card', 'GIFTCARD', 'Shopping gift card', 100.00),
    (7, 'Cinema Tickets', 'CINEMA', 'Free movie tickets', 50.00),
    (8, 'Restaurant Voucher', 'RESTAURANT', 'Dining voucher', 150.00),
    (9, 'Electronics', 'ELECTRONICS', 'Electronic device', 800.00),
    (10, 'Travel Package', 'TRAVEL', 'Vacation package', 3000.00)
) AS source(id, name, code, description, prize_value)
ON rewards.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, created_by, created_at, updated_by, updated_at, status, name, code, description, prize_value, version)
    VALUES (source.id, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'ACTIVE',
            source.name, source.code, source.description, source.prize_value, 0);

-- Create sequences for ID generation (add configuration_seq if missing)
CREATE SEQUENCE IF NOT EXISTS participant_id_seq START WITH 3000; -- Changed to 3000 to avoid conflicts
CREATE SEQUENCE IF NOT EXISTS spin_history_seq START WITH 3000; -- Changed for consistency
CREATE SEQUENCE IF NOT EXISTS configuration_seq START WITH 20; -- Add sequence for configurations

-- Generate test participants - Using an even higher ID range to avoid conflicts
-- First, create a temporary table with all valid province IDs
CREATE TEMPORARY TABLE IF NOT EXISTS valid_provinces AS
SELECT id FROM provinces;

-- Now insert participants using only the valid provinces starting from 3000
INSERT INTO participants (id, created_by, created_at, updated_by, updated_at, status, name, code, phone, address, last_adding_spin, province_id, version)
SELECT 
    3000 + t.x AS id, -- Changed from 2000 to 3000
    'system' AS created_by,
    CURRENT_TIMESTAMP AS created_at,
    'system' AS updated_by,
    CURRENT_TIMESTAMP AS updated_at,
    'ACTIVE' AS status,
    'Test User ' || (3000 + t.x) AS name, -- Updated name to match ID
    'TEST' || LPAD(CAST(3000 + t.x AS VARCHAR), 5, '0') AS code, -- Updated code to match ID
    '9' || LPAD(CAST(3000 + t.x AS VARCHAR), 9, '0') AS phone, -- Updated phone to match ID
    (3000 + t.x) || ' Test Street' AS address, -- Updated address to match ID
    0 AS last_adding_spin,
    vp.id AS province_id,
    0 AS version
FROM 
    (SELECT x FROM SYSTEM_RANGE(0, 99)) t
JOIN 
    (SELECT id, ROW_NUMBER() OVER() AS rn FROM valid_provinces) vp
ON 
    MOD(t.x, (SELECT COUNT(*) FROM valid_provinces)) + 1 = vp.rn;

-- Drop the temporary table
DROP TABLE IF EXISTS valid_provinces;

-- Add participant events for the new participants
INSERT INTO participant_events (participant_id, event_id, region_id, created_by, created_at, updated_by, updated_at, status, spins_remaining, version)
SELECT 
    p.id AS participant_id,
    el.event_id,
    el.region_id,
    'system' AS created_by,
    CURRENT_TIMESTAMP AS created_at,
    'system' AS updated_by,
    CURRENT_TIMESTAMP AS updated_at,
    'ACTIVE' AS status,
    1 + CAST(RAND() * 10 AS INT) AS spins_remaining,
    0 AS version
FROM participants p
JOIN (
    -- For each participant, select 1-3 random event locations
    SELECT 
        p.id AS participant_id,
        el.event_id,
        el.region_id,
        ROW_NUMBER() OVER(PARTITION BY p.id ORDER BY RAND()) AS event_rank
    FROM participants p
    CROSS JOIN event_locations el
    WHERE p.id >= 3000  -- Updated from 2000 to 3000
    AND p.id < 3100     -- Updated from 2100 to 3100
    AND el.event_id < 9 -- Limit to known event IDs
    AND el.region_id < 4 -- Limit to main regions for simplicity
) AS random_assignments ON random_assignments.participant_id = p.id
JOIN event_locations el ON el.event_id = random_assignments.event_id AND el.region_id = random_assignments.region_id
WHERE random_assignments.event_rank <= 1 + CAST(RAND() * 2 AS INT) -- 1 to 3 events per participant
AND NOT EXISTS (
    -- Avoid duplicate entries 
    SELECT 1 FROM participant_events pe 
    WHERE pe.participant_id = p.id 
    AND pe.event_id = el.event_id 
    AND pe.region_id = el.region_id
);

-- Add spin histories with columns that match the actual schema
INSERT INTO spin_histories (
    id, created_by, created_at, updated_by, updated_at, status,
    participant_id, participant_event_id, participant_region_id,
    spin_time, reward_id, reward_event_id, reward_region_id, win,
    version
)
SELECT 
    NEXT VALUE FOR spin_history_seq AS id,
    'system' AS created_by,
    CURRENT_TIMESTAMP AS created_at,
    'system' AS updated_by,
    CURRENT_TIMESTAMP AS updated_at,
    'ACTIVE' AS status,
    pe.participant_id,
    pe.event_id AS participant_event_id,
    pe.region_id AS participant_region_id,
    DATEADD('HOUR', -CAST(RAND() * 100 AS INT), CURRENT_TIMESTAMP) AS spin_time,
    CASE WHEN h.win_flag = 1 THEN re.reward_id ELSE NULL END AS reward_id,
    CASE WHEN h.win_flag = 1 THEN pe.event_id ELSE NULL END AS reward_event_id,
    CASE WHEN h.win_flag = 1 THEN pe.region_id ELSE NULL END AS reward_region_id,
    CASE WHEN h.win_flag = 1 THEN TRUE ELSE FALSE END AS win,
    0 AS version
FROM participant_events pe
CROSS JOIN (
    SELECT 0 AS win_flag UNION ALL SELECT 1 AS win_flag
) h
LEFT JOIN (
    SELECT 
        re.event_id,
        re.region_id,
        re.reward_id,
        ROW_NUMBER() OVER(PARTITION BY re.event_id, re.region_id ORDER BY RAND()) AS rn
    FROM reward_events re
) re ON re.event_id = pe.event_id AND re.region_id = pe.region_id AND re.rn = 1
WHERE pe.participant_id >= 3000  -- Updated from 2000 to 3000
AND pe.participant_id <= 3099    -- Updated from 2099 to 3099
AND RAND() > 0.5;

-- Add reward events for new rewards - fix column name from today_quantity to today_quantity
INSERT INTO reward_events (event_id, region_id, reward_id, created_by, created_at, updated_by, updated_at, status, quantity, today_quantity, version)
SELECT
    e.event_id,
    e.region_id,
    r.id AS reward_id,
    'system' AS created_by,
    CURRENT_TIMESTAMP AS created_at,
    'system' AS updated_by,
    CURRENT_TIMESTAMP AS updated_at,
    'ACTIVE' AS status,
    5 + CAST(RAND() * 20 AS INT) AS quantity,
    2 + CAST(RAND() * 5 AS INT) AS today_quantity,
    0 AS version
FROM (
    -- Select up to 50 combinations of event_locations and rewards
    SELECT
        el.event_id,
        el.region_id,
        r.id AS reward_id,
        ROW_NUMBER() OVER() AS row_num
    FROM event_locations el
    CROSS JOIN (
        -- Only use new rewards
        SELECT id FROM rewards WHERE id > 3
    ) r
    -- Avoid existing combinations
    WHERE NOT EXISTS (
        SELECT 1 FROM reward_events re
        WHERE re.event_id = el.event_id
        AND re.region_id = el.region_id
        AND re.reward_id = r.id
    )
) AS candidate_rows
JOIN event_locations e ON e.event_id = candidate_rows.event_id AND e.region_id = candidate_rows.region_id
JOIN rewards r ON r.id = candidate_rows.reward_id
WHERE candidate_rows.row_num <= 50; -- Limit to 50 records

-- Add additional Golden Hours - use MERGE to avoid PK violations
MERGE INTO golden_hours USING (VALUES
    (100, 'system', 3, 1, DATEADD('HOUR', 72, CURRENT_TIMESTAMP), DATEADD('HOUR', 74, CURRENT_TIMESTAMP), 1.8, 30, 0),
    (101, 'system', 3, 2, DATEADD('HOUR', 75, CURRENT_TIMESTAMP), DATEADD('HOUR', 77, CURRENT_TIMESTAMP), 1.6, 25, 0),
    (102, 'system', 4, 1, DATEADD('HOUR', 120, CURRENT_TIMESTAMP), DATEADD('HOUR', 124, CURRENT_TIMESTAMP), 2.2, 40, 0),
    (103, 'system', 5, 1, DATEADD('HOUR', 5, CURRENT_TIMESTAMP), DATEADD('HOUR', 10, CURRENT_TIMESTAMP), 3.0, 50, 0),
    (104, 'system', 5, 3, DATEADD('HOUR', 8, CURRENT_TIMESTAMP), DATEADD('HOUR', 13, CURRENT_TIMESTAMP), 2.5, 45, 0),
    (105, 'system', 6, 1, DATEADD('HOUR', 26, CURRENT_TIMESTAMP), DATEADD('HOUR', 30, CURRENT_TIMESTAMP), 1.9, 35, 0),
    (106, 'system', 7, 1, DATEADD('HOUR', 180, CURRENT_TIMESTAMP), DATEADD('HOUR', 186, CURRENT_TIMESTAMP), 2.3, 40, 0),
    (107, 'system', 8, 1, DATEADD('HOUR', -50, CURRENT_TIMESTAMP), DATEADD('HOUR', -45, CURRENT_TIMESTAMP), 1.5, 30, 5)
) AS source(id, created_by, event_id, region_id, start_time, end_time, multiplier, max_rewards, claimed_rewards)
ON golden_hours.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, created_by, created_at, updated_by, updated_at, status, event_id, region_id, start_time, end_time, multiplier, max_rewards, claimed_rewards, version)
    VALUES (source.id, source.created_by, CURRENT_TIMESTAMP, source.created_by, CURRENT_TIMESTAMP,
            CASE WHEN source.event_id = 8 THEN 'INACTIVE' ELSE 'ACTIVE' END,
            source.event_id, source.region_id, source.start_time, source.end_time,
            source.multiplier, source.max_rewards, source.claimed_rewards, 0);

-- Add additional configuration items - use a simple counter for IDs
MERGE INTO configurations USING (
    SELECT 
        20 + ROW_NUMBER() OVER (ORDER BY config_key) AS id,
        config_key,
        config_value,
        description
    FROM (VALUES
        ('WHEEL_SEGMENTS_LARGE', '12', 'Number of wheel segments (large)'),
        ('SPIN_ANIMATION_DURATION_LARGE', '3', 'Spin animation duration (seconds) for large wheels'),
        ('LOW_BALANCE_THRESHOLD_LARGE', '3', 'Low balance warning threshold for large events'),
        ('GOLDEN_HOUR_NOTIFICATION_LARGE', 'true', 'Show golden hour notifications for large events'),
        ('REWARD_PROBABILITY_DECIMAL_PLACES_LARGE', '4', 'Decimal places for reward probability display (large)'),
        ('UI_REFRESH_INTERVAL_LARGE', '60', 'UI data refresh interval (seconds) for large screens'),
        ('REPORTS_DEFAULT_DATE_RANGE_LARGE', '30', 'Default date range for reports (days) for large data')
    ) AS source(config_key, config_value, description)
) AS source
ON configurations.config_key = source.config_key
WHEN NOT MATCHED THEN
    INSERT (id, created_by, created_at, updated_by, updated_at, status, config_key, config_value, description, version)
    VALUES (source.id, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 
            'ACTIVE', source.config_key, source.config_value, source.description, 0);

-- Commit the transaction
COMMIT;
