--liquibase formatted sql
--logicalFilePath: db/changelog/db.changelog-master.sql

--changeset events:001-create-pgcrypto-extension
CREATE EXTENSION IF NOT EXISTS pgcrypto;

--changeset events:002-create-enum-types
CREATE TYPE event_category_type AS ENUM ('Music', 'Technology', 'Food', 'Entertainment', 'Sports', 'Art', 'Business', 'Education');
CREATE TYPE ticket_status_type AS ENUM ('confirmed', 'cancelled', 'refunded', 'pending');
CREATE TYPE user_role_type AS ENUM ('user', 'admin');

--changeset events:003-create-users-table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    role user_role_type DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--changeset events:004-create-events-table
CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    location VARCHAR(255) NOT NULL,
    description TEXT,
    long_description TEXT,
    category event_category_type NOT NULL,
    image VARCHAR(255),
    organizer VARCHAR(255),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_finished BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_time_valid CHECK (end_time > start_time)
);

--changeset events:005-create-sections-table
CREATE TABLE sections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    rows_count INTEGER NOT NULL CHECK (rows_count > 0),
    cols_count INTEGER NOT NULL CHECK (cols_count > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--changeset events:006-create-tickets-table
CREATE TABLE tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    section_id UUID NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
    total_price DECIMAL(10, 2) NOT NULL CHECK (total_price >= 0),
    status ticket_status_type DEFAULT 'confirmed',
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    qr_code_url VARCHAR(255),
    email_sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--changeset events:008-create-seats-table
CREATE TABLE seats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    section_id UUID NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    row_label VARCHAR(5) NOT NULL,
    seat_number INTEGER NOT NULL CHECK (seat_number > 0),
    is_available BOOLEAN DEFAULT TRUE,
    ticket_id UUID REFERENCES tickets(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--changeset events:009-create-indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_tickets_user_id ON tickets(user_id);
CREATE INDEX idx_tickets_event_id ON tickets(event_id);
CREATE INDEX idx_tickets_section_id ON tickets(section_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_events_category ON events(category);
CREATE INDEX idx_events_date ON events(date);
CREATE INDEX idx_events_is_finished ON events(is_finished);
CREATE INDEX idx_sections_event_id ON sections(event_id);
CREATE INDEX idx_seats_section_id ON seats(section_id);
CREATE INDEX idx_seats_event_id ON seats(event_id);
CREATE INDEX idx_seats_availability ON seats(event_id, is_available);
CREATE INDEX idx_seats_ticket_id ON seats(ticket_id);

--changeset events:012-add-seat-reservations
ALTER TABLE seats
    ADD COLUMN reserved_by UUID REFERENCES users(id) ON DELETE SET NULL,
    ADD COLUMN reservation_expires_at TIMESTAMP,
    ADD COLUMN reservation_payment_intent_id VARCHAR(255);

CREATE INDEX idx_seats_reservation_expires_at ON seats(reservation_expires_at);
CREATE INDEX idx_seats_reservation_payment_intent_id ON seats(reservation_payment_intent_id);

--changeset events:013-create-reservations-table
CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    expires_at TIMESTAMP NOT NULL,
    payment_intent_id VARCHAR(255) UNIQUE,
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE seats
    ADD COLUMN reservation_id UUID REFERENCES reservations(id) ON DELETE SET NULL;

CREATE INDEX idx_reservations_user_id ON reservations(user_id);
CREATE INDEX idx_reservations_event_id ON reservations(event_id);
CREATE INDEX idx_reservations_status_expires_at ON reservations(status, expires_at);
CREATE INDEX idx_reservations_payment_intent_id ON reservations(payment_intent_id);
CREATE INDEX idx_seats_reservation_id ON seats(reservation_id);

CREATE TRIGGER update_reservations_updated_at BEFORE UPDATE ON reservations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

--changeset events:010-create-updated-at-function splitStatements:false
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

--changeset events:011-create-updated-at-triggers
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_events_updated_at BEFORE UPDATE ON events
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_tickets_updated_at BEFORE UPDATE ON tickets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_sections_updated_at BEFORE UPDATE ON sections
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
