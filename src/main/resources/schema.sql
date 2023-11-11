CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS announcements (
    id text DEFAULT uuid_generate_v4()::text PRIMARY KEY,
    created_at timestamp DEFAULT now() NOT NULL,
    venue_id text NOT NULL,
    message text NOT NULL
);
CREATE INDEX IF NOT EXISTS announcements_venue_id_idx ON announcements (venue_id, created_at);

CREATE TABLE IF NOT EXISTS subscriptions (
    id text DEFAULT uuid_generate_v4()::text PRIMARY KEY,
    created_at timestamp DEFAULT now() NOT NULL,
    processed_at timestamp DEFAULT now() NOT NULL,
    email text NOT NULL,
    venue_id text NOT NULL,
    UNIQUE (email, venue_id)
);
CREATE INDEX IF NOT EXISTS subscriptions_processed_at_idx ON subscriptions (processed_at);

CREATE TABLE IF NOT EXISTS _shedlock (
    name VARCHAR(64) NOT NULL PRIMARY KEY,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL
);
