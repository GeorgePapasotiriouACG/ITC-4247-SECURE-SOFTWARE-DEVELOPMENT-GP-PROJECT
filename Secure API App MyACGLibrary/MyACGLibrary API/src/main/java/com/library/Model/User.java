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

// This class represents a User in our system
@Entity
@Table(name = "users") // Table name is "users"
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Username must be unique (no duplicates) and required
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // Role can be "ROLE_USER" or "ROLE_ADMIN"
    private String role = "ROLE_USER"; // Default role is USER

    // Empty constructor for JPA
    public User() {}

    // Constructor for creating users
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // ---------- GETTERS AND SETTERS ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // Helper method to check if user is an admin
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(role);
    }
}
