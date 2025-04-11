package com.exchange.matching.domain.service;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.infrastructure.dto.KafkaMatchingEvent;

public interface MatchingService {
    void matchOrders(CreateMatchingCommand command);
}
