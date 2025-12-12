/*
 *
 * Created by: George Papasotiriou
 * Date: 12/11/2025
 *
 * Configures security settings, JWT authentication, and authorization rules
 */

package com.library.Repository;

import com.library.Model.Book;
import com.library.Model.User;
import com.library.Model.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findByUserAndReturnDateIsNull(User user);
    BorrowRecord findByBookAndUserAndReturnDateIsNull(Book book, User user);
}
