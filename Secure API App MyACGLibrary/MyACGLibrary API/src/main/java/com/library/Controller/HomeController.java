/*
 * HomeController - Basic API endpoints
 * Created by: George Papasotiriou
 * Date: 2024-01-15
 */

package com.library.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Library Management System API");
        response.put("version", "1.0.0");
        response.put("createdBy", "George Papasotiriou");
        response.put("status", "running");
        response.put("endpoints", new String[] {
                "POST /api/auth/register - Register new user",
                "POST /api/auth/login - Login and get JWT token",
                "GET  /api/books - Get all books (requires token)",
                "POST /api/books - Add new book (admin only)",
                "PUT  /api/books/{id} - Update a book (admin only)",
                "DELETE /api/books/{id} - Delete a book (admin only)",
                "POST /api/borrow/{id} - Borrow a book",
                "POST /api/return/{id} - Return a book",
                "GET  /api/user/books - Get your borrowed books",
                "GET  /api/search?q=query - Search books (⚠️ has SQLi)"
        });
        return response;
    }

    @GetMapping("/api/test")
    public Map<String, Object> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "API is working correctly!");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/api/status")
    public Map<String, Object> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Library API");
        response.put("status", "UP");
        response.put("database", "H2");
        response.put("authentication", "JWT");
        return response;
    }
}