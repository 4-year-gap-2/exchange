package com.exchange.matching.domain.service;

import com.exchange.matching.application.command.CreateMatchingCommand;

public interface MatchingService {
    void matchOrders(CreateMatchingCommand command);
}
