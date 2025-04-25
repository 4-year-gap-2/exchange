package com.exchange.order_completed.domain.repository;

import com.exchange.order_completed.domain.entity.CompletedOrder;

import java.util.Optional;
import java.util.UUID;

public interface CompletedOrderReader {

    CompletedOrder findByUserIdAndOrderId(UUID userId, UUID orderId);

    Optional<CompletedOrder> findByOrderId(UUID orderId);
}
