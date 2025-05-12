package com.exchange.matching.infrastructure.external;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.enums.MatchingVersion;
import com.exchange.matching.application.service.MatchingApplicationService;
import com.exchange.matching.infrastructure.dto.KafkaMatchingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.exchange.matching.util.MetricsCollector;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {
    private final MatchingApplicationService matchingService;
    private final MetricsCollector metricsCollector;

    @KafkaListener(
            topics = {
                    "user-to-matching.execute-order-delivery.v1a",
                    "user-to-matching.execute-order-delivery.v1b",
                    "user-to-matching.execute-order-delivery.v2",
                    "user-to-matching.execute-order-delivery.v3",
                    "user-to-matching.execute-order-delivery.v4",
                    "user-to-matching.execute-order-delivery.v5",
                    "user-to-matching.execute-order-delivery.v6a",
                    "user-to-matching.execute-order-delivery.v6b",
                    "user-to-matching.execute-order-delivery.v6c",
                    "user-to-matching.execute-order-delivery.v6d"
            },
            containerFactory = "orderDeliveryKafkaListenerContainerFactory",
            concurrency = "3"
    )
    public void consume(ConsumerRecord<String, KafkaMatchingEvent> record) {
        String topic = record.topic();
        MatchingVersion version = extractVersionFromTopic(topic);

        metricsCollector.recordProcessing(() -> {
            KafkaMatchingEvent event = record.value();
            CreateMatchingCommand command = KafkaMatchingEvent.commandFromEvent(event);
            matchingService.processMatching(command, version);
            log.info("Processed event from topic: {}, version: {}", topic, version);
        }, version);
    }

    private MatchingVersion extractVersionFromTopic(String topic) {
        String versionCode = topic.substring(topic.lastIndexOf('.') + 1);
        return MatchingVersion.fromCode(versionCode);
    }
}