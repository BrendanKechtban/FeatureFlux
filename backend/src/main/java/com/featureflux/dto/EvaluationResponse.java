package com.featureflux.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResponse {
    private String flagKey;
    private String userId;
    private Boolean enabled;
    private Integer bucket;
}

