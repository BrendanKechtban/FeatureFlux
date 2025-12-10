package com.featureflux.controller;

import com.featureflux.dto.KillSwitchRequest;
import com.featureflux.entity.KillSwitch;
import com.featureflux.service.AuditService;
import com.featureflux.service.KillSwitchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/killswitch")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class KillSwitchController {

    private final KillSwitchService killSwitchService;
    private final AuditService auditService;

    @PostMapping("/{flagKey}/activate")
    public ResponseEntity<KillSwitch> activateKillSwitch(
            @PathVariable String flagKey,
            @Valid @RequestBody KillSwitchRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        String activatedBy = authentication.getName();
        KillSwitch killSwitch = killSwitchService.activateKillSwitch(
                flagKey, request.getReason(), activatedBy);
        
        auditService.logKillSwitchChange(flagKey, true, request.getReason(), httpRequest);
        
        return ResponseEntity.ok(killSwitch);
    }

    @PostMapping("/{flagKey}/deactivate")
    public ResponseEntity<KillSwitch> deactivateKillSwitch(
            @PathVariable String flagKey,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        KillSwitch killSwitch = killSwitchService.deactivateKillSwitch(flagKey);
        
        auditService.logKillSwitchChange(flagKey, false, "Deactivated by " + authentication.getName(), httpRequest);
        
        return ResponseEntity.ok(killSwitch);
    }

    @GetMapping("/{flagKey}")
    public ResponseEntity<KillSwitch> getKillSwitch(@PathVariable String flagKey) {
        return killSwitchService.getKillSwitch(flagKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<List<KillSwitch>> getActiveKillSwitches() {
        return ResponseEntity.ok(killSwitchService.getActiveKillSwitches());
    }
}

