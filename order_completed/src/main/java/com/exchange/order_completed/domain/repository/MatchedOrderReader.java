package com.exchange.order_completed.domain.repository;

import com.exchange.order_completed.domain.entity.MatchedOrder;

import java.util.Optional;
import java.util.UUID;

public interface MatchedOrderReader {

    MatchedOrder findByUserIdAndOrderId(UUID userId, UUID orderId, Integer attempt);

    Optional<MatchedOrder> findByOrderId(UUID orderId);
}
