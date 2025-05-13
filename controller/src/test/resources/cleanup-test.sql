-- Cleanup script for controller tests to leave database in clean state

-- Clean up all data while preserving schema
DELETE FROM spin_histories;
DELETE FROM golden_hours;
DELETE FROM reward_events;
DELETE FROM rewards;
DELETE FROM participant_events;
DELETE FROM participants;
DELETE FROM event_locations;
DELETE FROM region_provinces;
DELETE FROM provinces;
DELETE FROM events;
DELETE FROM regions;
DELETE FROM role_permissions;
DELETE FROM blacklisted_tokens;
DELETE FROM users;
DELETE FROM permissions;
DELETE FROM roles;
DELETE FROM configurations;
DELETE FROM audit_logs;
