package com.featureflux.controller;

import com.featureflux.dto.EvaluationRequest;
import com.featureflux.dto.EvaluationResponse;
import com.featureflux.service.FeatureEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluate")
@RequiredArgsConstructor
public class EvaluationController {

    private final FeatureEvaluationService evaluationService;

    @PostMapping
    public ResponseEntity<EvaluationResponse> evaluate(@Valid @RequestBody EvaluationRequest request) {
        boolean enabled = evaluationService.evaluate(request.getFlagKey(), request.getUserId());
        
        EvaluationResponse response = new EvaluationResponse();
        response.setFlagKey(request.getFlagKey());
        response.setUserId(request.getUserId());
        response.setEnabled(enabled);
        
        response.setBucket(calculateBucket(request.getFlagKey(), request.getUserId()));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{flagKey}/{userId}")
    public ResponseEntity<EvaluationResponse> evaluateGet(
            @PathVariable String flagKey,
            @PathVariable String userId) {
        boolean enabled = evaluationService.evaluate(flagKey, userId);
        
        EvaluationResponse response = new EvaluationResponse();
        response.setFlagKey(flagKey);
        response.setUserId(userId);
        response.setEnabled(enabled);
        response.setBucket(calculateBucket(flagKey, userId));
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Boolean>> evaluateBulk(@RequestBody Map<String, String> requests) {
        Map<String, Boolean> results = new HashMap<>();
        requests.forEach((flagKey, userId) -> {
            results.put(flagKey, evaluationService.evaluate(flagKey, userId));
        });
        return ResponseEntity.ok(results);
    }

    private int calculateBucket(String flagKey, String userId) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            String input = flagKey + ":" + userId;
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            int hashValue = Math.abs(java.nio.ByteBuffer.wrap(hash, 0, 4).getInt());
            return hashValue % 100;
        } catch (Exception e) {
            return Math.abs((flagKey + userId).hashCode()) % 100;
        }
    }
}

