package com.exchange.matching.application.service;

import com.exchange.matching.application.command.CreateMatchingCommand;

public interface MatchingService {
    String matchOrders(CreateMatchingCommand command);
}
