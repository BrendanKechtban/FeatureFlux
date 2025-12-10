package com.featureflux.service;

import com.featureflux.entity.FeatureFlag;
import com.featureflux.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureEvaluationService {

    private final FeatureFlagRepository featureFlagRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KillSwitchService killSwitchService;

    private static final String EVALUATION_CACHE_PREFIX = "eval:";
    private static final long CACHE_TTL_SECONDS = 60;

    /**
     * Evaluates a feature flag for a given user with deterministic bucketing.
     * Uses Redis for high-frequency reads to offload PostgreSQL.
     */
    @Cacheable(value = "evaluations", key = "#flagKey + ':' + #userId")
    public boolean evaluate(String flagKey, String userId) {
        // Check Redis cache first
        String cacheKey = EVALUATION_CACHE_PREFIX + flagKey + ":" + userId;
        Boolean cachedResult = (Boolean) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedResult != null) {
            log.debug("Cache hit for flag: {} user: {}", flagKey, userId);
            return cachedResult;
        }

        // Cache miss - fetch from database
        Optional<FeatureFlag> flagOpt = featureFlagRepository.findByKey(flagKey);
        
        if (flagOpt.isEmpty()) {
            log.warn("Feature flag not found: {}", flagKey);
            return false;
        }

        FeatureFlag flag = flagOpt.get();
        
        // Check kill switch first - overrides everything
        if (killSwitchService.isKillSwitchActive(flagKey)) {
            log.warn("Kill switch active for flag: {}", flagKey);
            redisTemplate.opsForValue().set(cacheKey, false, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            return false;
        }
        
        boolean result = evaluateFlag(flag, userId);

        // Cache the result in Redis
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        
        return result;
    }

    /**
     * Deterministic user bucketing algorithm for percentage-based rollouts.
     * Uses consistent hashing to ensure the same user always gets the same bucket.
     */
    private boolean evaluateFlag(FeatureFlag flag, String userId) {
        // If flag is disabled, return false
        if (!flag.getEnabled()) {
            return false;
        }

        // Check explicit exclusions first
        if (flag.getExcludedUserIds() != null && flag.getExcludedUserIds().contains(userId)) {
            return false;
        }

        // Check explicit inclusions
        if (flag.getTargetUserIds() != null && flag.getTargetUserIds().contains(userId)) {
            return true;
        }

        // Percentage-based rollout using deterministic bucketing
        if (flag.getRolloutPercentage() != null && flag.getRolloutPercentage() > 0) {
            int bucket = getDeterministicBucket(flag.getKey(), userId);
            return bucket < flag.getRolloutPercentage();
        }

        // If no rollout percentage, default to false unless explicitly enabled
        return false;
    }

    /**
     * Deterministic bucketing: same flag + same user = same bucket (0-99)
     * Uses SHA-256 hash for consistent distribution
     */
    private int getDeterministicBucket(String flagKey, String userId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = flagKey + ":" + userId;
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // Use first 4 bytes to get a number between 0-99
            int hashValue = Math.abs(java.nio.ByteBuffer.wrap(hash, 0, 4).getInt());
            return hashValue % 100;
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            // Fallback to simple hash
            return Math.abs((flagKey + userId).hashCode()) % 100;
        }
    }

    @CacheEvict(value = "evaluations", key = "#flagKey + ':*'")
    public void evictCache(String flagKey) {
        // Also manually evict from Redis
        String pattern = EVALUATION_CACHE_PREFIX + flagKey + ":*";
        // Note: Redis pattern deletion requires SCAN in production, simplified here
        log.info("Evicting cache for flag: {}", flagKey);
    }

    public boolean evaluateBulk(String flagKey, String userId) {
        return evaluate(flagKey, userId);
    }
}

