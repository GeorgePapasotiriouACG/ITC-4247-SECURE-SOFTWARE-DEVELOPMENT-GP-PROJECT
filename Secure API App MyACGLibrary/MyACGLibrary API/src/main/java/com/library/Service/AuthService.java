/*
 * AuthService - Authentication Service
 * Created by: George Papasotiriou
 * Date: 12/11/2025
 *
 * Handles user authentication, registration, and user details loading
 */

package com.library.Service;

import com.library.Model.User;
import com.library.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class AuthService implements UserDetailsService {

    private static final Logger logger = Logger.getLogger(AuthService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.warning("User not found: " + username);
            return false;
        }

        boolean matches = passwordEncoder.matches(password, user.getPassword());
        logger.info("Authentication attempt for " + username + ": " + (matches ? "SUCCESS" : "FAILED"));
        return matches;
    }

    public boolean registerUser(String username, String password, String role) {
        if (userRepository.findByUsername(username) != null) {
            logger.warning("Username already exists: " + username);
            return false;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_" + role.toUpperCase());

        userRepository.save(user);
        logger.info("User registered: " + username + " with role: " + role);
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().replace("ROLE_", ""))
                .build();
    }
}
