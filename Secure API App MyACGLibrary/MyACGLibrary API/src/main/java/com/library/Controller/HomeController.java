/*
 * HomeController - Basic API endpoints with Database Status
 * Created by: George Papasotiriou
 * Date: 12/11/2025
 */

package com.library.Controller;

import com.library.Model.Book;
import com.library.Model.User;
import com.library.Repository.BookRepository;
import com.library.Repository.BorrowRecordRepository;
import com.library.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Library Management System API");
        response.put("version", "1.0.0");
        response.put("createdBy", "George Papasotiriou");
        response.put("status", "running");
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoints", new String[] {
                "GET  /                     - API Home (this page)",
                "GET  /api/test             - Basic API test",
                "GET  /api/status           - API status",
                "GET  /api/database-status  - Database connection status",
                "POST /api/auth/register    - Register new user",
                "POST /api/auth/login       - Login and get JWT token",
                "GET  /api/books            - Get all books (requires token)",
                "POST /api/books            - Add new book (admin only)",
                "PUT  /api/books/{id}       - Update a book (admin only)",
                "DELETE /api/books/{id}     - Delete a book (admin only)",
                "POST /api/borrow/{id}      - Borrow a book",
                "POST /api/return/{id}      - Return a book",
                "GET  /api/user/books       - Get your borrowed books",
                "GET  /api/search?q=query   - Search books (⚠️ has SQLi)"
        });
        return response;
    }

    @GetMapping("/api/test")
    public Map<String, Object> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "API is working correctly!");
        response.put("timestamp", System.currentTimeMillis());
        response.put("databaseConnected", true);
        return response;
    }

    @GetMapping("/api/status")
    public Map<String, Object> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Library API");
        response.put("status", "UP");
        response.put("database", "H2 File-based");
        response.put("authentication", "JWT");
        response.put("timestamp", LocalDateTime.now());
        response.put("dataPath", "./data/librarydb.mv.db");
        return response;
    }

    @GetMapping("/api/database-status")
    public Map<String, Object> databaseStatus() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get database statistics
            long userCount = userRepository.count();
            long bookCount = bookRepository.count();
            long borrowCount = borrowRecordRepository.count();

            // Get available vs borrowed books
            long availableBooks = bookRepository.findAll().stream()
                    .filter(Book::isAvailable)
                    .count();
            long borrowedBooks = bookCount - availableBooks;

            response.put("status", "CONNECTED");
            response.put("database", "H2 File-based");
            response.put("file", "./data/librarydb.mv.db");
            response.put("h2Console", "http://localhost:8080/h2-console");
            response.put("timestamp", LocalDateTime.now());

            // Database statistics
            response.put("statistics", Map.of(
                    "total_users", userCount,
                    "total_books", bookCount,
                    "total_borrow_records", borrowCount,
                    "available_books", availableBooks,
                    "borrowed_books", borrowedBooks
            ));

            // Get some sample data
            List<User> users = userRepository.findAll();
            List<Book> books = bookRepository.findAll();

            // Sample users
            List<Map<String, Object>> sampleUsers = users.stream()
                    .map(u -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("id", u.getId());
                        userMap.put("username", u.getUsername());
                        userMap.put("role", u.getRole());
                        userMap.put("active", u.isActive());
                        userMap.put("created", u.getCreatedAt());
                        return userMap;
                    })
                    .collect(Collectors.toList());

            response.put("users", sampleUsers);

            // Sample books (limited to 5)
            List<Map<String, Object>> sampleBooks = books.stream()
                    .limit(5)
                    .map(b -> {
                        Map<String, Object> bookMap = new HashMap<>();
                        bookMap.put("id", b.getId());
                        bookMap.put("title", b.getTitle());
                        bookMap.put("author", b.getAuthor());
                        bookMap.put("isbn", b.getIsbn());
                        bookMap.put("available", b.isAvailable());
                        bookMap.put("created", b.getCreatedAt());
                        return bookMap;
                    })
                    .collect(Collectors.toList());

            response.put("sample_books", sampleBooks);

            // Database health check
            try {
                // Try to execute a simple query
                userRepository.findByUsername("admin");
                response.put("health", "HEALTHY");
                response.put("message", "Database is properly connected and responding");
            } catch (Exception e) {
                response.put("health", "WARNING");
                response.put("message", "Database connected but query test failed: " + e.getMessage());
            }

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", "Database connection failed: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            response.put("recommendation", "Check if H2 database is properly initialized in ./data/ directory");
        }

        return response;
    }
}
