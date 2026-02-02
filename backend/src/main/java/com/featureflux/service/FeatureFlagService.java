package com.featureflux.service;

import com.featureflux.entity.FeatureFlag;
import com.featureflux.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagService {

    private final FeatureFlagRepository repository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuditService auditService;
    
    private static final String EVALUATION_CACHE_PREFIX = "eval:";

    public List<FeatureFlag> getAllFlags() {
        return repository.findByArchivedFalse();
    }

    public Optional<FeatureFlag> getFlagById(Long id) {
        return repository.findById(id);
    }

    @Cacheable(value = "featureFlags", key = "#key")
    public Optional<FeatureFlag> getFlagByKey(String key) {
        return repository.findByKey(key);
    }

    @Transactional
    public FeatureFlag createFlag(FeatureFlag flag, jakarta.servlet.http.HttpServletRequest request) {
        if (repository.findByKey(flag.getKey()).isPresent()) {
            throw new IllegalArgumentException("Feature flag with key '" + flag.getKey() + "' already exists");
        }
        FeatureFlag saved = repository.save(flag);
        evictEvaluationCache(flag.getKey());
        if (request != null) {
            auditService.logFeatureFlagChange("CREATE", saved, null, request);
        }
        return saved;
    }

    @Transactional
    @CacheEvict(value = "featureFlags", key = "#flag.key")
    public FeatureFlag updateFlag(FeatureFlag flag, jakarta.servlet.http.HttpServletRequest request) {
        FeatureFlag existing = repository.findById(flag.getId())
                .orElseThrow(() -> new IllegalArgumentException("Feature flag not found"));
        
        if (!existing.getKey().equals(flag.getKey())) {
            throw new IllegalArgumentException("Cannot change feature flag key");
        }
        
        FeatureFlag updated = repository.save(flag);
        evictEvaluationCache(flag.getKey());
        if (request != null) {
            auditService.logFeatureFlagChange("UPDATE", updated, existing, request);
        }
        return updated;
    }

    @Transactional
    @CacheEvict(value = "featureFlags", key = "#key")
    public void deleteFlag(String key, jakarta.servlet.http.HttpServletRequest request) {
        FeatureFlag flag = repository.findByKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Feature flag not found"));
        FeatureFlag oldFlag = FeatureFlag.builder()
                .id(flag.getId())
                .key(flag.getKey())
                .name(flag.getName())
                .enabled(flag.getEnabled())
                .rolloutPercentage(flag.getRolloutPercentage())
                .build();
        flag.setArchived(true);
        repository.save(flag);
        evictEvaluationCache(key);
        if (request != null) {
            auditService.logFeatureFlagChange("DELETE", flag, oldFlag, request);
        }
    }

    @Transactional
    @CacheEvict(value = "featureFlags", key = "#key")
    public FeatureFlag toggleFlag(String key, boolean enabled, jakarta.servlet.http.HttpServletRequest request) {
        FeatureFlag flag = repository.findByKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Feature flag not found"));
        FeatureFlag oldFlag = FeatureFlag.builder()
                .id(flag.getId())
                .key(flag.getKey())
                .name(flag.getName())
                .enabled(flag.getEnabled())
                .rolloutPercentage(flag.getRolloutPercentage())
                .build();
        flag.setEnabled(enabled);
        FeatureFlag updated = repository.save(flag);
        evictEvaluationCache(key);
        if (request != null) {
            auditService.logFeatureFlagChange("TOGGLE", updated, oldFlag, request);
        }
        return updated;
    }
    
    private void evictEvaluationCache(String flagKey) {
        // Evict cache entries for this flag
        // Note: Pattern deletion requires SCAN in production
        log.info("Evicting evaluation cache for flag: {}", flagKey);
    }
}

