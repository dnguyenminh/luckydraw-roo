-- Insert test rewards first (if not exists)
INSERT INTO rewards (id, event_id, region_id, name, code, status, prize_value, created_by, created_at)
SELECT 
    x,
    1,
    1,
    'Reward ' || x,
    'REWARD_' || x,
    'ACTIVE',
    100000,
    'system',
    CURRENT_TIMESTAMP
FROM SYSTEM_RANGE(1, 10)
WHERE NOT EXISTS (SELECT 1 FROM rewards WHERE id = x);

-- Insert reward_events data
INSERT INTO reward_events (event_id, region_id, reward_id, quantity, today_quantity, status, created_by, created_at)
SELECT 
    1,
    1,
    x,
    1000,
    1000,
    'ACTIVE',
    'system',
    CURRENT_TIMESTAMP
FROM SYSTEM_RANGE(1, 10)
WHERE NOT EXISTS (
    SELECT 1 FROM reward_events 
    WHERE event_id = 1 AND region_id = 1 AND reward_id = x
);

-- Insert Spin Histories with Golden Hour linking
INSERT INTO spin_histories (
    id, event_id, participant_id, participant_event_id, participant_region_id,
    reward_event_id, reward_region_id, reward_id, golden_hour_id,
    spin_time, win, multiplier, status, created_by, created_at
)
SELECT
    x,
    pe.event_id,
    pe.participant_id,
    pe.event_id,
    pe.region_id,
    CASE WHEN MOD(x, 3) = 0 THEN pe.event_id ELSE NULL END,
    CASE WHEN MOD(x, 3) = 0 THEN pe.region_id ELSE NULL END,
    CASE WHEN MOD(x, 3) = 0 THEN MOD(x, 10) + 1 ELSE NULL END,
    CASE WHEN MOD(x, 5) = 0 THEN
        (SELECT id FROM golden_hours gh
         WHERE gh.event_id = pe.event_id
         AND gh.region_id = pe.region_id
         AND gh.status = 'ACTIVE'
         FETCH FIRST 1 ROW ONLY)
    ELSE NULL END,
    DATEADD('MINUTE', -1 * (10 - x), CURRENT_TIMESTAMP),
    CASE WHEN MOD(x, 3) = 0 THEN TRUE ELSE FALSE END,
    CASE WHEN MOD(x, 5) = 0 THEN 1.5 ELSE 1.0 END,
    'ACTIVE',
    'system',
    CURRENT_TIMESTAMP
FROM SYSTEM_RANGE(1, 10)
CROSS JOIN (
    SELECT event_id, region_id, participant_id
    FROM participant_events
    WHERE status = 'ACTIVE'
    LIMIT 1
);
