package com.featureflux.service;

import com.featureflux.entity.AuditLog;
import com.featureflux.entity.FeatureFlag;
import com.featureflux.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void logFeatureFlagChange(String action, FeatureFlag flag, FeatureFlag oldFlag, HttpServletRequest request) {
        try {
            String oldValue = oldFlag != null ? objectMapper.writeValueAsString(oldFlag) : null;
            String newValue = flag != null ? objectMapper.writeValueAsString(flag) : null;
            String performedBy = getCurrentUsername();
            String ipAddress = getClientIpAddress(request);

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType("FEATURE_FLAG")
                    .entityId(flag != null ? flag.getId() : (oldFlag != null ? oldFlag.getId() : null))
                    .entityKey(flag != null ? flag.getKey() : (oldFlag != null ? oldFlag.getKey() : null))
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .performedBy(performedBy)
                    .ipAddress(ipAddress)
                    .description(buildDescription(action, flag, oldFlag))
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit log created: {} by {}", action, performedBy);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    @Transactional
    public void logKillSwitchChange(String flagKey, boolean activated, String reason, HttpServletRequest request) {
        try {
            String performedBy = getCurrentUsername();
            String ipAddress = getClientIpAddress(request);

            AuditLog auditLog = AuditLog.builder()
                    .action("KILL_SWITCH")
                    .entityType("FEATURE_FLAG")
                    .entityKey(flagKey)
                    .performedBy(performedBy)
                    .ipAddress(ipAddress)
                    .description(String.format("Kill switch %s for flag '%s'. Reason: %s", 
                            activated ? "activated" : "deactivated", flagKey, reason))
                    .newValue(String.format("{\"killSwitchActive\": %s, \"reason\": \"%s\"}", activated, reason))
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Kill switch audit log created: {} by {}", flagKey, performedBy);
        } catch (Exception e) {
            log.error("Failed to create kill switch audit log", e);
        }
    }

    public List<AuditLog> getAuditLogsForFlag(String flagKey) {
        return auditLogRepository.findByEntityTypeAndEntityKeyOrderByTimestampDesc("FEATURE_FLAG", flagKey);
    }

    public List<AuditLog> getRecentAuditLogs(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return auditLogRepository.findRecentLogs(since);
    }

    public List<AuditLog> getAuditLogsByUser(String username) {
        return auditLogRepository.findByPerformedByOrderByTimestampDesc(username);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "SYSTEM";
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String buildDescription(String action, FeatureFlag flag, FeatureFlag oldFlag) {
        if (flag == null && oldFlag == null) return action;
        
        String flagKey = flag != null ? flag.getKey() : oldFlag.getKey();
        
        switch (action) {
            case "CREATE":
                return String.format("Created feature flag '%s'", flagKey);
            case "UPDATE":
                return String.format("Updated feature flag '%s'", flagKey);
            case "DELETE":
                return String.format("Deleted feature flag '%s'", flagKey);
            case "TOGGLE":
                boolean newState = flag != null && flag.getEnabled();
                return String.format("Toggled feature flag '%s' to %s", flagKey, newState ? "enabled" : "disabled");
            default:
                return action + " on " + flagKey;
        }
    }
}

