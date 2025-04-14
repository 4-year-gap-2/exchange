package com.exchange.matching.infrastructure.external;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.service.MatchingFacade;
import com.exchange.matching.infrastructure.dto.KafkaMatchingEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchingEventConsumer {

    private final MatchingFacade matchingFacade;


//    @RetryableTopic(
//            attempts = "3", // 최대 3회 재시도
//            backoff = @Backoff(delay = 10 * 1000, multiplier = 3, maxDelay = 10 * 60 * 1000), // 재시도 간격 설정
//            include = IllegalArgumentException.class // 모든 예외에 대해 재시도
//    )
    @KafkaListener(topics = {"matching-events"}, groupId = "matching-service")
    public void consume(ConsumerRecord<String, KafkaMatchingEvent> record, Acknowledgment acknowledgment) {
        String topic = record.topic();
        KafkaMatchingEvent event = record.value();

        System.out.println("topic 이름: " + topic);

        CreateMatchingCommand command = KafkaMatchingEvent.commandFromEvent(event);
        String value = matchingFacade.match(command);
        acknowledgment.acknowledge();
    }
}