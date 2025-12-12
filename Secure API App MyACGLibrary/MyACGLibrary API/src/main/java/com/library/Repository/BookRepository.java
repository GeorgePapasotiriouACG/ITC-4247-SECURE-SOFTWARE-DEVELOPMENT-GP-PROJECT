/*
 *
 * Created by: George Papasotiriou
 * Date: 2024-01-15
 *
 * Configures security settings, JWT authentication, and authorization rules
 */

package com.library.Repository;

import com.library.Model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    // Safe search using JPA method
    List<Book> findByTitleContainingOrAuthorContaining(String title, String author);

    // UNSAFE search using native SQL (vulnerable to SQL injection!)
    @Query(value = "SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'",
            nativeQuery = true)
    List<Book> searchUnsafe(@Param("query") String query);
}