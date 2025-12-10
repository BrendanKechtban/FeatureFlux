package com.featureflux.repository;

import com.featureflux.entity.KillSwitch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KillSwitchRepository extends JpaRepository<KillSwitch, Long> {
    Optional<KillSwitch> findByFlagKey(String flagKey);
    
    List<KillSwitch> findByActiveTrue();
}

