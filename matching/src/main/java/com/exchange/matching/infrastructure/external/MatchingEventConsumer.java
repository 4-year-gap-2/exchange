package com.exchange.matching.infrastructure.external;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.domain.service.MatchingService;
import com.exchange.matching.infrastructure.dto.KafkaMatchingEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MatchingEventConsumer {

    private final MatchingService matchingService;

    public MatchingEventConsumer(MatchingService matchingServiceV3) {
        this.matchingService = matchingServiceV3;
    }

    @KafkaListener(topics = "matching-events", groupId = "matching-service")
    public void consume(KafkaMatchingEvent event) {
        CreateMatchingCommand command = KafkaMatchingEvent.commandFromEvent(event);
        matchingService.matchOrders(command);
    }
}