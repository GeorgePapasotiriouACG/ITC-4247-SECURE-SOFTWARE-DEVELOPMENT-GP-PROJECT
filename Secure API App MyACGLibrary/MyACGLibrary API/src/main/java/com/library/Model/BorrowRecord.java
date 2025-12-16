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
import java.time.LocalDateTime;

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

    // Due date (14 days from borrow date)
    private LocalDate dueDate = LocalDate.now().plusDays(14);

    // Date when book was returned (null if not returned yet)
    private LocalDate returnDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Empty constructor for JPA
    public BorrowRecord() {}

    // Constructor for new borrow records
    public BorrowRecord(Book book, User user) {
        this.book = book;
        this.user = user;
        this.borrowDate = LocalDate.now();
        this.dueDate = LocalDate.now().plusDays(14);
        this.returnDate = null; // Not returned yet
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Check if book has been returned
    public boolean isReturned() {
        return returnDate != null;
    }
}
