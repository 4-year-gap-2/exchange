package com.exchange.order_completed.domain.repository;

import com.exchange.order_completed.domain.entity.UnmatchedOrder;

import java.util.UUID;

public interface UnmatchedOrderReader {

    UnmatchedOrder findByUserIdAndOrderId(UUID userId, UUID orderId, Integer attempt);

}
