/*
 * DatabaseInitializer - Ensures database is properly initialized
 * Created by: George Papasotiriou
 * Date: 12/11/2025
 *
 * This service ensures the database has initial data
 * but doesn't reset it on every startup
 */

package com.library.Service;

import com.library.Model.Book;
import com.library.Model.User;
import com.library.Repository.BookRepository;
import com.library.Repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Logger;

@Service
public class DatabaseInitializer {

    private static final Logger logger = Logger.getLogger(DatabaseInitializer.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void initializeDatabase() {
        try {
            // Check if users exist
            long userCount = userRepository.count();
            long bookCount = bookRepository.count();

            if (userCount == 0) {
                logger.info("Initializing database with sample data...");
                createInitialUsers();
            }

            if (bookCount == 0) {
                createInitialBooks();
            }

            logger.info("Database check complete. Users: " + userCount + ", Books: " + bookCount);

        } catch (Exception e) {
            logger.warning("Database initialization error: " + e.getMessage());
        }
    }

    private void createInitialUsers() {
        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);

        // Create regular users
        String[] users = {"alice", "bob", "charlie", "diana"};
        for (String username : users) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole("ROLE_USER");
            userRepository.save(user);
        }

        logger.info("Created initial users");
    }

    private void createInitialBooks() {
        // Sample books
        String[][] books = {
                {"The Great Gatsby", "F. Scott Fitzgerald", "9780743273565"},
                {"To Kill a Mockingbird", "Harper Lee", "9780061120084"},
                {"1984", "George Orwell", "9780451524935"},
                {"Pride and Prejudice", "Jane Austen", "9780141439518"},
                {"The Hobbit", "J.R.R. Tolkien", "9780547928227"},
                {"Harry Potter and the Philosopher's Stone", "J.K. Rowling", "9780747532743"},
                {"The Catcher in the Rye", "J.D. Salinger", "9780316769488"},
                {"The Lord of the Rings", "J.R.R. Tolkien", "9780544003415"},
                {"Brave New World", "Aldous Huxley", "9780060850524"},
                {"The Da Vinci Code", "Dan Brown", "9780307474278"}
        };

        for (String[] bookData : books) {
            Book book = new Book(bookData[0], bookData[1], bookData[2]);
            bookRepository.save(book);
        }

        logger.info("Created initial books");
    }
}
