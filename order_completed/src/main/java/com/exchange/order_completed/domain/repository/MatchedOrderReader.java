package com.exchange.order_completed.domain.repository;

import com.exchange.order_completed.domain.entity.MatchedOrder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchedOrderReader {

    MatchedOrder findMatchedOrder(UUID userId, LocalDate yearMonthDate, UUID idempotencyId, Integer attempt);

    Optional<MatchedOrder> findByOrderId(UUID orderId);

    List<MatchedOrder> findByUserIdAndYearMonthDate(UUID userId, LocalDate yearMonthDate);
}
