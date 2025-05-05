package com.exchange.order_completed.application.service;

import com.exchange.order_completed.domain.entity.MatchedOrder;
import com.exchange.order_completed.domain.repository.MatchedOrderReader;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
@Service
public class OrderCacheService {

    private final MatchedOrderReader matchedOrderReader;

    @Cacheable(value = "userOrders", key = "#userId")
    public List<MatchedOrder> getCachedOrders(UUID userId) {
        log.info(">>> 실제 DB에서 조회: {}", userId);
        return matchedOrderReader.findByUserId(userId);
    }
}
