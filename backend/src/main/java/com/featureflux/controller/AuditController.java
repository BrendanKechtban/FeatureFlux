package com.featureflux.controller;

import com.featureflux.entity.AuditLog;
import com.featureflux.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/flag/{flagKey}")
    public ResponseEntity<List<AuditLog>> getAuditLogsForFlag(@PathVariable String flagKey) {
        return ResponseEntity.ok(auditService.getAuditLogsForFlag(flagKey));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(@PathVariable String username) {
        return ResponseEntity.ok(auditService.getAuditLogsByUser(username));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<AuditLog>> getRecentAuditLogs(
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(auditService.getRecentAuditLogs(hours));
    }
}

