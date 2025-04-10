package com.exchange.matching.domain.service;

import com.exchange.matching.infrastructure.dto.KafkaMatchingEvent;

public interface MatchingService {
    void matchOrders(KafkaMatchingEvent event);
}
