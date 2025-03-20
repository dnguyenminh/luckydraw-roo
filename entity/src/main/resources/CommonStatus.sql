INSERT INTO common_statuses (name, description, is_end_state, allows_modification, display_order)
VALUES 
('ACTIVE', 'Active status', false, true, 10),
('INACTIVE', 'Inactive status', false, false, 20),
('DRAFT', 'Draft status', false, true, 30),
('ARCHIVED', 'Archived status', true, false, 40),
('DELETED', 'Deleted status', true, false, 50)
ON CONFLICT (name) DO UPDATE 
SET description = EXCLUDED.description,
    is_end_state = EXCLUDED.is_end_state,
    allows_modification = EXCLUDED.allows_modification,
    display_order = EXCLUDED.display_order;