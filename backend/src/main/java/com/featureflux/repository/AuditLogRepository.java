package com.featureflux.repository;

import com.featureflux.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);
    
    List<AuditLog> findByEntityTypeAndEntityKeyOrderByTimestampDesc(String entityType, String entityKey);
    
    List<AuditLog> findByPerformedByOrderByTimestampDesc(String performedBy);
    
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentLogs(LocalDateTime since);
}

