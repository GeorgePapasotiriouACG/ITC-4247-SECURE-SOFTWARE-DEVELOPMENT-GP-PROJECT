/*
 *
 * Created by: George Papasotiriou
 * Date: 2024-01-15
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
     * NEW ENDPOINT: Update a book (Admin only)
     * PUT /api/books/{id}
     * Requires: ADMIN role
     * Body: {"title": "Updated Title", "author": "Updated Author", "isbn": "updated-isbn", "available": true/false}
     */
    @PutMapping("/books/{id}")
    public ResponseEntity<?> updateBook(@PathVariable String id, @RequestBody Map<String, Object> bookData) {
        try {
            // Check if user is admin
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User currentUser = libraryService.getUserByUsername(username);

            if (!currentUser.isAdmin()) {
                return createErrorResponse("Only administrators can update books", HttpStatus.FORBIDDEN);
            }

            // Try to parse book ID
            Long bookId;
            try {
                bookId = Long.parseLong(id);
            } catch (NumberFormatException e) {
                return createErrorResponse("Invalid book ID format: " + id);
            }

            // Find the book
            Book book = libraryService.getBookById(bookId);
            if (book == null) {
                return createErrorResponse("Book not found with ID: " + bookId);
            }

            // Update book fields if provided
            if (bookData.containsKey("title")) {
                book.setTitle((String) bookData.get("title"));
            }
            if (bookData.containsKey("author")) {
                book.setAuthor((String) bookData.get("author"));
            }
            if (bookData.containsKey("isbn")) {
                book.setIsbn((String) bookData.get("isbn"));
            }
            if (bookData.containsKey("available")) {
                Object available = bookData.get("available");
                if (available instanceof Boolean) {
                    book.setAvailable((Boolean) available);
                } else if (available instanceof String) {
                    book.setAvailable(Boolean.parseBoolean((String) available));
                }
            }

            // Save updated book
            libraryService.saveBook(book);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Book updated successfully");
            response.put("book", book);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse("Failed to update book: " + e.getMessage());
        }
    }

    /**
     * NEW ENDPOINT: Delete a book (Admin only)
     * DELETE /api/books/{id}
     * Requires: ADMIN role
     */
    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable String id) {
        try {
            // Check if user is admin
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User currentUser = libraryService.getUserByUsername(username);

            if (!currentUser.isAdmin()) {
                return createErrorResponse("Only administrators can delete books", HttpStatus.FORBIDDEN);
            }

            // Try to parse book ID
            Long bookId;
            try {
                bookId = Long.parseLong(id);
            } catch (NumberFormatException e) {
                return createErrorResponse("Invalid book ID format: " + id);
            }

            // Find the book
            Book book = libraryService.getBookById(bookId);
            if (book == null) {
                return createErrorResponse("Book not found with ID: " + bookId);
            }

            // Check if book is currently borrowed
            if (!book.isAvailable()) {
                return createErrorResponse("Cannot delete a book that is currently borrowed");
            }

            // Delete the book
            boolean deleted = libraryService.deleteBook(bookId);

            if (deleted) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Book deleted successfully");
                response.put("bookId", bookId);
                return ResponseEntity.ok(response);
            } else {
                return createErrorResponse("Failed to delete book");
            }
        } catch (Exception e) {
            return createErrorResponse("Failed to delete book: " + e.getMessage());
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