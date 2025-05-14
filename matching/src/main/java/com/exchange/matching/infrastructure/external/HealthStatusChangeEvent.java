package com.exchange.matching.infrastructure.external;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HealthStatusChangeEvent {
    private final boolean healthy;
}
