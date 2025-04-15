package com.exchange.matching.application.service;

import com.exchange.matching.application.command.CreateMatchingCommand;

public interface MatchingService {
    void matchOrders(CreateMatchingCommand command);
}
