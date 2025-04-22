-- Update the quantity attribute in event_locations to ensure it's not null
UPDATE event_locations SET quantity = 0 WHERE quantity IS NULL;

-- Fix any potential referential integrity issues with rewards and event_locations
UPDATE rewards SET event_location_id = NULL WHERE event_location_id NOT IN (SELECT id FROM event_locations);
