/*
 *
 * Created by: George Papasotiriou
 * Date: 2024-01-15
 *
 * Configures security settings, JWT authentication, and authorization rules
 */

package com.library.Controller;

import com.library.Model.Book;
import com.library.Model.User;
import com.library.Service.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main controller for our Library API.
 * Contains 4+ endpoints with authentication and authorization.
 * Each endpoint returns clear success or error messages.
 */
@RestController
@RequestMapping("/api")
public class MyACGLibraryController {
    
    @Autowired
    private LibraryService libraryService;
    
    /**
     * ENDPOINT 1: Get all books
     * GET /api/books
     * Requires: USER or ADMIN role
     */
    @GetMapping("/books")
    public ResponseEntity<?> getAllBooks() {
        try {
            List<Book> books = libraryService.getAllBooks();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Found " + books.size() + " books");
            response.put("books", books);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse("Failed to get books: " + e.getMessage());
        }
    }
    
    /**
     * ENDPOINT 2: Add a new book (Admin only)
     * POST /api/books
     * Requires: ADMIN role
     * Body: {"title": "Book Title", "author": "Author Name", "isbn": "1234567890"}
     */
    @PostMapping("/books")
    public ResponseEntity<?> addBook(@RequestBody Map<String, String> bookData) {
        try {
            // Check if user is admin
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User currentUser = libraryService.getUserByUsername(username);
            
            if (!currentUser.isAdmin()) {
                return createErrorResponse("Only administrators can add books", HttpStatus.FORBIDDEN);
            }
            
            // Get book details from request
            String title = bookData.get("title");
            String author = bookData.get("author");
            String isbn = bookData.get("isbn");
            
            // Check if required fields are provided
            if (title == null || author == null || isbn == null) {
                return createErrorResponse("Missing required fields: title, author, or isbn");
            }
            
            // Create and save the book
            Book newBook = new Book(title, author, isbn);
            libraryService.saveBook(newBook);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Book added successfully");
            response.put("book", newBook);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return createErrorResponse("Failed to add book: " + e.getMessage());
        }
    }
    
    /**
     * ENDPOINT 3: Borrow a book
     * POST /api/borrow/{bookId}
     * Requires: USER or ADMIN role
     * Path variable: bookId (the ID of the book to borrow)
     */
    @PostMapping("/borrow/{bookId}")
    public ResponseEntity<?> borrowBook(@PathVariable String bookId) {
        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = libraryService.getUserByUsername(username);
            
            // Try to parse book ID (vulnerable to type confusion!)
            Long id = Long.parseLong(bookId);
            
            // Check if book exists and is available
            Book book = libraryService.getBookById(id);
            if (book == null) {
                return createErrorResponse("Book not found with ID: " + bookId);
            }
            
            if (!book.isAvailable()) {
                return createErrorResponse("Book is already borrowed");
            }
            
            // Borrow the book
            libraryService.borrowBook(book, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Book borrowed successfully");
            response.put("book", book.getTitle());
            response.put("dueDate", "14 days from now");
            
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            // This is a vulnerability - we expose error details
            return createErrorResponse("Invalid book ID format: " + bookId);
        } catch (Exception e) {
            return createErrorResponse("Failed to borrow book: " + e.getMessage());
        }
    }
    
    /**
     * ENDPOINT 4: Return a book
     * POST /api/return/{bookId}
     * Requires: USER or ADMIN role
     */
    @PostMapping("/return/{bookId}")
    public ResponseEntity<?> returnBook(@PathVariable String bookId) {
        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = libraryService.getUserByUsername(username);
            
            Long id = Long.parseLong(bookId);
            
            // Return the book
            boolean success = libraryService.returnBook(id, user);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Book returned successfully");
                return ResponseEntity.ok(response);
            } else {
                return createErrorResponse("You haven't borrowed this book");
            }
        } catch (Exception e) {
            return createErrorResponse("Failed to return book: " + e.getMessage());
        }
    }
    
    /**
     * ENDPOINT 5: Get user's borrowed books
     * GET /api/user/books
     * Requires: USER or ADMIN role
     */
    @GetMapping("/user/books")
    public ResponseEntity<?> getMyBooks() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = libraryService.getUserByUsername(username);
            
            List<Book> borrowedBooks = libraryService.getBorrowedBooks(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "You have " + borrowedBooks.size() + " borrowed books");
            response.put("books", borrowedBooks);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse("Failed to get your books: " + e.getMessage());
        }
    }
    
    /**
     * ENDPOINT 6: Search books by title (Vulnerable endpoint!)
     * GET /api/search
     * Requires: USER or ADMIN role
     * Query parameter: q (search query)
     * INTENTIONAL VULNERABILITY: SQL injection for testing!
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchBooks(@RequestParam("q") String query) {
        try {
            // VULNERABLE: Direct string concatenation for SQL!
            List<Book> books = libraryService.searchBooksUnsafe(query);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Found " + books.size() + " books");
            response.put("books", books);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse("Search failed: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to create error responses
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message) {
        return createErrorResponse(message, HttpStatus.BAD_REQUEST);
    }
    
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        error.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(status).body(error);
    }
}