package com.exchange.matching.application.service;

import com.exchange.matching.application.command.CreateMatchingCommand;
import org.springframework.stereotype.Service;

@Service
public class MatchingServiceV1 implements MatchingService {
    @Override
    public String matchOrders(CreateMatchingCommand command) {

        return null;
    }
}
