package com.featureflux.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // CREATE, UPDATE, DELETE, TOGGLE, KILL_SWITCH

    @Column(nullable = false)
    private String entityType; // FEATURE_FLAG, USER, etc.

    @Column
    private Long entityId;

    @Column
    private String entityKey; // For feature flags

    @Column(length = 2000)
    private String oldValue; // JSON representation of old state

    @Column(length = 2000)
    private String newValue; // JSON representation of new state

    @Column(nullable = false)
    private String performedBy; // Username

    @Column
    private String ipAddress;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(length = 1000)
    private String description;
}

