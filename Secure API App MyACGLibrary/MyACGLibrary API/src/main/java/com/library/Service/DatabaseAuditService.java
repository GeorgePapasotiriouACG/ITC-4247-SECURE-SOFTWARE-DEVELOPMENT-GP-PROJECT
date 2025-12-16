/*
 * DatabaseAuditService - Tracks database changes for security analysis
 * Created by: George Papasotiriou
 * Date: 12/11/2025
 */

package com.library.Service;

import com.library.Model.AuditLog;
import com.library.Repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

@Service
public class DatabaseAuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logDatabaseChange(String tableName, Long recordId, String action,
                                  String oldValues, String newValues, String username) {

        AuditLog log = new AuditLog();
        log.setTableName(tableName);
        log.setRecordId(recordId);
        log.setAction(action);
        log.setOldValues(oldValues);
        log.setNewValues(newValues);
        log.setPerformedBy(username);
        log.setPerformedAt(LocalDateTime.now());

        auditLogRepository.save(log);
    }

    public void logSecurityEvent(String description, String severity, HttpServletRequest request) {
        AuditLog log = new AuditLog();
        log.setTableName("SECURITY_EVENT");
        log.setAction(severity);
        log.setOldValues(description);
        log.setNewValues(request.getRemoteAddr());
        log.setPerformedAt(LocalDateTime.now());

        auditLogRepository.save(log);
    }
}
