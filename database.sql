DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS users;

DROP TYPE IF EXISTS event_category_type;
DROP TYPE IF EXISTS ticket_status_type;
DROP TYPE IF EXISTS user_role_type;

CREATE TYPE event_category_type AS ENUM ('Music', 'Technology', 'Food', 'Entertainment', 'Sports', 'Art', 'Business', 'Education');
CREATE TYPE ticket_status_type AS ENUM ('confirmed', 'cancelled', 'refunded', 'pending');
CREATE TYPE user_role_type AS ENUM ('user', 'admin');

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role user_role_type DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE events (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    location VARCHAR(255) NOT NULL,
    description TEXT,
    long_description TEXT,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    category event_category_type NOT NULL,
    image VARCHAR(255),
    organizer VARCHAR(255),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    available_tickets INTEGER NOT NULL CHECK (available_tickets >= 0),
    total_tickets INTEGER NOT NULL CHECK (total_tickets > 0),
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    is_finished BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_tickets_valid CHECK (available_tickets <= total_tickets),
    CONSTRAINT check_time_valid CHECK (end_time > start_time)
);

CREATE TABLE tickets (
    id VARCHAR(50) PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price_per_ticket DECIMAL(10, 2) NOT NULL CHECK (price_per_ticket >= 0),
    total_price DECIMAL(10, 2) NOT NULL CHECK (total_price >= 0),
    status ticket_status_type DEFAULT 'confirmed',
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    qr_code_url VARCHAR(255),
    email_sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_total_price CHECK (total_price = price_per_ticket * quantity)
);

CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, event_id)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_tickets_user_id ON tickets(user_id);
CREATE INDEX idx_tickets_event_id ON tickets(event_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_events_category ON events(category);
CREATE INDEX idx_events_date ON events(date);
CREATE INDEX idx_events_is_finished ON events(is_finished);
CREATE INDEX idx_reviews_event_id ON reviews(event_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);


INSERT INTO events (title, date, location, description, long_description, price, category, image, organizer, start_time, end_time, available_tickets, total_tickets) VALUES
('Summer Music Festival', '2025-06-21', 'Central Park, NYC', 'A full-day event with live performances by top artists.', 'Join us for an incredible day of music and fun at Central Park! The Summer Music Festival brings together the best artists from around the world for a day you won''t forget. Enjoy food vendors, art installations, and of course, amazing musical performances across three stages. Early arrival is recommended as space is limited.', 49.99, 'Music', '/api/placeholder/800/400', 'NYC Events Co.', '11:00:00', '22:00:00', 3, 3),

('Tech Innovators Conference', '2025-07-10', 'San Francisco, CA', 'Meet industry leaders and explore the future of tech.', 'The Tech Innovators Conference is where the brightest minds in technology come together to share ideas and shape the future. This year''s conference features keynote speakers from leading tech companies, hands-on workshops, networking opportunities, and exhibits showcasing cutting-edge innovations. Perfect for professionals, entrepreneurs, and tech enthusiasts.', 199.00, 'Technology', '/api/placeholder/800/400', 'Tech Forward', '09:00:00', '18:00:00', 200, 200),

('Food & Wine Expo', '2025-08-05', 'Chicago, IL', 'Taste gourmet dishes and world-class wines.', 'Indulge your senses at the Food & Wine Expo, Chicago''s premier culinary event. Sample exquisite dishes prepared by renowned chefs, discover rare and delicious wines from around the world, and learn cooking techniques from expert demonstrations. The expo features over 100 vendors, cooking competitions, and exclusive tasting sessions.', 29.00, 'Food', '/api/placeholder/800/400', 'Taste of America', '12:00:00', '20:00:00', 300, 300),

('Annual Comic Convention', '2025-09-15', 'Los Angeles, CA', 'Celebrate comics, movies, and pop culture with fellow fans.', 'The Annual Comic Convention brings together fans, creators, and stars for a celebration of all things pop culture! Meet your favorite comic artists, attend celebrity panels, show off your best cosplay, and shop for exclusive merchandise. This year''s convention will feature special guests from blockbuster superhero movies, anime voice actors, and legendary comic creators.', 45.00, 'Entertainment', '/api/placeholder/800/400', 'Fandom Events', '10:00:00', '19:00:00', 1000, 1000),

('Marathon City Run', '2025-10-10', 'Boston, MA', 'Join thousands of runners in this exciting city marathon.', 'Challenge yourself at the Marathon City Run, a 26.2-mile journey through the historic streets of Boston. This USATF-certified course takes runners past iconic landmarks and through beautiful neighborhoods, with cheering spectators lining the route. Registration includes a race kit, finisher medal, and access to the post-race celebration with live music and refreshments.', 75.00, 'Sports', '/api/placeholder/800/400', 'Boston Athletics Association', '07:00:00', '14:00:00', 10000, 10000);