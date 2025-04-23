-- Event Locations table with composite primary key
CREATE TABLE event_locations (
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    description TEXT,
    max_spin INT DEFAULT 100,
    today_spin INT DEFAULT 100,
    daily_spin_distributing_rate DOUBLE DEFAULT 0,
    daily_spin_dist_rate DOUBLE DEFAULT 0,  -- Original field from entity
    remaining_today_spin DOUBLE DEFAULT 0,  -- Add the missing column that Hibernate expects
    version BIGINT DEFAULT 0,
    PRIMARY KEY (event_id, region_id),
    FOREIGN KEY (event_id) REFERENCES events(id),
    FOREIGN KEY (region_id) REFERENCES regions(id)
);

-- Check if rewards table exists, create it if not
CREATE TABLE IF NOT EXISTS rewards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    event_location_id BIGINT,
    prize_value DECIMAL(15,2),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_location_id) REFERENCES event_locations(event_id)
);
