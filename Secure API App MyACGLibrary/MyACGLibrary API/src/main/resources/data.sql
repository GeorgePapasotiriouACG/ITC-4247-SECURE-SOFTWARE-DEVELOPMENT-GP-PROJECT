-- ===========================================
-- Sample Data for Library Management System
-- Created by: George Papasotiriou
-- Date: 12/11/2025
-- ===========================================
-- Insert users with BCrypt encoded passwords
-- BCrypt hash for 'password123': $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKsUi
INSERT INTO users (username, password, role) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKsUi', 'ROLE_ADMIN'),
('alice', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKsUi', 'ROLE_USER'),
('bob', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKsUi', 'ROLE_USER'),
('charlie', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKsUi', 'ROLE_USER');

-- Insert sample books
INSERT INTO books (title, author, isbn, available) VALUES
('The Great Gatsby', 'F. Scott Fitzgerald', '9780743273565', true),
('To Kill a Mockingbird', 'Harper Lee', '9780061120084', true),
('1984', 'George Orwell', '9780451524935', true),
('Pride and Prejudice', 'Jane Austen', '9780141439518', true),
('The Hobbit', 'J.R.R. Tolkien', '9780547928227', true),
('Harry Potter and the Philosopher''s Stone', 'J.K. Rowling', '9780747532743', true),
('The Catcher in the Rye', 'J.D. Salinger', '9780316769488', true),
('The Lord of the Rings', 'J.R.R. Tolkien', '9780544003415', true);

-- Insert some borrow records (for testing)
INSERT INTO borrow_records (book_id, user_id, borrow_date, return_date) VALUES
(1, 2, DATEADD('DAY', -10, CURRENT_DATE), NULL),  -- Alice borrowed The Great Gatsby 10 days ago
(3, 3, DATEADD('DAY', -5, CURRENT_DATE), NULL);   -- Bob borrowed 1984 5 days ago

-- Update book availability for borrowed books
UPDATE books SET available = false WHERE id IN (1, 3);
