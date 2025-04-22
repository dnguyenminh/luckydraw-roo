-- Create event_locations table with direct ID instead of composite key
CREATE TABLE IF NOT EXISTS event_locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
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

-- Create rewards table with reference to event_location.id
CREATE TABLE IF NOT EXISTS rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    event_location_id BIGINT NOT NULL,
    prize_value DECIMAL(10,2),
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_rewards_event_location FOREIGN KEY (event_location_id) REFERENCES event_locations(id)
);

-- Create golden_hours table with reference to event_location.id
CREATE TABLE IF NOT EXISTS golden_hours (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    event_location_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    multiplier DECIMAL(5,2) NOT NULL,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_golden_hours_event_location FOREIGN KEY (event_location_id) REFERENCES event_locations(id)
);

-- Create participant_events table with reference to event_location.id
CREATE TABLE IF NOT EXISTS participant_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    event_id BIGINT NOT NULL,
    event_location_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    spins_remaining INT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_participant_events_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_participant_events_event_location FOREIGN KEY (event_location_id) REFERENCES event_locations(id),
    CONSTRAINT fk_participant_events_participant FOREIGN KEY (participant_id) REFERENCES participants(id)
);
