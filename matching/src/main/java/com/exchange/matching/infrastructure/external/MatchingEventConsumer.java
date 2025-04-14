package com.exchange.matching.infrastructure.external;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.service.MatchingFacade;
import com.exchange.matching.common.aop.TimeTrace;
import com.exchange.matching.domain.service.MatchingService;
import com.exchange.matching.domain.service.MatchingServiceV2;
import com.exchange.matching.infrastructure.dto.KafkaMatchingEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component

public class MatchingEventConsumer {

    private final MatchingFacade matchingFacade;

    public MatchingEventConsumer(MatchingFacade matchingFacade) {
        this.matchingFacade = matchingFacade;
    }


    @RetryableTopic(
            attempts = "3", // 최대 3회 재시도
            backoff = @Backoff(delay = 10 * 1000, multiplier = 3, maxDelay = 10 * 60 * 1000), // 재시도 간격 설정
            include = IllegalArgumentException.class // 모든 예외에 대해 재시도
    )
    @KafkaListener(topics = {"matching-events"}, groupId = "matching-service")
    public void consume(ConsumerRecord<String, KafkaMatchingEvent> record) {
        String topic = record.topic();
        KafkaMatchingEvent event = record.value();

        System.out.println("topic 이름: " + topic);

        CreateMatchingCommand command = KafkaMatchingEvent.commandFromEvent(event);
        matchingFacade.match(command);
    }
}