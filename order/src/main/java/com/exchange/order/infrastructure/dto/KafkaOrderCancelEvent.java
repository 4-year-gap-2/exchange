package com.exchange.order.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class KafkaOrderCancelEvent {
    // 유저ID, 오더ID 전달
    private UUID orderId;
    private UUID userId;
}

