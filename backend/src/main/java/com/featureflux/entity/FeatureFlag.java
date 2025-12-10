package com.featureflux.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feature_flags")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer rolloutPercentage = 0;

    @ElementCollection
    @CollectionTable(name = "flag_target_users", joinColumns = @JoinColumn(name = "flag_id"))
    @Column(name = "user_id")
    @Builder.Default
    private List<String> targetUserIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "flag_excluded_users", joinColumns = @JoinColumn(name = "flag_id"))
    @Column(name = "user_id")
    @Builder.Default
    private List<String> excludedUserIds = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean archived = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}

