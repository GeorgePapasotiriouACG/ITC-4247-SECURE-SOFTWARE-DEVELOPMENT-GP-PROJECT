/*
 * AuthController - Authentication Controller
 * Created by: George Papasotiriou
 * Date: 12/11/2025
 *
 * Handles user authentication, registration, and JWT token generation
 */

package com.library.Service;  // CHANGED: Must match folder name

import com.library.Service.AuthService;
import com.library.Utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication controller for login and token generation.
 * This controller handles user authentication and registration.


 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Login endpoint - Authenticates user and returns JWT token
     * POST /api/auth/login
     * Body: {"username": "username", "password": "password"}
     * Returns: JWT token for accessing protected endpoints
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        if (username == null || password == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Username and password required");
            return ResponseEntity.badRequest().body(error);
        }

        // Authenticate user
        boolean authenticated = authService.authenticate(username, password);

        if (authenticated) {
            // Generate JWT token
            String token = jwtUtil.generateToken(username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("username", username);

            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Invalid username or password");
            return ResponseEntity.status(401).body(error);
        }
    }

    /**
     * Register new user endpoint
     * POST /api/auth/register
     * Body: {"username": "newuser", "password": "password", "role": "USER"}
     * Role is optional (defaults to USER)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) {
        try {
            String username = userData.get("username");
            String password = userData.get("password");
            String role = userData.getOrDefault("role", "USER");

            if (username == null || password == null) {
                return createErrorResponse("Username and password required");
            }

            boolean created = authService.registerUser(username, password, role);

            if (created) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "User registered successfully");
                return ResponseEntity.ok(response);
            } else {
                return createErrorResponse("Username already exists");
            }
        } catch (Exception e) {
            return createErrorResponse("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Helper method to create error responses
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        return ResponseEntity.badRequest().body(error);
    }
}
