/*
 *
 * Created by: George Papasotiriou
 * Date: 2024-01-15
 *
 * Configures security settings, JWT authentication, and authorization rules
 */

package com.library.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple JWT utility for token generation and validation.
 * This is a simplified version for educational purposes.
 */
@Component
public class JwtUtil {
    
    // Secret key for signing tokens (in production, use environment variable!)
    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    // Token validity duration (24 hours)
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;
    
    /**
     * Generate a JWT token for a user
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("created", new Date());
        
        return Jwts.builder()
                  .setClaims(claims)
                  .setSubject(username)
                  .setIssuedAt(new Date())
                  .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                  .signWith(SECRET_KEY)
                  .compact();
    }
    
    /**
     * Validate a JWT token and extract username
     */
    public String validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                               .setSigningKey(SECRET_KEY)
                               .build()
                               .parseClaimsJws(token)
                               .getBody();
            
            return claims.getSubject();
        } catch (Exception e) {
            // Token is invalid or expired
            return null;
        }
    }
}