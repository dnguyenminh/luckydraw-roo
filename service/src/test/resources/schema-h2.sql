-- Schema for H2 database (test environment)
-- Use IF NOT EXISTS for all table definitions

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    object_type VARCHAR(100),
    object_id VARCHAR(100),
    property_path VARCHAR(255),
    old_value CLOB,
    new_value CLOB,
    value_type VARCHAR(100),
    update_time TIMESTAMP,
    context VARCHAR(255),
    action_type VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- User management tables
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    role_type VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    display_order INT DEFAULT 0,
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50), -- Changed from 'permission_type' to 'type'
    description VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    full_name VARCHAR(255),
    role_id BIGINT,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Continue with other table definitions using IF NOT EXISTS
