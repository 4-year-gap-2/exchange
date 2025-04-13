package com.exchange.matching.infrastructure.external;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.service.MatchingFacade;
import com.exchange.matching.domain.service.MatchingService;
import com.exchange.matching.domain.service.MatchingServiceV2;
import com.exchange.matching.infrastructure.dto.KafkaMatchingEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component

public class MatchingEventConsumer {

    private final MatchingServiceV2 matchingService;
    private final MatchingFacade matchingFacade;

    public MatchingEventConsumer(MatchingServiceV2 matchingServiceV3, MatchingFacade matchingFacade) {
        this.matchingService = matchingServiceV3;
        this.matchingFacade = matchingFacade;
    }

    @KafkaListener(topics = "matching-events", groupId = "matching-service")
    public void consume(KafkaMatchingEvent event) {
        CreateMatchingCommand command = KafkaMatchingEvent.commandFromEvent(event);
        matchingFacade.match(command);
    }
}