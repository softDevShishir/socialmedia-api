-- Sample test users (optional - for development)
-- All accounts below use the password: password123
INSERT INTO users (email, password, username, first_name, last_name, bio, role, follower_count, following_count, created_at, updated_at)
VALUES
  ('john@example.com', '$2a$10$5MI3mg7SejPruq3/ULjWU.JDuQCfAw2K/ZRSXd3CFK9nQPTO47SLa', 'john_doe', 'John', 'Doe', 'Tech enthusiast', 'USER', 120, 85, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('jane@example.com', '$2a$10$5MI3mg7SejPruq3/ULjWU.JDuQCfAw2K/ZRSXd3CFK9nQPTO47SLa', 'jane_smith', 'Jane', 'Smith', 'Photographer and traveler', 'USER', 340, 210, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('alex@example.com', '$2a$10$5MI3mg7SejPruq3/ULjWU.JDuQCfAw2K/ZRSXd3CFK9nQPTO47SLa', 'alex_wong', 'Alex', 'Wong', 'Building things with code', 'USER', 15, 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('maria@example.com', '$2a$10$5MI3mg7SejPruq3/ULjWU.JDuQCfAw2K/ZRSXd3CFK9nQPTO47SLa', 'maria_garcia', 'Maria', 'Garcia', 'Coffee, books, and long walks', 'USER', 502, 340, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('sam@example.com', '$2a$10$5MI3mg7SejPruq3/ULjWU.JDuQCfAw2K/ZRSXd3CFK9nQPTO47SLa', 'sam_lee', 'Sam', 'Lee', NULL, 'USER', 8, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('admin@example.com', '$2a$10$5MI3mg7SejPruq3/ULjWU.JDuQCfAw2K/ZRSXd3CFK9nQPTO47SLa', 'admin', 'Site', 'Admin', 'Platform administrator', 'ADMIN', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;
