package com.featureflux.service;

import com.featureflux.entity.KillSwitch;
import com.featureflux.repository.KillSwitchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class KillSwitchService {

    private final KillSwitchRepository killSwitchRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KILL_SWITCH_CACHE_PREFIX = "killswitch:";
    private static final long CACHE_TTL_SECONDS = 300; // 5 minutes

    @Transactional
    @CacheEvict(value = "killSwitches", key = "#flagKey")
    public KillSwitch activateKillSwitch(String flagKey, String reason, String activatedBy) {
        Optional<KillSwitch> existing = killSwitchRepository.findByFlagKey(flagKey);
        
        KillSwitch killSwitch = existing.orElseGet(() -> KillSwitch.builder()
                .flagKey(flagKey)
                .active(false)
                .build());
        
        killSwitch.setActive(true);
        killSwitch.setReason(reason);
        killSwitch.setActivatedBy(activatedBy);
        
        KillSwitch saved = killSwitchRepository.save(killSwitch);
        
        // Cache in Redis for fast lookup
        String cacheKey = KILL_SWITCH_CACHE_PREFIX + flagKey;
        redisTemplate.opsForValue().set(cacheKey, true, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        
        log.info("Kill switch activated for flag: {} by {}", flagKey, activatedBy);
        return saved;
    }

    @Transactional
    @CacheEvict(value = "killSwitches", key = "#flagKey")
    public KillSwitch deactivateKillSwitch(String flagKey) {
        Optional<KillSwitch> existing = killSwitchRepository.findByFlagKey(flagKey);
        
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Kill switch not found for flag: " + flagKey);
        }
        
        KillSwitch killSwitch = existing.get();
        killSwitch.setActive(false);
        killSwitch.setReason(null);
        killSwitch.setActivatedBy(null);
        
        KillSwitch saved = killSwitchRepository.save(killSwitch);
        
        // Remove from Redis cache
        String cacheKey = KILL_SWITCH_CACHE_PREFIX + flagKey;
        redisTemplate.delete(cacheKey);
        
        log.info("Kill switch deactivated for flag: {}", flagKey);
        return saved;
    }

    public boolean isKillSwitchActive(String flagKey) {
        // Check Redis cache first
        String cacheKey = KILL_SWITCH_CACHE_PREFIX + flagKey;
        Boolean cached = (Boolean) redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return cached;
        }
        
        // Cache miss - check database
        Optional<KillSwitch> killSwitch = killSwitchRepository.findByFlagKey(flagKey);
        boolean active = killSwitch.map(KillSwitch::getActive).orElse(false);
        
        // Cache the result
        redisTemplate.opsForValue().set(cacheKey, active, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        
        return active;
    }

    public List<KillSwitch> getActiveKillSwitches() {
        return killSwitchRepository.findByActiveTrue();
    }

    public Optional<KillSwitch> getKillSwitch(String flagKey) {
        return killSwitchRepository.findByFlagKey(flagKey);
    }
}

