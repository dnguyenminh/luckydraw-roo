-- H2-specific schema for event_locations with direct id instead of composite key
CREATE TABLE IF NOT EXISTS event_locations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    max_spin INT,
    quantity INT CHECK (quantity >= 0),
    win_probability DECIMAL(5,4),
    event_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_event_locations_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_event_locations_region FOREIGN KEY (region_id) REFERENCES regions(id)
);

-- H2-specific schema for rewards with proper event_location_id reference
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
    CONSTRAINT fk_rewards_event_location FOREIGN KEY (event_location_id) REFERENCES event_locations(id)
);
