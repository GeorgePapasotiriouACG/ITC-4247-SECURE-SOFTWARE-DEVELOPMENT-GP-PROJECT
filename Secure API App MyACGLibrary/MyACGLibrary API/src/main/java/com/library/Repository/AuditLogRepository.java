/*
 * AuditLogRepository - Repository for audit logs
 * Created by: George Papasotiriou
 * Date: 12/11/2025
 */

package com.library.Repository;

import com.library.Model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTableName(String tableName);
    List<AuditLog> findByAction(String action);
    List<AuditLog> findByPerformedAtBetween(LocalDateTime start, LocalDateTime end);
    List<AuditLog> findByPerformedBy(String username);
}
