-- Test database schema setup

-- Create database objects in correct order
CREATE TABLE IF NOT EXISTS regions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status INTEGER DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by_id VARCHAR(255),
    updated_by_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS provinces (
    id BIGSERIAL PRIMARY KEY,
    region_id BIGINT REFERENCES regions(id),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    status INTEGER DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by_id VARCHAR(255),
    updated_by_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    status INTEGER DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by_id VARCHAR(255),
    updated_by_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    initial_spins INTEGER DEFAULT 0,
    daily_spin_limit INTEGER DEFAULT 0,
    default_win_probability DOUBLE PRECISION DEFAULT 0.0,
    status INTEGER DEFAULT 1,
    metadata TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by_id VARCHAR(255),
    updated_by_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS event_locations (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id),
    region_id BIGINT NOT NULL REFERENCES regions(id),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    initial_spins INTEGER DEFAULT 0,
    daily_spin_limit INTEGER DEFAULT 0,
    default_win_probability DOUBLE PRECISION DEFAULT 0.0,
    status INTEGER DEFAULT 1,
    metadata TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by_id VARCHAR(255),
    updated_by_id VARCHAR(255),
    UNIQUE(event_id, region_id)
);

CREATE TABLE IF NOT EXISTS participants (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255),
    account VARCHAR(50) NOT NULL UNIQUE,
    metadata TEXT,
    province_id BIGINT REFERENCES provinces(id),
    user_id BIGINT REFERENCES users(id),
    status INTEGER DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by_id VARCHAR(255),
    updated_by_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    status INTEGER DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by_id VARCHAR(255),
    updated_by_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS participant_roles (
    participant_id BIGINT NOT NULL REFERENCES participants(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (participant_id, role_id)
);

CREATE TABLE IF NOT EXISTS rewards (
    id BIGSERIAL PRIMARY KEY,
    event_location_id BIGINT NOT NULL REFERENCES event_locations(id),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    points INTEGER DEFAULT 0,
    points_required INTEGER DEFAULT 0,
    total_quantity INTEGER DEFAULT 0,
    remaining_quantity INTEGER DEFAULT 0,
    daily_limit INTEGER DEFAULT 0,
    win_probability DOUBLE PRECISION DEFAULT 0.0,
    valid_from TIMESTAMP,
    valid_until TIMESTAMP,
    status INTEGER DEFAULT 1,
    metadata TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by_id VARCHAR(255),
    updated_by_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS golden_hours (
    id BIGSERIAL PRIMARY KEY,
    event_location_id BIGINT NOT NULL REFERENCES event_locations(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    win_probability DOUBLE PRECISION DEFAULT 0.0,
    status INTEGER DEFAULT 1,
    metadata TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by_id VARCHAR(255),
    updated_by_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS participant_events (
    id BIGSERIAL PRIMARY KEY,
    participant_id BIGINT NOT NULL REFERENCES participants(id),
    event_location_id BIGINT NOT NULL REFERENCES event_locations(id),
    total_spins INTEGER DEFAULT 0,
    available_spins INTEGER DEFAULT 0,
    daily_spin_count INTEGER DEFAULT 0,
    total_wins INTEGER DEFAULT 0,
    total_points INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    metadata TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by_id VARCHAR(255),
    updated_by_id VARCHAR(255),
    UNIQUE(participant_id, event_location_id)
);

CREATE TABLE IF NOT EXISTS spin_histories (
    id BIGSERIAL PRIMARY KEY,
    participant_id BIGINT NOT NULL REFERENCES participants(id),
    event_location_id BIGINT NOT NULL REFERENCES event_locations(id),
    reward_id BIGINT REFERENCES rewards(id),
    golden_hour_id BIGINT REFERENCES golden_hours(id),
    timestamp TIMESTAMP NOT NULL,
    win BOOLEAN DEFAULT FALSE,
    points_earned INTEGER DEFAULT 0,
    points_spent INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    metadata TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by_id VARCHAR(255),
    updated_by_id VARCHAR(255)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_event_code ON events(code);
CREATE INDEX IF NOT EXISTS idx_event_dates ON events(start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_event_location_code ON event_locations(code);
CREATE INDEX IF NOT EXISTS idx_participant_code ON participants(code);
CREATE INDEX IF NOT EXISTS idx_reward_code ON rewards(code);
CREATE INDEX IF NOT EXISTS idx_spin_history_date ON spin_histories(timestamp);

    create table blacklisted_tokens (
        status integer not null,
        created_at timestamp(6),
        expiry_time timestamp(6) not null,
        id bigserial not null,
        updated_at timestamp(6),
        user_id bigint,
        version bigint not null,
        created_by varchar(255),
        metadata TEXT,
        reason varchar(255),
        token varchar(255) not null unique,
        token_type varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_locations (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        event_id bigint,
        id bigserial not null,
        province_id bigint,
        region_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table events (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        end_time timestamp(6),
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table golden_hours (
        daily_limit integer,
        points_multiplier float(53),
        status integer not null,
        total_uses integer,
        win_probability float(53),
        win_probability_multiplier float(53),
        created_at timestamp(6),
        end_time timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_events (
        available_spins integer,
        daily_spin_count integer,
        status integer not null,
        total_points integer,
        total_spins integer,
        total_wins integer,
        created_at timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        participant_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_roles (
        participant_id bigint not null,
        role_id bigint not null,
        primary key (participant_id, role_id)
    );

    create table participants (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        province_id bigint,
        updated_at timestamp(6),
        user_id bigint unique,
        version bigint not null,
        account varchar(255) not null unique,
        code varchar(255),
        created_by varchar(255),
        email varchar(255),
        metadata varchar(255),
        name varchar(255) not null,
        phone varchar(255) not null unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table provinces (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        region_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table regions (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table rewards (
        daily_count integer,
        daily_limit integer,
        points integer,
        points_required integer,
        remaining_quantity integer,
        status integer not null,
        total_quantity integer,
        win_probability float(53),
        created_at timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        updated_at timestamp(6),
        valid_from timestamp(6),
        valid_until timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table roles (
        priority integer,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255) unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table spin_histories (
        points_earned integer,
        points_spent integer,
        status integer not null,
        win boolean,
        created_at timestamp(6),
        event_location_id bigint,
        golden_hour_id bigint,
        id bigserial not null,
        participant_id bigint,
        reward_id bigint,
        timestamp timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table user_roles (
        role_id bigint not null,
        user_id bigint not null,
        primary key (role_id, user_id)
    );

    create table users (
        account_locked boolean,
        account_non_expired boolean,
        account_non_locked boolean,
        credentials_non_expired boolean,
        failed_attempts integer,
        password_expired boolean,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        locked_until timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        email varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        metadata varchar(255),
        password varchar(255),
        phone_number varchar(255),
        position varchar(255),
        refresh_token varchar(255),
        updated_by varchar(255),
        username varchar(255) unique,
        primary key (id)
    );

    alter table if exists blacklisted_tokens 
       add constraint FKt7v2sldreoy4pqla7y6kd7uu 
       foreign key (user_id) 
       references users;

    alter table if exists event_locations 
       add constraint FKdquu070vft3scg2w79xy9secn 
       foreign key (event_id) 
       references events;

    alter table if exists event_locations 
       add constraint FKoputwo3ch1tbxhv4mp5m0p2gc 
       foreign key (province_id) 
       references provinces;

    alter table if exists event_locations 
       add constraint FKs41txxkfquf582772ahkwlub2 
       foreign key (region_id) 
       references regions;

    alter table if exists golden_hours 
       add constraint FKiextogf8tkm1q3gu9vblyswnq 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists participant_events 
       add constraint FKd015ah4sdlr6ry5rx5nvnu6qa 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists participant_events 
       add constraint FKjjitit5lfpkqyk3nvwel8xgf 
       foreign key (participant_id) 
       references participants;

    alter table if exists participant_roles 
       add constraint FK70v5tkbsbn1o6xl1c22b4owtn 
       foreign key (role_id) 
       references roles;

    alter table if exists participant_roles 
       add constraint FK5ub8a1wn0n40afuju04u13qlh 
       foreign key (participant_id) 
       references participants;

    alter table if exists participants 
       add constraint FKapubofdx7bicmjxnngjaf4mcj 
       foreign key (province_id) 
       references provinces;

    alter table if exists participants 
       add constraint FKghixrahoj1s8cloinfx8lyeqa 
       foreign key (user_id) 
       references users;

    alter table if exists provinces 
       add constraint FKr52p9hvmia0r4042b4s4h6qil 
       foreign key (region_id) 
       references regions;

    alter table if exists rewards 
       add constraint FK465nimj9nfk08cul0s94bu79n 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists spin_histories 
       add constraint FKn5ghsrn4a8u3tve4lpbat9cf1 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists spin_histories 
       add constraint FKcje0w53l1u9ctyuxv36ufpmmu 
       foreign key (golden_hour_id) 
       references golden_hours;

    alter table if exists spin_histories 
       add constraint FKqyc5uaa3ar8vpbrbom3tspk2x 
       foreign key (participant_id) 
       references participants;

    alter table if exists spin_histories 
       add constraint FKatkkxrtpf21fsnr98awyel68c 
       foreign key (reward_id) 
       references rewards;

    alter table if exists user_roles 
       add constraint FKh8ciramu9cc9q3qcqiv4ue8a6 
       foreign key (role_id) 
       references roles;

    alter table if exists user_roles 
       add constraint FKhfh9dx7w3ubf1co1vdev94g3f 
       foreign key (user_id) 
       references users;

    create table blacklisted_tokens (
        status integer not null,
        created_at timestamp(6),
        expiry_time timestamp(6) not null,
        id bigserial not null,
        updated_at timestamp(6),
        user_id bigint,
        version bigint not null,
        created_by varchar(255),
        metadata TEXT,
        reason varchar(255),
        token varchar(255) not null unique,
        token_type varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_locations (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        event_id bigint,
        id bigserial not null,
        province_id bigint,
        region_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table events (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        end_time timestamp(6),
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table golden_hours (
        daily_limit integer,
        points_multiplier float(53),
        status integer not null,
        total_uses integer,
        win_probability float(53),
        win_probability_multiplier float(53),
        created_at timestamp(6),
        end_time timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_events (
        available_spins integer,
        daily_spin_count integer,
        status integer not null,
        total_points integer,
        total_spins integer,
        total_wins integer,
        created_at timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        last_spin_time timestamp(6),
        last_sync_time timestamp(6),
        participant_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_roles (
        participant_id bigint not null,
        role_id bigint not null,
        primary key (participant_id, role_id)
    );

    create table participants (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        province_id bigint,
        updated_at timestamp(6),
        user_id bigint unique,
        version bigint not null,
        account varchar(255) not null unique,
        code varchar(255),
        created_by varchar(255),
        email varchar(255),
        metadata varchar(255),
        name varchar(255) not null,
        phone varchar(255) not null unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table provinces (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        region_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table regions (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table rewards (
        daily_count integer,
        daily_limit integer,
        points integer,
        points_required integer,
        remaining_quantity integer,
        status integer not null,
        total_quantity integer,
        win_probability float(53),
        created_at timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        updated_at timestamp(6),
        valid_from timestamp(6),
        valid_until timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table roles (
        priority integer,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255) unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table spin_histories (
        points_earned integer,
        points_spent integer,
        status integer not null,
        win boolean,
        created_at timestamp(6),
        event_location_id bigint,
        golden_hour_id bigint,
        id bigserial not null,
        participant_id bigint,
        reward_id bigint,
        timestamp timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table user_roles (
        role_id bigint not null,
        user_id bigint not null,
        primary key (role_id, user_id)
    );

    create table users (
        account_locked boolean,
        account_non_expired boolean,
        account_non_locked boolean,
        credentials_non_expired boolean,
        failed_attempts integer,
        password_expired boolean,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        locked_until timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        email varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        metadata varchar(255),
        password varchar(255),
        phone_number varchar(255),
        position varchar(255),
        refresh_token varchar(255),
        updated_by varchar(255),
        username varchar(255) unique,
        primary key (id)
    );

    alter table if exists blacklisted_tokens 
       add constraint FKt7v2sldreoy4pqla7y6kd7uu 
       foreign key (user_id) 
       references users;

    alter table if exists event_locations 
       add constraint FKdquu070vft3scg2w79xy9secn 
       foreign key (event_id) 
       references events;

    alter table if exists event_locations 
       add constraint FKoputwo3ch1tbxhv4mp5m0p2gc 
       foreign key (province_id) 
       references provinces;

    alter table if exists event_locations 
       add constraint FKs41txxkfquf582772ahkwlub2 
       foreign key (region_id) 
       references regions;

    alter table if exists golden_hours 
       add constraint FKiextogf8tkm1q3gu9vblyswnq 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists participant_events 
       add constraint FKd015ah4sdlr6ry5rx5nvnu6qa 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists participant_events 
       add constraint FKjjitit5lfpkqyk3nvwel8xgf 
       foreign key (participant_id) 
       references participants;

    alter table if exists participant_roles 
       add constraint FK70v5tkbsbn1o6xl1c22b4owtn 
       foreign key (role_id) 
       references roles;

    alter table if exists participant_roles 
       add constraint FK5ub8a1wn0n40afuju04u13qlh 
       foreign key (participant_id) 
       references participants;

    alter table if exists participants 
       add constraint FKapubofdx7bicmjxnngjaf4mcj 
       foreign key (province_id) 
       references provinces;

    alter table if exists participants 
       add constraint FKghixrahoj1s8cloinfx8lyeqa 
       foreign key (user_id) 
       references users;

    alter table if exists provinces 
       add constraint FKr52p9hvmia0r4042b4s4h6qil 
       foreign key (region_id) 
       references regions;

    alter table if exists rewards 
       add constraint FK465nimj9nfk08cul0s94bu79n 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists spin_histories 
       add constraint FKn5ghsrn4a8u3tve4lpbat9cf1 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists spin_histories 
       add constraint FKcje0w53l1u9ctyuxv36ufpmmu 
       foreign key (golden_hour_id) 
       references golden_hours;

    alter table if exists spin_histories 
       add constraint FKqyc5uaa3ar8vpbrbom3tspk2x 
       foreign key (participant_id) 
       references participants;

    alter table if exists spin_histories 
       add constraint FKatkkxrtpf21fsnr98awyel68c 
       foreign key (reward_id) 
       references rewards;

    alter table if exists user_roles 
       add constraint FKh8ciramu9cc9q3qcqiv4ue8a6 
       foreign key (role_id) 
       references roles;

    alter table if exists user_roles 
       add constraint FKhfh9dx7w3ubf1co1vdev94g3f 
       foreign key (user_id) 
       references users;

    create table blacklisted_tokens (
        status integer not null,
        created_at timestamp(6),
        expiry_time timestamp(6) not null,
        id bigserial not null,
        updated_at timestamp(6),
        user_id bigint,
        version bigint not null,
        created_by varchar(255),
        metadata TEXT,
        reason varchar(255),
        token varchar(255) not null unique,
        token_type varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_locations (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        event_id bigint not null,
        id bigserial not null,
        region_id bigint not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null unique,
        name varchar(100) not null,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_provinces (
        event_id bigint not null,
        province_id bigint not null,
        primary key (event_id, province_id)
    );

    create table events (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        end_time timestamp(6),
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table golden_hours (
        daily_limit integer,
        points_multiplier float(53),
        status integer not null,
        total_uses integer,
        win_probability float(53),
        win_probability_multiplier float(53),
        created_at timestamp(6),
        end_time timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_events (
        daily_spins_used integer,
        initial_spins integer,
        remaining_spins integer,
        status integer not null,
        total_points integer,
        total_spins integer,
        total_wins integer,
        created_at timestamp(6),
        event_id bigint not null,
        event_location_id bigint not null,
        id bigserial not null,
        participant_id bigint not null,
        province_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_roles (
        participant_id bigint not null,
        role_id bigint not null,
        primary key (participant_id, role_id)
    );

    create table participants (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        province_id bigint,
        updated_at timestamp(6),
        user_id bigint unique,
        version bigint not null,
        account varchar(255) not null unique,
        code varchar(255),
        created_by varchar(255),
        email varchar(255),
        metadata varchar(255),
        name varchar(255) not null,
        phone varchar(255) not null unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table provinces (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        population bigint,
        region_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null,
        name varchar(100) not null,
        created_by varchar(255),
        metadata varchar(255),
        region_code varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table region (
        default_win_probability float(53),
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null unique,
        name varchar(100) not null,
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table rewards (
        daily_count integer,
        daily_limit integer,
        points integer,
        points_required integer,
        remaining_quantity integer,
        status integer not null,
        total_quantity integer,
        win_probability float(53),
        created_at timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        updated_at timestamp(6),
        valid_from timestamp(6),
        valid_until timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table role_permissions (
        role_id bigint not null,
        permission varchar(255) check (permission in ('VIEW_EVENTS','CREATE_EVENT','EDIT_EVENT','DELETE_EVENT','VIEW_PARTICIPANTS','CREATE_PARTICIPANT','EDIT_PARTICIPANT','DELETE_PARTICIPANT','VIEW_REWARDS','CREATE_REWARD','EDIT_REWARD','DELETE_REWARD','VIEW_USERS','CREATE_USER','EDIT_USER','DELETE_USER','VIEW_ROLES','CREATE_ROLE','EDIT_ROLE','DELETE_ROLE','MANAGE_SYSTEM','VIEW_REPORTS'))
    );

    create table roles (
        priority integer,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        name varchar(20) check (name in ('ADMIN','MANAGER','OPERATOR','USER','GUEST')),
        code varchar(50) unique,
        description varchar(200),
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table spin_histories (
        finalized boolean,
        points_earned integer,
        status integer not null,
        win boolean,
        created_at timestamp(6),
        event_location_id bigint,
        golden_hour_id bigint,
        id bigserial not null,
        participant_event_id bigint not null,
        reward_id bigint,
        spin_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table user_roles (
        role_id bigint not null,
        user_id bigint not null,
        primary key (role_id, user_id)
    );

    create table users (
        account_expired boolean,
        credentials_expired boolean,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        last_login timestamp(6),
        locked_until timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        email varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        metadata varchar(255),
        password varchar(255) not null,
        phone_number varchar(255),
        position varchar(255),
        refresh_token varchar(255),
        updated_by varchar(255),
        username varchar(255) not null unique,
        primary key (id)
    );

    alter table if exists blacklisted_tokens 
       add constraint FKt7v2sldreoy4pqla7y6kd7uu 
       foreign key (user_id) 
       references users;

    alter table if exists event_locations 
       add constraint FKdquu070vft3scg2w79xy9secn 
       foreign key (event_id) 
       references events;

    alter table if exists event_locations 
       add constraint FK67osrbf60n45yieja8frjcdyp 
       foreign key (region_id) 
       references region;

    alter table if exists event_provinces 
       add constraint FKjery6fo6inw9u08pm2op2mapv 
       foreign key (province_id) 
       references provinces;

    alter table if exists event_provinces 
       add constraint FKhb3bbux1ipjav2d3wap0o1n6x 
       foreign key (event_id) 
       references events;

    alter table if exists golden_hours 
       add constraint FKiextogf8tkm1q3gu9vblyswnq 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists participant_events 
       add constraint FKsodxjcwjkui9kprbjnq6687h6 
       foreign key (event_id) 
       references events;

    alter table if exists participant_events 
       add constraint FKd015ah4sdlr6ry5rx5nvnu6qa 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists participant_events 
       add constraint FKjjitit5lfpkqyk3nvwel8xgf 
       foreign key (participant_id) 
       references participants;

    alter table if exists participant_events 
       add constraint FK3e479x0gga1sw3ojsib6s5llo 
       foreign key (province_id) 
       references provinces;

    alter table if exists participant_roles 
       add constraint FK70v5tkbsbn1o6xl1c22b4owtn 
       foreign key (role_id) 
       references roles;

    alter table if exists participant_roles 
       add constraint FK5ub8a1wn0n40afuju04u13qlh 
       foreign key (participant_id) 
       references participants;

    alter table if exists participants 
       add constraint FKapubofdx7bicmjxnngjaf4mcj 
       foreign key (province_id) 
       references provinces;

    alter table if exists participants 
       add constraint FKghixrahoj1s8cloinfx8lyeqa 
       foreign key (user_id) 
       references users;

    alter table if exists provinces 
       add constraint FKbvdfhcpk9245le7drrydsbuc4 
       foreign key (region_id) 
       references region;

    alter table if exists rewards 
       add constraint FK465nimj9nfk08cul0s94bu79n 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists role_permissions 
       add constraint FKn5fotdgk8d1xvo8nav9uv3muc 
       foreign key (role_id) 
       references roles;

    alter table if exists spin_histories 
       add constraint FKn5ghsrn4a8u3tve4lpbat9cf1 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists spin_histories 
       add constraint FKcje0w53l1u9ctyuxv36ufpmmu 
       foreign key (golden_hour_id) 
       references golden_hours;

    alter table if exists spin_histories 
       add constraint FKdsnjjkw28f8i55mpqn22q6j6i 
       foreign key (participant_event_id) 
       references participant_events;

    alter table if exists spin_histories 
       add constraint FKatkkxrtpf21fsnr98awyel68c 
       foreign key (reward_id) 
       references rewards;

    alter table if exists user_roles 
       add constraint FKh8ciramu9cc9q3qcqiv4ue8a6 
       foreign key (role_id) 
       references roles;

    alter table if exists user_roles 
       add constraint FKhfh9dx7w3ubf1co1vdev94g3f 
       foreign key (user_id) 
       references users;

    create table blacklisted_tokens (
        status integer not null,
        created_at timestamp(6),
        expiry_time timestamp(6) not null,
        id bigserial not null,
        updated_at timestamp(6),
        user_id bigint,
        version bigint not null,
        created_by varchar(255),
        metadata TEXT,
        reason varchar(255),
        token varchar(255) not null unique,
        token_type varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_locations (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        event_id bigint not null,
        id bigserial not null,
        region_id bigint not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null unique,
        name varchar(100) not null,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_provinces (
        event_id bigint not null,
        province_id bigint not null,
        primary key (event_id, province_id)
    );

    create table events (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        end_time timestamp(6),
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table golden_hours (
        daily_limit integer,
        points_multiplier float(53),
        status integer not null,
        total_uses integer,
        win_probability float(53),
        win_probability_multiplier float(53),
        created_at timestamp(6),
        end_time timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_events (
        daily_spins_used integer,
        initial_spins integer,
        remaining_spins integer,
        status integer not null,
        total_points integer,
        total_spins integer,
        total_wins integer,
        created_at timestamp(6),
        event_id bigint not null,
        event_location_id bigint not null,
        id bigserial not null,
        participant_id bigint not null,
        province_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_roles (
        participant_id bigint not null,
        role_id bigint not null,
        primary key (participant_id, role_id)
    );

    create table participants (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        province_id bigint,
        updated_at timestamp(6),
        user_id bigint unique,
        version bigint not null,
        account varchar(255) not null unique,
        code varchar(255),
        created_by varchar(255),
        email varchar(255),
        metadata varchar(255),
        name varchar(255) not null,
        phone varchar(255) not null unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table provinces (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        population bigint,
        region_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null,
        name varchar(100) not null,
        created_by varchar(255),
        metadata varchar(255),
        region_code varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table region (
        default_win_probability float(53),
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null unique,
        name varchar(100) not null,
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table rewards (
        daily_count integer,
        daily_limit integer,
        points integer,
        points_required integer,
        remaining_quantity integer,
        status integer not null,
        total_quantity integer,
        win_probability float(53),
        created_at timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        updated_at timestamp(6),
        valid_from timestamp(6),
        valid_until timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table role_permissions (
        role_id bigint not null,
        permission varchar(255) check (permission in ('VIEW_EVENTS','CREATE_EVENT','EDIT_EVENT','DELETE_EVENT','VIEW_PARTICIPANTS','CREATE_PARTICIPANT','EDIT_PARTICIPANT','DELETE_PARTICIPANT','VIEW_REWARDS','CREATE_REWARD','EDIT_REWARD','DELETE_REWARD','VIEW_USERS','CREATE_USER','EDIT_USER','DELETE_USER','VIEW_ROLES','CREATE_ROLE','EDIT_ROLE','DELETE_ROLE','MANAGE_SYSTEM','VIEW_REPORTS'))
    );

    create table roles (
        priority integer,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        name varchar(20) check (name in ('ADMIN','MANAGER','OPERATOR','USER','GUEST')),
        code varchar(50) unique,
        description varchar(200),
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table spin_histories (
        finalized boolean,
        points_earned integer,
        status integer not null,
        win boolean,
        created_at timestamp(6),
        event_location_id bigint,
        golden_hour_id bigint,
        id bigserial not null,
        participant_event_id bigint not null,
        reward_id bigint,
        spin_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table user_roles (
        role_id bigint not null,
        user_id bigint not null,
        primary key (role_id, user_id)
    );

    create table users (
        account_expired boolean,
        credentials_expired boolean,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        last_login timestamp(6),
        locked_until timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        email varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        metadata varchar(255),
        password varchar(255) not null,
        phone_number varchar(255),
        position varchar(255),
        refresh_token varchar(255),
        updated_by varchar(255),
        username varchar(255) not null unique,
        primary key (id)
    );

    alter table if exists blacklisted_tokens 
       add constraint FKt7v2sldreoy4pqla7y6kd7uu 
       foreign key (user_id) 
       references users;

    alter table if exists event_locations 
       add constraint FKdquu070vft3scg2w79xy9secn 
       foreign key (event_id) 
       references events;

    alter table if exists event_locations 
       add constraint FK67osrbf60n45yieja8frjcdyp 
       foreign key (region_id) 
       references region;

    alter table if exists event_provinces 
       add constraint FKjery6fo6inw9u08pm2op2mapv 
       foreign key (province_id) 
       references provinces;

    alter table if exists event_provinces 
       add constraint FKhb3bbux1ipjav2d3wap0o1n6x 
       foreign key (event_id) 
       references events;

    alter table if exists golden_hours 
       add constraint FKiextogf8tkm1q3gu9vblyswnq 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists participant_events 
       add constraint FKsodxjcwjkui9kprbjnq6687h6 
       foreign key (event_id) 
       references events;

    alter table if exists participant_events 
       add constraint FKd015ah4sdlr6ry5rx5nvnu6qa 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists participant_events 
       add constraint FKjjitit5lfpkqyk3nvwel8xgf 
       foreign key (participant_id) 
       references participants;

    alter table if exists participant_events 
       add constraint FK3e479x0gga1sw3ojsib6s5llo 
       foreign key (province_id) 
       references provinces;

    alter table if exists participant_roles 
       add constraint FK70v5tkbsbn1o6xl1c22b4owtn 
       foreign key (role_id) 
       references roles;

    alter table if exists participant_roles 
       add constraint FK5ub8a1wn0n40afuju04u13qlh 
       foreign key (participant_id) 
       references participants;

    alter table if exists participants 
       add constraint FKapubofdx7bicmjxnngjaf4mcj 
       foreign key (province_id) 
       references provinces;

    alter table if exists participants 
       add constraint FKghixrahoj1s8cloinfx8lyeqa 
       foreign key (user_id) 
       references users;

    alter table if exists provinces 
       add constraint FKbvdfhcpk9245le7drrydsbuc4 
       foreign key (region_id) 
       references region;

    alter table if exists rewards 
       add constraint FK465nimj9nfk08cul0s94bu79n 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists role_permissions 
       add constraint FKn5fotdgk8d1xvo8nav9uv3muc 
       foreign key (role_id) 
       references roles;

    alter table if exists spin_histories 
       add constraint FKn5ghsrn4a8u3tve4lpbat9cf1 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists spin_histories 
       add constraint FKcje0w53l1u9ctyuxv36ufpmmu 
       foreign key (golden_hour_id) 
       references golden_hours;

    alter table if exists spin_histories 
       add constraint FKdsnjjkw28f8i55mpqn22q6j6i 
       foreign key (participant_event_id) 
       references participant_events;

    alter table if exists spin_histories 
       add constraint FKatkkxrtpf21fsnr98awyel68c 
       foreign key (reward_id) 
       references rewards;

    alter table if exists user_roles 
       add constraint FKh8ciramu9cc9q3qcqiv4ue8a6 
       foreign key (role_id) 
       references roles;

    alter table if exists user_roles 
       add constraint FKhfh9dx7w3ubf1co1vdev94g3f 
       foreign key (user_id) 
       references users;

    create table blacklisted_tokens (
        status integer not null,
        created_at timestamp(6),
        expiry_time timestamp(6) not null,
        id bigserial not null,
        updated_at timestamp(6),
        user_id bigint,
        version bigint not null,
        created_by varchar(255),
        metadata TEXT,
        reason varchar(255),
        token varchar(255) not null unique,
        token_type varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_locations (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        event_id bigint not null,
        id bigserial not null,
        region_id bigint not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null unique,
        name varchar(100) not null,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_provinces (
        event_id bigint not null,
        province_id bigint not null,
        primary key (event_id, province_id)
    );

    create table events (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        end_time timestamp(6),
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table golden_hours (
        daily_limit integer,
        points_multiplier float(53),
        status integer not null,
        total_uses integer,
        win_probability float(53),
        win_probability_multiplier float(53),
        created_at timestamp(6),
        end_time timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_events (
        daily_spins_used integer,
        initial_spins integer,
        remaining_spins integer,
        status integer not null,
        total_points integer,
        total_spins integer,
        total_wins integer,
        created_at timestamp(6),
        event_id bigint not null,
        event_location_id bigint not null,
        id bigserial not null,
        participant_id bigint not null,
        province_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_roles (
        participant_id bigint not null,
        role_id bigint not null,
        primary key (participant_id, role_id)
    );

    create table participants (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        province_id bigint,
        updated_at timestamp(6),
        user_id bigint unique,
        version bigint not null,
        account varchar(255) not null unique,
        code varchar(255),
        created_by varchar(255),
        email varchar(255),
        metadata varchar(255),
        name varchar(255) not null,
        phone varchar(255) not null unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table provinces (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        population bigint,
        region_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null,
        name varchar(100) not null,
        created_by varchar(255),
        metadata varchar(255),
        region_code varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table region (
        default_win_probability float(53),
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null unique,
        name varchar(100) not null,
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table rewards (
        daily_count integer,
        daily_limit integer,
        points integer,
        points_required integer,
        remaining_quantity integer,
        status integer not null,
        total_quantity integer,
        win_probability float(53),
        created_at timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        updated_at timestamp(6),
        valid_from timestamp(6),
        valid_until timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table role_permissions (
        role_id bigint not null,
        permission varchar(255) check (permission in ('VIEW_EVENTS','CREATE_EVENT','EDIT_EVENT','DELETE_EVENT','VIEW_PARTICIPANTS','CREATE_PARTICIPANT','EDIT_PARTICIPANT','DELETE_PARTICIPANT','VIEW_REWARDS','CREATE_REWARD','EDIT_REWARD','DELETE_REWARD','VIEW_USERS','CREATE_USER','EDIT_USER','DELETE_USER','VIEW_ROLES','CREATE_ROLE','EDIT_ROLE','DELETE_ROLE','MANAGE_SYSTEM','VIEW_REPORTS'))
    );

    create table roles (
        priority integer,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        name varchar(20) check (name in ('ADMIN','MANAGER','OPERATOR','USER','GUEST')),
        code varchar(50) unique,
        description varchar(200),
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table spin_histories (
        finalized boolean,
        points_earned integer,
        status integer not null,
        win boolean,
        created_at timestamp(6),
        event_location_id bigint,
        golden_hour_id bigint,
        id bigserial not null,
        participant_event_id bigint not null,
        reward_id bigint,
        spin_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table user_roles (
        role_id bigint not null,
        user_id bigint not null,
        primary key (role_id, user_id)
    );

    create table users (
        account_expired boolean,
        credentials_expired boolean,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        last_login timestamp(6),
        locked_until timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        email varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        metadata varchar(255),
        password varchar(255) not null,
        phone_number varchar(255),
        position varchar(255),
        refresh_token varchar(255),
        updated_by varchar(255),
        username varchar(255) not null unique,
        primary key (id)
    );

    alter table if exists blacklisted_tokens 
       add constraint FKt7v2sldreoy4pqla7y6kd7uu 
       foreign key (user_id) 
       references users;

    alter table if exists event_locations 
       add constraint FKdquu070vft3scg2w79xy9secn 
       foreign key (event_id) 
       references events;

    alter table if exists event_locations 
       add constraint FK67osrbf60n45yieja8frjcdyp 
       foreign key (region_id) 
       references region;

    alter table if exists event_provinces 
       add constraint FKjery6fo6inw9u08pm2op2mapv 
       foreign key (province_id) 
       references provinces;

    alter table if exists event_provinces 
       add constraint FKhb3bbux1ipjav2d3wap0o1n6x 
       foreign key (event_id) 
       references events;

    alter table if exists golden_hours 
       add constraint FKiextogf8tkm1q3gu9vblyswnq 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists participant_events 
       add constraint FKsodxjcwjkui9kprbjnq6687h6 
       foreign key (event_id) 
       references events;

    alter table if exists participant_events 
       add constraint FKd015ah4sdlr6ry5rx5nvnu6qa 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists participant_events 
       add constraint FKjjitit5lfpkqyk3nvwel8xgf 
       foreign key (participant_id) 
       references participants;

    alter table if exists participant_events 
       add constraint FK3e479x0gga1sw3ojsib6s5llo 
       foreign key (province_id) 
       references provinces;

    alter table if exists participant_roles 
       add constraint FK70v5tkbsbn1o6xl1c22b4owtn 
       foreign key (role_id) 
       references roles;

    alter table if exists participant_roles 
       add constraint FK5ub8a1wn0n40afuju04u13qlh 
       foreign key (participant_id) 
       references participants;

    alter table if exists participants 
       add constraint FKapubofdx7bicmjxnngjaf4mcj 
       foreign key (province_id) 
       references provinces;

    alter table if exists participants 
       add constraint FKghixrahoj1s8cloinfx8lyeqa 
       foreign key (user_id) 
       references users;

    alter table if exists provinces 
       add constraint FKbvdfhcpk9245le7drrydsbuc4 
       foreign key (region_id) 
       references region;

    alter table if exists rewards 
       add constraint FK465nimj9nfk08cul0s94bu79n 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists role_permissions 
       add constraint FKn5fotdgk8d1xvo8nav9uv3muc 
       foreign key (role_id) 
       references roles;

    alter table if exists spin_histories 
       add constraint FKn5ghsrn4a8u3tve4lpbat9cf1 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists spin_histories 
       add constraint FKcje0w53l1u9ctyuxv36ufpmmu 
       foreign key (golden_hour_id) 
       references golden_hours;

    alter table if exists spin_histories 
       add constraint FKdsnjjkw28f8i55mpqn22q6j6i 
       foreign key (participant_event_id) 
       references participant_events;

    alter table if exists spin_histories 
       add constraint FKatkkxrtpf21fsnr98awyel68c 
       foreign key (reward_id) 
       references rewards;

    alter table if exists user_roles 
       add constraint FKh8ciramu9cc9q3qcqiv4ue8a6 
       foreign key (role_id) 
       references roles;

    alter table if exists user_roles 
       add constraint FKhfh9dx7w3ubf1co1vdev94g3f 
       foreign key (user_id) 
       references users;

    create table blacklisted_tokens (
        status integer not null,
        created_at timestamp(6),
        expiry_time timestamp(6) not null,
        id bigserial not null,
        updated_at timestamp(6),
        user_id bigint,
        version bigint not null,
        created_by varchar(255),
        metadata TEXT,
        reason varchar(255),
        token varchar(255) not null unique,
        token_type varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_locations (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        event_id bigint not null,
        id bigserial not null,
        region_id bigint not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null unique,
        name varchar(100) not null,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_provinces (
        event_id bigint not null,
        province_id bigint not null,
        primary key (event_id, province_id)
    );

    create table events (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        end_time timestamp(6),
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table golden_hours (
        daily_limit integer,
        points_multiplier float(53),
        status integer not null,
        total_uses integer,
        win_probability float(53),
        win_probability_multiplier float(53),
        created_at timestamp(6),
        end_time timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_events (
        daily_spins_used integer,
        initial_spins integer,
        remaining_spins integer,
        status integer not null,
        total_points integer,
        total_spins integer,
        total_wins integer,
        created_at timestamp(6),
        event_id bigint not null,
        event_location_id bigint not null,
        id bigserial not null,
        participant_id bigint not null,
        province_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_roles (
        participant_id bigint not null,
        role_id bigint not null,
        primary key (participant_id, role_id)
    );

    create table participants (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        province_id bigint,
        updated_at timestamp(6),
        user_id bigint unique,
        version bigint not null,
        account varchar(255) not null unique,
        code varchar(255),
        created_by varchar(255),
        email varchar(255),
        metadata varchar(255),
        name varchar(255) not null,
        phone varchar(255) not null unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table provinces (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        population bigint,
        region_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null,
        name varchar(100) not null,
        created_by varchar(255),
        metadata varchar(255),
        region_code varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table region (
        default_win_probability float(53),
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null unique,
        name varchar(100) not null,
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table rewards (
        daily_count integer,
        daily_limit integer,
        points integer,
        points_required integer,
        remaining_quantity integer,
        status integer not null,
        total_quantity integer,
        win_probability float(53),
        created_at timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        updated_at timestamp(6),
        valid_from timestamp(6),
        valid_until timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table role_permissions (
        role_id bigint not null,
        permission varchar(255) check (permission in ('VIEW_EVENTS','CREATE_EVENT','EDIT_EVENT','DELETE_EVENT','VIEW_PARTICIPANTS','CREATE_PARTICIPANT','EDIT_PARTICIPANT','DELETE_PARTICIPANT','VIEW_REWARDS','CREATE_REWARD','EDIT_REWARD','DELETE_REWARD','VIEW_USERS','CREATE_USER','EDIT_USER','DELETE_USER','VIEW_ROLES','CREATE_ROLE','EDIT_ROLE','DELETE_ROLE','MANAGE_SYSTEM','VIEW_REPORTS'))
    );

    create table roles (
        priority integer,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        name varchar(20) check (name in ('ADMIN','MANAGER','OPERATOR','USER','GUEST')),
        code varchar(50) unique,
        description varchar(200),
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table spin_histories (
        finalized boolean,
        points_earned integer,
        status integer not null,
        win boolean,
        created_at timestamp(6),
        event_location_id bigint,
        golden_hour_id bigint,
        id bigserial not null,
        participant_event_id bigint not null,
        reward_id bigint,
        spin_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table user_roles (
        role_id bigint not null,
        user_id bigint not null,
        primary key (role_id, user_id)
    );

    create table users (
        account_expired boolean,
        credentials_expired boolean,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        last_login timestamp(6),
        locked_until timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        email varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        metadata varchar(255),
        password varchar(255) not null,
        phone_number varchar(255),
        position varchar(255),
        refresh_token varchar(255),
        updated_by varchar(255),
        username varchar(255) not null unique,
        primary key (id)
    );

    alter table if exists blacklisted_tokens 
       add constraint FKt7v2sldreoy4pqla7y6kd7uu 
       foreign key (user_id) 
       references users;

    alter table if exists event_locations 
       add constraint FKdquu070vft3scg2w79xy9secn 
       foreign key (event_id) 
       references events;

    alter table if exists event_locations 
       add constraint FK67osrbf60n45yieja8frjcdyp 
       foreign key (region_id) 
       references region;

    alter table if exists event_provinces 
       add constraint FKjery6fo6inw9u08pm2op2mapv 
       foreign key (province_id) 
       references provinces;

    alter table if exists event_provinces 
       add constraint FKhb3bbux1ipjav2d3wap0o1n6x 
       foreign key (event_id) 
       references events;

    alter table if exists golden_hours 
       add constraint FKiextogf8tkm1q3gu9vblyswnq 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists participant_events 
       add constraint FKsodxjcwjkui9kprbjnq6687h6 
       foreign key (event_id) 
       references events;

    alter table if exists participant_events 
       add constraint FKd015ah4sdlr6ry5rx5nvnu6qa 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists participant_events 
       add constraint FKjjitit5lfpkqyk3nvwel8xgf 
       foreign key (participant_id) 
       references participants;

    alter table if exists participant_events 
       add constraint FK3e479x0gga1sw3ojsib6s5llo 
       foreign key (province_id) 
       references provinces;

    alter table if exists participant_roles 
       add constraint FK70v5tkbsbn1o6xl1c22b4owtn 
       foreign key (role_id) 
       references roles;

    alter table if exists participant_roles 
       add constraint FK5ub8a1wn0n40afuju04u13qlh 
       foreign key (participant_id) 
       references participants;

    alter table if exists participants 
       add constraint FKapubofdx7bicmjxnngjaf4mcj 
       foreign key (province_id) 
       references provinces;

    alter table if exists participants 
       add constraint FKghixrahoj1s8cloinfx8lyeqa 
       foreign key (user_id) 
       references users;

    alter table if exists provinces 
       add constraint FKbvdfhcpk9245le7drrydsbuc4 
       foreign key (region_id) 
       references region;

    alter table if exists rewards 
       add constraint FK465nimj9nfk08cul0s94bu79n 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists role_permissions 
       add constraint FKn5fotdgk8d1xvo8nav9uv3muc 
       foreign key (role_id) 
       references roles;

    alter table if exists spin_histories 
       add constraint FKn5ghsrn4a8u3tve4lpbat9cf1 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists spin_histories 
       add constraint FKcje0w53l1u9ctyuxv36ufpmmu 
       foreign key (golden_hour_id) 
       references golden_hours;

    alter table if exists spin_histories 
       add constraint FKdsnjjkw28f8i55mpqn22q6j6i 
       foreign key (participant_event_id) 
       references participant_events;

    alter table if exists spin_histories 
       add constraint FKatkkxrtpf21fsnr98awyel68c 
       foreign key (reward_id) 
       references rewards;

    alter table if exists user_roles 
       add constraint FKh8ciramu9cc9q3qcqiv4ue8a6 
       foreign key (role_id) 
       references roles;

    alter table if exists user_roles 
       add constraint FKhfh9dx7w3ubf1co1vdev94g3f 
       foreign key (user_id) 
       references users;

    create table blacklisted_tokens (
        status integer not null,
        created_at timestamp(6),
        expiry_time timestamp(6) not null,
        id bigserial not null,
        updated_at timestamp(6),
        user_id bigint,
        version bigint not null,
        created_by varchar(255),
        metadata TEXT,
        reason varchar(255),
        token varchar(255) not null unique,
        token_type varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        end_time timestamp(6),
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_location (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        event_id bigint not null,
        id bigserial not null,
        region_id bigint not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null unique,
        name varchar(100) not null,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_provinces (
        event_id bigint not null,
        province_id bigint not null,
        primary key (event_id, province_id)
    );

    create table golden_hours (
        daily_limit integer,
        points_multiplier float(53),
        status integer not null,
        total_uses integer,
        win_probability float(53),
        win_probability_multiplier float(53),
        created_at timestamp(6),
        end_time timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_events (
        daily_spins_used integer,
        initial_spins integer,
        remaining_spins integer,
        status integer not null,
        total_points integer,
        total_spins integer,
        total_wins integer,
        created_at timestamp(6),
        event_id bigint not null,
        event_location_id bigint not null,
        id bigserial not null,
        participant_id bigint not null,
        province_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_roles (
        participant_id bigint not null,
        role_id bigint not null,
        primary key (participant_id, role_id)
    );

    create table participants (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        province_id bigint,
        updated_at timestamp(6),
        user_id bigint unique,
        version bigint not null,
        account varchar(255) not null unique,
        code varchar(255),
        created_by varchar(255),
        email varchar(255),
        metadata varchar(255),
        name varchar(255) not null,
        phone varchar(255) not null unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table province (
        default_win_probability float(53),
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        population bigint,
        region_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null,
        name varchar(100) not null,
        created_by varchar(255),
        metadata varchar(255),
        region_code varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table region (
        default_win_probability float(53),
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null unique,
        name varchar(100) not null,
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table rewards (
        daily_count integer,
        daily_limit integer,
        points integer,
        points_required integer,
        remaining_quantity integer,
        status integer not null,
        total_quantity integer,
        win_probability float(53),
        created_at timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        updated_at timestamp(6),
        valid_from timestamp(6),
        valid_until timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table role_permissions (
        role_id bigint not null,
        permission varchar(255) check (permission in ('VIEW_EVENTS','CREATE_EVENT','EDIT_EVENT','DELETE_EVENT','VIEW_PARTICIPANTS','CREATE_PARTICIPANT','EDIT_PARTICIPANT','DELETE_PARTICIPANT','VIEW_REWARDS','CREATE_REWARD','EDIT_REWARD','DELETE_REWARD','VIEW_USERS','CREATE_USER','EDIT_USER','DELETE_USER','VIEW_ROLES','CREATE_ROLE','EDIT_ROLE','DELETE_ROLE','MANAGE_SYSTEM','VIEW_REPORTS'))
    );

    create table roles (
        priority integer,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        name varchar(20) check (name in ('ADMIN','MANAGER','OPERATOR','USER','GUEST')),
        code varchar(50) unique,
        description varchar(200),
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table spin_histories (
        finalized boolean,
        points_earned integer,
        status integer not null,
        win boolean,
        created_at timestamp(6),
        event_location_id bigint,
        golden_hour_id bigint,
        id bigserial not null,
        participant_event_id bigint not null,
        reward_id bigint,
        spin_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table user_roles (
        role_id bigint not null,
        user_id bigint not null,
        primary key (role_id, user_id)
    );

    create table users (
        account_expired boolean,
        credentials_expired boolean,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        last_login timestamp(6),
        locked_until timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        email varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        metadata varchar(255),
        password varchar(255) not null,
        phone_number varchar(255),
        position varchar(255),
        refresh_token varchar(255),
        updated_by varchar(255),
        username varchar(255) not null unique,
        primary key (id)
    );

    alter table if exists blacklisted_tokens 
       add constraint FKt7v2sldreoy4pqla7y6kd7uu 
       foreign key (user_id) 
       references users;

    alter table if exists event_location 
       add constraint FKbn1d7fexc2sywojnk93i7vvye 
       foreign key (event_id) 
       references event;

    alter table if exists event_location 
       add constraint FKhclmyywn8a6ts46igc2et4re3 
       foreign key (region_id) 
       references region;

    alter table if exists event_provinces 
       add constraint FKq3o6xx4gjdhb6hw34blvuhy7g 
       foreign key (province_id) 
       references province;

    alter table if exists event_provinces 
       add constraint FKa6j8my1tiy5ob5wvnwucrjm6a 
       foreign key (event_id) 
       references event;

    alter table if exists golden_hours 
       add constraint FKd1lq9qcki1lngxlt30om69ep7 
       foreign key (event_location_id) 
       references event_location;

    alter table if exists participant_events 
       add constraint FKfqvcfcgot4i0e581y5gjc2iyn 
       foreign key (event_id) 
       references event;

    alter table if exists participant_events 
       add constraint FKbwn8ufullhdam9rbks8lw0hgl 
       foreign key (event_location_id) 
       references event_location;

    alter table if exists participant_events 
       add constraint FKjjitit5lfpkqyk3nvwel8xgf 
       foreign key (participant_id) 
       references participants;

    alter table if exists participant_events 
       add constraint FKrqh4au8kefrfcf4vm9idlqj0d 
       foreign key (province_id) 
       references province;

    alter table if exists participant_roles 
       add constraint FK70v5tkbsbn1o6xl1c22b4owtn 
       foreign key (role_id) 
       references roles;

    alter table if exists participant_roles 
       add constraint FK5ub8a1wn0n40afuju04u13qlh 
       foreign key (participant_id) 
       references participants;

    alter table if exists participants 
       add constraint FKigcjsqxpvlml58wc1ys0e95je 
       foreign key (province_id) 
       references province;

    alter table if exists participants 
       add constraint FKghixrahoj1s8cloinfx8lyeqa 
       foreign key (user_id) 
       references users;

    alter table if exists province 
       add constraint FKc7qs0yyib7q7aeqek906g81cn 
       foreign key (region_id) 
       references region;

    alter table if exists rewards 
       add constraint FKdwssrhmk120pnee3bd6fi9abq 
       foreign key (event_location_id) 
       references event_location;

    alter table if exists role_permissions 
       add constraint FKn5fotdgk8d1xvo8nav9uv3muc 
       foreign key (role_id) 
       references roles;

    alter table if exists spin_histories 
       add constraint FK3jjr3flpxvcdejf8gn4uqrfgu 
       foreign key (event_location_id) 
       references event_location;

    alter table if exists spin_histories 
       add constraint FKcje0w53l1u9ctyuxv36ufpmmu 
       foreign key (golden_hour_id) 
       references golden_hours;

    alter table if exists spin_histories 
       add constraint FKdsnjjkw28f8i55mpqn22q6j6i 
       foreign key (participant_event_id) 
       references participant_events;

    alter table if exists spin_histories 
       add constraint FKatkkxrtpf21fsnr98awyel68c 
       foreign key (reward_id) 
       references rewards;

    alter table if exists user_roles 
       add constraint FKh8ciramu9cc9q3qcqiv4ue8a6 
       foreign key (role_id) 
       references roles;

    alter table if exists user_roles 
       add constraint FKhfh9dx7w3ubf1co1vdev94g3f 
       foreign key (user_id) 
       references users;

    create table blacklisted_tokens (
        status integer not null,
        created_at timestamp(6),
        expiry_time timestamp(6) not null,
        id bigserial not null,
        updated_at timestamp(6),
        user_id bigint,
        version bigint not null,
        created_by varchar(255),
        metadata TEXT,
        reason varchar(255),
        token varchar(255) not null unique,
        token_type varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        end_time timestamp(6),
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_location (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        event_id bigint not null,
        id bigserial not null,
        region_id bigint not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null unique,
        name varchar(100) not null,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_provinces (
        event_id bigint not null,
        province_id bigint not null,
        primary key (event_id, province_id)
    );

    create table golden_hours (
        daily_limit integer,
        points_multiplier float(53),
        status integer not null,
        total_uses integer,
        win_probability float(53),
        win_probability_multiplier float(53),
        created_at timestamp(6),
        end_time timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_events (
        daily_spins_used integer,
        initial_spins integer,
        remaining_spins integer,
        status integer not null,
        total_points integer,
        total_spins integer,
        total_wins integer,
        created_at timestamp(6),
        event_id bigint not null,
        event_location_id bigint not null,
        id bigserial not null,
        participant_id bigint not null,
        province_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_roles (
        participant_id bigint not null,
        role_id bigint not null,
        primary key (participant_id, role_id)
    );

    create table participants (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        province_id bigint,
        updated_at timestamp(6),
        user_id bigint unique,
        version bigint not null,
        account varchar(255) not null unique,
        code varchar(255),
        created_by varchar(255),
        email varchar(255),
        metadata varchar(255),
        name varchar(255) not null,
        phone varchar(255) not null unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table province (
        default_win_probability float(53),
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        population bigint,
        region_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null,
        name varchar(100) not null,
        created_by varchar(255),
        metadata varchar(255),
        region_code varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table region (
        default_win_probability float(53),
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null unique,
        name varchar(100) not null,
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table rewards (
        daily_count integer,
        daily_limit integer,
        points integer,
        points_required integer,
        remaining_quantity integer,
        status integer not null,
        total_quantity integer,
        win_probability float(53),
        created_at timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        updated_at timestamp(6),
        valid_from timestamp(6),
        valid_until timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table role_permissions (
        role_id bigint not null,
        permission varchar(255) check (permission in ('VIEW_EVENTS','CREATE_EVENT','EDIT_EVENT','DELETE_EVENT','VIEW_PARTICIPANTS','CREATE_PARTICIPANT','EDIT_PARTICIPANT','DELETE_PARTICIPANT','VIEW_REWARDS','CREATE_REWARD','EDIT_REWARD','DELETE_REWARD','VIEW_USERS','CREATE_USER','EDIT_USER','DELETE_USER','VIEW_ROLES','CREATE_ROLE','EDIT_ROLE','DELETE_ROLE','MANAGE_SYSTEM','VIEW_REPORTS'))
    );

    create table roles (
        priority integer,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        name varchar(20) check (name in ('ADMIN','MANAGER','OPERATOR','USER','GUEST')),
        code varchar(50) unique,
        description varchar(200),
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table spin_histories (
        finalized boolean,
        points_earned integer,
        status integer not null,
        win boolean,
        created_at timestamp(6),
        event_location_id bigint,
        golden_hour_id bigint,
        id bigserial not null,
        participant_event_id bigint not null,
        reward_id bigint,
        spin_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table user_roles (
        role_id bigint not null,
        user_id bigint not null,
        primary key (role_id, user_id)
    );

    create table users (
        account_expired boolean,
        credentials_expired boolean,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        last_login timestamp(6),
        locked_until timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        email varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        metadata varchar(255),
        password varchar(255) not null,
        phone_number varchar(255),
        position varchar(255),
        refresh_token varchar(255),
        updated_by varchar(255),
        username varchar(255) not null unique,
        primary key (id)
    );

    alter table if exists blacklisted_tokens 
       add constraint FKt7v2sldreoy4pqla7y6kd7uu 
       foreign key (user_id) 
       references users;

    alter table if exists event_location 
       add constraint FKbn1d7fexc2sywojnk93i7vvye 
       foreign key (event_id) 
       references event;

    alter table if exists event_location 
       add constraint FKhclmyywn8a6ts46igc2et4re3 
       foreign key (region_id) 
       references region;

    alter table if exists event_provinces 
       add constraint FKq3o6xx4gjdhb6hw34blvuhy7g 
       foreign key (province_id) 
       references province;

    alter table if exists event_provinces 
       add constraint FKa6j8my1tiy5ob5wvnwucrjm6a 
       foreign key (event_id) 
       references event;

    alter table if exists golden_hours 
       add constraint FKd1lq9qcki1lngxlt30om69ep7 
       foreign key (event_location_id) 
       references event_location;

    alter table if exists participant_events 
       add constraint FKfqvcfcgot4i0e581y5gjc2iyn 
       foreign key (event_id) 
       references event;

    alter table if exists participant_events 
       add constraint FKbwn8ufullhdam9rbks8lw0hgl 
       foreign key (event_location_id) 
       references event_location;

    alter table if exists participant_events 
       add constraint FKjjitit5lfpkqyk3nvwel8xgf 
       foreign key (participant_id) 
       references participants;

    alter table if exists participant_events 
       add constraint FKrqh4au8kefrfcf4vm9idlqj0d 
       foreign key (province_id) 
       references province;

    alter table if exists participant_roles 
       add constraint FK70v5tkbsbn1o6xl1c22b4owtn 
       foreign key (role_id) 
       references roles;

    alter table if exists participant_roles 
       add constraint FK5ub8a1wn0n40afuju04u13qlh 
       foreign key (participant_id) 
       references participants;

    alter table if exists participants 
       add constraint FKigcjsqxpvlml58wc1ys0e95je 
       foreign key (province_id) 
       references province;

    alter table if exists participants 
       add constraint FKghixrahoj1s8cloinfx8lyeqa 
       foreign key (user_id) 
       references users;

    alter table if exists province 
       add constraint FKc7qs0yyib7q7aeqek906g81cn 
       foreign key (region_id) 
       references region;

    alter table if exists rewards 
       add constraint FKdwssrhmk120pnee3bd6fi9abq 
       foreign key (event_location_id) 
       references event_location;

    alter table if exists role_permissions 
       add constraint FKn5fotdgk8d1xvo8nav9uv3muc 
       foreign key (role_id) 
       references roles;

    alter table if exists spin_histories 
       add constraint FK3jjr3flpxvcdejf8gn4uqrfgu 
       foreign key (event_location_id) 
       references event_location;

    alter table if exists spin_histories 
       add constraint FKcje0w53l1u9ctyuxv36ufpmmu 
       foreign key (golden_hour_id) 
       references golden_hours;

    alter table if exists spin_histories 
       add constraint FKdsnjjkw28f8i55mpqn22q6j6i 
       foreign key (participant_event_id) 
       references participant_events;

    alter table if exists spin_histories 
       add constraint FKatkkxrtpf21fsnr98awyel68c 
       foreign key (reward_id) 
       references rewards;

    alter table if exists user_roles 
       add constraint FKh8ciramu9cc9q3qcqiv4ue8a6 
       foreign key (role_id) 
       references roles;

    alter table if exists user_roles 
       add constraint FKhfh9dx7w3ubf1co1vdev94g3f 
       foreign key (user_id) 
       references users;

    create table blacklisted_tokens (
        status integer not null,
        created_at timestamp(6),
        expiry_time timestamp(6) not null,
        id bigserial not null,
        updated_at timestamp(6),
        user_id bigint,
        version bigint not null,
        created_by varchar(255),
        metadata TEXT,
        reason varchar(255),
        token varchar(255) not null unique,
        token_type varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        end_time timestamp(6),
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_location (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        event_id bigint not null,
        id bigserial not null,
        region_id bigint not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null unique,
        name varchar(100) not null,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_provinces (
        event_id bigint not null,
        province_id bigint not null,
        primary key (event_id, province_id)
    );

    create table golden_hours (
        daily_limit integer,
        points_multiplier float(53),
        status integer not null,
        total_uses integer,
        win_probability float(53),
        win_probability_multiplier float(53),
        created_at timestamp(6),
        end_time timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_events (
        daily_spins_used integer,
        initial_spins integer,
        remaining_spins integer,
        status integer not null,
        total_points integer,
        total_spins integer,
        total_wins integer,
        created_at timestamp(6),
        event_id bigint not null,
        event_location_id bigint not null,
        id bigserial not null,
        participant_id bigint not null,
        province_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_roles (
        participant_id bigint not null,
        role_id bigint not null,
        primary key (participant_id, role_id)
    );

    create table participants (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        province_id bigint,
        updated_at timestamp(6),
        user_id bigint unique,
        version bigint not null,
        account varchar(255) not null unique,
        code varchar(255),
        created_by varchar(255),
        email varchar(255),
        metadata varchar(255),
        name varchar(255) not null,
        phone varchar(255) not null unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table province (
        default_win_probability float(53),
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        population bigint,
        region_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null,
        name varchar(100) not null,
        created_by varchar(255),
        metadata varchar(255),
        region_code varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table region (
        default_win_probability float(53),
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null unique,
        name varchar(100) not null,
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table rewards (
        daily_count integer,
        daily_limit integer,
        points integer,
        points_required integer,
        remaining_quantity integer,
        status integer not null,
        total_quantity integer,
        win_probability float(53),
        created_at timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        updated_at timestamp(6),
        valid_from timestamp(6),
        valid_until timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table role_permissions (
        role_id bigint not null,
        permission varchar(255) check (permission in ('VIEW_EVENTS','CREATE_EVENT','EDIT_EVENT','DELETE_EVENT','VIEW_PARTICIPANTS','CREATE_PARTICIPANT','EDIT_PARTICIPANT','DELETE_PARTICIPANT','VIEW_REWARDS','CREATE_REWARD','EDIT_REWARD','DELETE_REWARD','VIEW_USERS','CREATE_USER','EDIT_USER','DELETE_USER','VIEW_ROLES','CREATE_ROLE','EDIT_ROLE','DELETE_ROLE','MANAGE_SYSTEM','VIEW_REPORTS'))
    );

    create table roles (
        priority integer,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        name varchar(20) check (name in ('ADMIN','MANAGER','OPERATOR','USER','GUEST')),
        code varchar(50) unique,
        description varchar(200),
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table spin_histories (
        finalized boolean,
        points_earned integer,
        status integer not null,
        win boolean,
        created_at timestamp(6),
        event_location_id bigint,
        golden_hour_id bigint,
        id bigserial not null,
        participant_event_id bigint not null,
        reward_id bigint,
        spin_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table user_roles (
        role_id bigint not null,
        user_id bigint not null,
        primary key (role_id, user_id)
    );

    create table users (
        account_expired boolean,
        credentials_expired boolean,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        last_login timestamp(6),
        locked_until timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        email varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        metadata varchar(255),
        password varchar(255) not null,
        phone_number varchar(255),
        position varchar(255),
        refresh_token varchar(255),
        updated_by varchar(255),
        username varchar(255) not null unique,
        primary key (id)
    );

    alter table if exists blacklisted_tokens 
       add constraint FKt7v2sldreoy4pqla7y6kd7uu 
       foreign key (user_id) 
       references users;

    alter table if exists event_location 
       add constraint FKbn1d7fexc2sywojnk93i7vvye 
       foreign key (event_id) 
       references event;

    alter table if exists event_location 
       add constraint FKhclmyywn8a6ts46igc2et4re3 
       foreign key (region_id) 
       references region;

    alter table if exists event_provinces 
       add constraint FKq3o6xx4gjdhb6hw34blvuhy7g 
       foreign key (province_id) 
       references province;

    alter table if exists event_provinces 
       add constraint FKa6j8my1tiy5ob5wvnwucrjm6a 
       foreign key (event_id) 
       references event;

    alter table if exists golden_hours 
       add constraint FKd1lq9qcki1lngxlt30om69ep7 
       foreign key (event_location_id) 
       references event_location;

    alter table if exists participant_events 
       add constraint FKfqvcfcgot4i0e581y5gjc2iyn 
       foreign key (event_id) 
       references event;

    alter table if exists participant_events 
       add constraint FKbwn8ufullhdam9rbks8lw0hgl 
       foreign key (event_location_id) 
       references event_location;

    alter table if exists participant_events 
       add constraint FKjjitit5lfpkqyk3nvwel8xgf 
       foreign key (participant_id) 
       references participants;

    alter table if exists participant_events 
       add constraint FKrqh4au8kefrfcf4vm9idlqj0d 
       foreign key (province_id) 
       references province;

    alter table if exists participant_roles 
       add constraint FK70v5tkbsbn1o6xl1c22b4owtn 
       foreign key (role_id) 
       references roles;

    alter table if exists participant_roles 
       add constraint FK5ub8a1wn0n40afuju04u13qlh 
       foreign key (participant_id) 
       references participants;

    alter table if exists participants 
       add constraint FKigcjsqxpvlml58wc1ys0e95je 
       foreign key (province_id) 
       references province;

    alter table if exists participants 
       add constraint FKghixrahoj1s8cloinfx8lyeqa 
       foreign key (user_id) 
       references users;

    alter table if exists province 
       add constraint FKc7qs0yyib7q7aeqek906g81cn 
       foreign key (region_id) 
       references region;

    alter table if exists rewards 
       add constraint FKdwssrhmk120pnee3bd6fi9abq 
       foreign key (event_location_id) 
       references event_location;

    alter table if exists role_permissions 
       add constraint FKn5fotdgk8d1xvo8nav9uv3muc 
       foreign key (role_id) 
       references roles;

    alter table if exists spin_histories 
       add constraint FK3jjr3flpxvcdejf8gn4uqrfgu 
       foreign key (event_location_id) 
       references event_location;

    alter table if exists spin_histories 
       add constraint FKcje0w53l1u9ctyuxv36ufpmmu 
       foreign key (golden_hour_id) 
       references golden_hours;

    alter table if exists spin_histories 
       add constraint FKdsnjjkw28f8i55mpqn22q6j6i 
       foreign key (participant_event_id) 
       references participant_events;

    alter table if exists spin_histories 
       add constraint FKatkkxrtpf21fsnr98awyel68c 
       foreign key (reward_id) 
       references rewards;

    alter table if exists user_roles 
       add constraint FKh8ciramu9cc9q3qcqiv4ue8a6 
       foreign key (role_id) 
       references roles;

    alter table if exists user_roles 
       add constraint FKhfh9dx7w3ubf1co1vdev94g3f 
       foreign key (user_id) 
       references users;

    create table public.event_locations (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        event_id bigint not null,
        id bigserial not null,
        region_id bigint not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null unique,
        name varchar(100) not null,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table public.events (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        end_time timestamp(6),
        id bigserial not null,
        remaining_spins bigint,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table public.regions (
        default_win_probability float(53),
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null unique,
        name varchar(100) not null,
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table blacklisted_tokens (
        status integer not null,
        created_at timestamp(6),
        expiry_time timestamp(6) not null,
        id bigserial not null,
        updated_at timestamp(6),
        user_id bigint,
        version bigint not null,
        created_by varchar(255),
        metadata TEXT,
        reason varchar(255),
        token varchar(255) not null unique,
        token_type varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_provinces (
        event_id bigint not null,
        province_id bigint not null,
        primary key (event_id, province_id)
    );

    create table golden_hours (
        daily_limit integer,
        points_multiplier float(53),
        status integer not null,
        total_uses integer,
        win_probability float(53),
        win_probability_multiplier float(53),
        created_at timestamp(6),
        end_time timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_events (
        daily_spins_used integer,
        initial_spins integer,
        remaining_spins integer,
        status integer not null,
        total_points integer,
        total_spins integer,
        total_wins integer,
        created_at timestamp(6),
        event_id bigint not null,
        event_location_id bigint not null,
        id bigserial not null,
        participant_id bigint not null,
        province_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_roles (
        participant_id bigint not null,
        role_id bigint not null,
        primary key (participant_id, role_id)
    );

    create table participants (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        province_id bigint,
        updated_at timestamp(6),
        user_id bigint unique,
        version bigint not null,
        account varchar(255) not null unique,
        code varchar(255),
        created_by varchar(255),
        email varchar(255),
        metadata varchar(255),
        name varchar(255) not null,
        phone varchar(255) not null unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table province (
        default_win_probability float(53),
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        population bigint,
        region_id bigint,
        updated_at timestamp(6),
        version bigint not null,
        code varchar(20) not null,
        name varchar(100) not null,
        created_by varchar(255),
        metadata varchar(255),
        region_code varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table rewards (
        daily_count integer,
        daily_limit integer,
        points integer,
        points_required integer,
        remaining_quantity integer,
        status integer not null,
        total_quantity integer,
        win_probability float(53),
        created_at timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        updated_at timestamp(6),
        valid_from timestamp(6),
        valid_until timestamp(6),
        version bigint not null,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table role_permissions (
        role_id bigint not null,
        permission varchar(255) check (permission in ('VIEW_EVENTS','CREATE_EVENT','EDIT_EVENT','DELETE_EVENT','VIEW_PARTICIPANTS','CREATE_PARTICIPANT','EDIT_PARTICIPANT','DELETE_PARTICIPANT','VIEW_REWARDS','CREATE_REWARD','EDIT_REWARD','DELETE_REWARD','VIEW_USERS','CREATE_USER','EDIT_USER','DELETE_USER','VIEW_ROLES','CREATE_ROLE','EDIT_ROLE','DELETE_ROLE','MANAGE_SYSTEM','VIEW_REPORTS'))
    );

    create table roles (
        priority integer,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint not null,
        name varchar(20) check (name in ('ADMIN','MANAGER','OPERATOR','USER','GUEST')),
        code varchar(50) unique,
        description varchar(200),
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table spin_histories (
        finalized boolean,
        points_earned integer,
        status integer not null,
        win boolean,
        created_at timestamp(6),
        event_location_id bigint,
        golden_hour_id bigint,
        id bigserial not null,
        participant_event_id bigint not null,
        reward_id bigint,
        spin_time timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table user_roles (
        role_id bigint not null,
        user_id bigint not null,
        primary key (role_id, user_id)
    );

    create table users (
        account_expired boolean,
        credentials_expired boolean,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        last_login timestamp(6),
        locked_until timestamp(6),
        updated_at timestamp(6),
        version bigint not null,
        created_by varchar(255),
        email varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        metadata varchar(255),
        password varchar(255) not null,
        phone_number varchar(255),
        position varchar(255),
        refresh_token varchar(255),
        updated_by varchar(255),
        username varchar(255) not null unique,
        primary key (id)
    );

    alter table if exists public.event_locations 
       add constraint FKdquu070vft3scg2w79xy9secn 
       foreign key (event_id) 
       references public.events;

    alter table if exists public.event_locations 
       add constraint FKs41txxkfquf582772ahkwlub2 
       foreign key (region_id) 
       references public.regions;

    alter table if exists blacklisted_tokens 
       add constraint FKt7v2sldreoy4pqla7y6kd7uu 
       foreign key (user_id) 
       references users;

    alter table if exists event_provinces 
       add constraint FKq3o6xx4gjdhb6hw34blvuhy7g 
       foreign key (province_id) 
       references province;

    alter table if exists event_provinces 
       add constraint FKhb3bbux1ipjav2d3wap0o1n6x 
       foreign key (event_id) 
       references public.events;

    alter table if exists golden_hours 
       add constraint FKiextogf8tkm1q3gu9vblyswnq 
       foreign key (event_location_id) 
       references public.event_locations;

    alter table if exists participant_events 
       add constraint FKsodxjcwjkui9kprbjnq6687h6 
       foreign key (event_id) 
       references public.events;

    alter table if exists participant_events 
       add constraint FKd015ah4sdlr6ry5rx5nvnu6qa 
       foreign key (event_location_id) 
       references public.event_locations;

    alter table if exists participant_events 
       add constraint FKjjitit5lfpkqyk3nvwel8xgf 
       foreign key (participant_id) 
       references participants;

    alter table if exists participant_events 
       add constraint FKrqh4au8kefrfcf4vm9idlqj0d 
       foreign key (province_id) 
       references province;

    alter table if exists participant_roles 
       add constraint FK70v5tkbsbn1o6xl1c22b4owtn 
       foreign key (role_id) 
       references roles;

    alter table if exists participant_roles 
       add constraint FK5ub8a1wn0n40afuju04u13qlh 
       foreign key (participant_id) 
       references participants;

    alter table if exists participants 
       add constraint FKigcjsqxpvlml58wc1ys0e95je 
       foreign key (province_id) 
       references province;

    alter table if exists participants 
       add constraint FKghixrahoj1s8cloinfx8lyeqa 
       foreign key (user_id) 
       references users;

    alter table if exists province 
       add constraint FKiaxdcchv9utxmtjlexyn2jmf 
       foreign key (region_id) 
       references public.regions;

    alter table if exists rewards 
       add constraint FK465nimj9nfk08cul0s94bu79n 
       foreign key (event_location_id) 
       references public.event_locations;

    alter table if exists role_permissions 
       add constraint FKn5fotdgk8d1xvo8nav9uv3muc 
       foreign key (role_id) 
       references roles;

    alter table if exists spin_histories 
       add constraint FKn5ghsrn4a8u3tve4lpbat9cf1 
       foreign key (event_location_id) 
       references public.event_locations;

    alter table if exists spin_histories 
       add constraint FKcje0w53l1u9ctyuxv36ufpmmu 
       foreign key (golden_hour_id) 
       references golden_hours;

    alter table if exists spin_histories 
       add constraint FKdsnjjkw28f8i55mpqn22q6j6i 
       foreign key (participant_event_id) 
       references participant_events;

    alter table if exists spin_histories 
       add constraint FKatkkxrtpf21fsnr98awyel68c 
       foreign key (reward_id) 
       references rewards;

    alter table if exists user_roles 
       add constraint FKh8ciramu9cc9q3qcqiv4ue8a6 
       foreign key (role_id) 
       references roles;

    alter table if exists user_roles 
       add constraint FKhfh9dx7w3ubf1co1vdev94g3f 
       foreign key (user_id) 
       references users;
