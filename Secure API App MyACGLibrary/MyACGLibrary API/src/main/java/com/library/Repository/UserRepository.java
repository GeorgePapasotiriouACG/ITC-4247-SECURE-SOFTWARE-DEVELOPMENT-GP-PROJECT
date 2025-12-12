/*
 *
 * Created by: George Papasotiriou
 * Date: 12/11/2025
 *
 * Configures security settings, JWT authentication, and authorization rules
 */

package com.library.Repository;

import com.library.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
