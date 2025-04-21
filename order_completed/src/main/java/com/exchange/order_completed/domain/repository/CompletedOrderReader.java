package com.exchange.order_completed.domain.repository;

import com.exchange.order_completed.domain.entiry.CompletedOrder;

import java.util.UUID;

public interface CompletedOrderReader {

    CompletedOrder findByUserIdAndOrderId(UUID userId, UUID orderId);
}
