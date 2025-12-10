package com.featureflux.controller;

import com.featureflux.dto.FeatureFlagDTO;
import com.featureflux.entity.FeatureFlag;
import com.featureflux.service.FeatureFlagService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/flags")
@RequiredArgsConstructor
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    @GetMapping
    public ResponseEntity<List<FeatureFlagDTO>> getAllFlags() {
        List<FeatureFlagDTO> flags = featureFlagService.getAllFlags().stream()
                .map(FeatureFlagDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(flags);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeatureFlagDTO> getFlagById(@PathVariable Long id) {
        return featureFlagService.getFlagById(id)
                .map(flag -> ResponseEntity.ok(FeatureFlagDTO.fromEntity(flag)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/key/{key}")
    public ResponseEntity<FeatureFlagDTO> getFlagByKey(@PathVariable String key) {
        return featureFlagService.getFlagByKey(key)
                .map(flag -> ResponseEntity.ok(FeatureFlagDTO.fromEntity(flag)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FeatureFlagDTO> createFlag(@Valid @RequestBody FeatureFlagDTO dto, HttpServletRequest request) {
        try {
            FeatureFlag flag = featureFlagService.createFlag(dto.toEntity(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(FeatureFlagDTO.fromEntity(flag));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeatureFlagDTO> updateFlag(@PathVariable Long id, @Valid @RequestBody FeatureFlagDTO dto, HttpServletRequest request) {
        try {
            dto.setId(id);
            FeatureFlag flag = featureFlagService.updateFlag(dto.toEntity(), request);
            return ResponseEntity.ok(FeatureFlagDTO.fromEntity(flag));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteFlag(@PathVariable String key, HttpServletRequest request) {
        try {
            featureFlagService.deleteFlag(key, request);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{key}/toggle")
    public ResponseEntity<FeatureFlagDTO> toggleFlag(@PathVariable String key, @RequestBody ToggleRequest request, HttpServletRequest httpRequest) {
        try {
            FeatureFlag flag = featureFlagService.toggleFlag(key, request.getEnabled(), httpRequest);
            return ResponseEntity.ok(FeatureFlagDTO.fromEntity(flag));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Data
    static class ToggleRequest {
        private Boolean enabled;
    }
}

