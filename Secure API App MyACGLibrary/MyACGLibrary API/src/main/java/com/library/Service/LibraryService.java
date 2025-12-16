/*
 *
 * Created by: George Papasotiriou
 * Date: 12/11/2025
 *
 * Handles user authentication, registration, and user details loading
 */

package com.library.Service;

import com.library.Model.*;
import com.library.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// CORRECT: Use Spring's @Transactional, not javax or jakarta
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LibraryService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    // Get all books from database
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Get a specific book by ID
    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    // Save a new book to database
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    // Delete a book by ID
    @Transactional
    public boolean deleteBook(Long bookId) {
        try {
            // First delete any borrow records for this book
            Book book = getBookById(bookId);
            if (book != null) {
                // In a real system, we should check for existing borrow records
                // For simplicity, we'll just delete the book
                bookRepository.deleteById(bookId);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting book: " + e.getMessage());
            return false;
        }
    }

    // Add a new book with title, author, ISBN
    public Book addBook(String title, String author, String isbn) {
        Book book = new Book(title, author, isbn);
        return bookRepository.save(book);
    }

    // Find user by username
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Borrow a book (mark as unavailable and create record)
    @Transactional  // This annotation ensures both operations complete together
    public void borrowBook(Book book, User user) {
        // Mark book as unavailable
        book.setAvailable(false);
        bookRepository.save(book);

        // Create borrow record
        BorrowRecord record = new BorrowRecord(book, user);
        borrowRecordRepository.save(record);
    }

    // Borrow a book by ID (alternative method)
    @Transactional
    public void borrowBook(Long bookId, User user) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));

        if (!book.isAvailable()) {
            throw new RuntimeException("Book is already borrowed");
        }

        book.setAvailable(false);
        bookRepository.save(book);

        BorrowRecord record = new BorrowRecord(book, user);
        borrowRecordRepository.save(record);
    }

    // Return a book (mark as available and update record)
    @Transactional
    public boolean returnBook(Long bookId, User user) {
        // Find the book
        Book book = getBookById(bookId);
        if (book == null) return false;

        // Find the borrow record
        BorrowRecord record = borrowRecordRepository.findByBookAndUserAndReturnDateIsNull(book, user);
        if (record == null) return false;

        // Mark as returned
        record.setReturnDate(java.time.LocalDate.now());
        borrowRecordRepository.save(record);

        // Mark book as available again
        book.setAvailable(true);
        bookRepository.save(book);

        return true;
    }

    // Get all books borrowed by a user (not returned yet)
    public List<Book> getBorrowedBooks(User user) {
        List<BorrowRecord> records = borrowRecordRepository.findByUserAndReturnDateIsNull(user);

        // Convert borrow records to books
        return records.stream()
                .map(BorrowRecord::getBook)
                .collect(Collectors.toList());
    }

    // ---------- INTENTIONALLY VULNERABLE METHOD! ----------
    // This method has SQL injection vulnerability
    // DO NOT USE THIS IN REAL APPLICATIONS!
    // I am including it for security testing purposes
    // WARNING: This is unsafe! Direct string concatenation in SQL
    // Example attack: query = "' OR '1'='1" would return ALL books
    // UNSAFE: Vulnerable to SQL injection
    public List<Book> searchBooksUnsafe(String query) {
        // This is unsafe - direct string concatenation in SQL!
        return bookRepository.searchUnsafe(query);
    }
}
