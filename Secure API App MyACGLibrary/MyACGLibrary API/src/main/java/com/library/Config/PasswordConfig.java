/*
 * PasswordConfig - Password Encoder Configuration
 * Created by: George Papasotiriou
 * Date: 12/11/2025
 *
 * Separate configuration for PasswordEncoder to avoid circular dependencies
 */

package com.library.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
