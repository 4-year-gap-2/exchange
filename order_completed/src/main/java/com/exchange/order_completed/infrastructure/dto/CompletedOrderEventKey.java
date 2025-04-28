package com.exchange.order_completed.infrastructure.dto;


import java.time.LocalDateTime;
import java.util.UUID;

public class CompletedOrderEventKey {

    private UUID userId;
    private UUID orderId;
    private LocalDateTime createdAt;
}
