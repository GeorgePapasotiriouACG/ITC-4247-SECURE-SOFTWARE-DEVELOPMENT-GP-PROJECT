/*
 *
 * Created by: George Papasotiriou
 * Date: 12/11/2025
 *
 * Configures security settings, JWT authentication, and authorization rules
 */

package com.library.Model;

// IMPORTANT: Use jakarta.persistence, not javax.persistence
import jakarta.persistence.*;
import java.time.LocalDate;

// This class tracks when users borrow and return books
@Entity
@Table(name = "borrow_records")
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne means many borrow records can point to one book
    @ManyToOne
    @JoinColumn(name = "book_id") // Foreign key to books table
    private Book book;

    @ManyToOne
    @JoinColumn(name = "user_id") // Foreign key to users table
    private User user;

    // Date when book was borrowed (auto-set to today)
    private LocalDate borrowDate = LocalDate.now();

    // Date when book was returned (null if not returned yet)
    private LocalDate returnDate;

    // Empty constructor for JPA
    public BorrowRecord() {}

    // Constructor for new borrow records
    public BorrowRecord(Book book, User user) {
        this.book = book;
        this.user = user;
        this.borrowDate = LocalDate.now();
        this.returnDate = null; // Not returned yet
    }

    // ---------- GETTERS AND SETTERS ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    // Check if book has been returned
    public boolean isReturned() {
        return returnDate != null;
    }
}
