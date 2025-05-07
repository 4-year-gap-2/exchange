package com.exchange.order_completed.domain.repository;

import com.exchange.order_completed.domain.cassandra.entity.UnmatchedOrder;

import java.util.UUID;

public interface UnmatchedOrderReader {

    UnmatchedOrder findUnmatchedOrder(UUID userId, UUID orderId, Integer attempt);
}
