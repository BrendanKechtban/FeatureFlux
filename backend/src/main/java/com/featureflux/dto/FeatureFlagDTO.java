package com.featureflux.dto;

import com.featureflux.entity.FeatureFlag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlagDTO {
    private Long id;
    private String key;
    private String name;
    private String description;
    private Boolean enabled;
    private Integer rolloutPercentage;
    private List<String> targetUserIds;
    private List<String> excludedUserIds;
    private Boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FeatureFlagDTO fromEntity(FeatureFlag flag) {
        return FeatureFlagDTO.builder()
                .id(flag.getId())
                .key(flag.getKey())
                .name(flag.getName())
                .description(flag.getDescription())
                .enabled(flag.getEnabled())
                .rolloutPercentage(flag.getRolloutPercentage())
                .targetUserIds(flag.getTargetUserIds() != null ? new ArrayList<>(flag.getTargetUserIds()) : new ArrayList<>())
                .excludedUserIds(flag.getExcludedUserIds() != null ? new ArrayList<>(flag.getExcludedUserIds()) : new ArrayList<>())
                .archived(flag.getArchived())
                .createdAt(flag.getCreatedAt())
                .updatedAt(flag.getUpdatedAt())
                .build();
    }

    public FeatureFlag toEntity() {
        return FeatureFlag.builder()
                .id(this.id)
                .key(this.key)
                .name(this.name)
                .description(this.description)
                .enabled(this.enabled != null ? this.enabled : false)
                .rolloutPercentage(this.rolloutPercentage != null ? this.rolloutPercentage : 0)
                .targetUserIds(this.targetUserIds != null ? new ArrayList<>(this.targetUserIds) : new ArrayList<>())
                .excludedUserIds(this.excludedUserIds != null ? new ArrayList<>(this.excludedUserIds) : new ArrayList<>())
                .archived(this.archived != null ? this.archived : false)
                .build();
    }
}

