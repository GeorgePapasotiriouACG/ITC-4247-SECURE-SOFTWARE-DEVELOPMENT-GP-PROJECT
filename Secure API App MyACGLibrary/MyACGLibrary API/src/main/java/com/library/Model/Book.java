/*
 *
 * Created by: George Papasotiriou
 * Date: 2024-01-15
 *
 * Configures security settings, JWT authentication, and authorization rules
 */

package com.library.Model;

// IMPORTANT: Spring Boot 2.7.0 uses JAKARTA, not JAVAX
import jakarta.persistence.*;

// This class represents a Book in our library
// @Entity tells Spring this is a database table
@Entity
@Table(name = "books")
public class Book {

    // @Id means this is the primary key (unique ID for each book)
    // @GeneratedValue means the database automatically creates IDs
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column with nullable=false means this field is required
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    // ISBN is optional (no nullable=false)
    private String isbn;

    // Available by default (true = book can be borrowed)
    private boolean available = true;

    // Empty constructor needed for JPA (database framework)
    public Book() {}

    // Constructor for creating new books
    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.available = true; // New books are always available
    }

    // ---------- GETTERS AND SETTERS ----------
    // These let us read and change the book's properties

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    // toString() creates a readable string for debugging
    @Override
    public String toString() {
        return String.format("Book[id=%d, title='%s', author='%s', available=%s]",
                id, title, author, available ? "Yes" : "No");
    }
}