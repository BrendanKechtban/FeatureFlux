package com.featureflux.repository;

import com.featureflux.entity.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {
    Optional<FeatureFlag> findByKey(String key);
    
    List<FeatureFlag> findByArchivedFalse();
    
    @Query("SELECT f FROM FeatureFlag f WHERE f.archived = false AND f.enabled = true")
    List<FeatureFlag> findActiveFlags();
}

