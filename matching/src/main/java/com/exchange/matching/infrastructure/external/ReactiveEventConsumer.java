package com.exchange.matching.infrastructure.external;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.enums.MatchingVersion;
import com.exchange.matching.application.service.MatchingApplicationService;
import com.exchange.matching.infrastructure.dto.KafkaMatchingEvent;
import com.exchange.matching.util.MetricsCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveEventConsumer {
    private final MatchingApplicationService matchingService;
    private final MetricsCollector metricsCollector;
    private final ReceiveServerHealthMonitor healthMonitor;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaReceiver<String, KafkaMatchingEvent> kafkaReceiver;

    private final AtomicBoolean running = new AtomicBoolean(true);

    @EventListener(ApplicationStartedEvent.class)
    public void startConsumer() {
        log.info("Starting reactive Kafka consumer");

        kafkaReceiver.receive()
                .publishOn(Schedulers.boundedElastic())
                .filter(record -> {
                    if (!running.get()) {
                        record.receiverOffset().acknowledge();
                        return false;
                    }
                    return true;
                })
                .flatMap(this::processRecord)
                .subscribe(
                        success -> {},
                        error -> log.error("Error in Kafka stream processing", error),
                        () -> log.info("Kafka consumer completed")
                );
    }

    private Mono<Void> processRecord(ReceiverRecord<String, KafkaMatchingEvent> record) {
        return Mono.defer(() -> {
//            if (!healthMonitor.isHealthy()) {
//                log.info("Receive 서버 상태 이상 감지: 처리 불가 메시지를 재시도 큐로 전송합니다.");
//                kafkaTemplate.send("matching-to-matching.execute-receiver-unavailable.retry", record.key(), record.value());
//                return Mono.just(record.receiverOffset())
//                        .doOnNext(ReceiverOffset::acknowledge)
//                        .then();
//            }

            String topic = record.topic();
            MatchingVersion version = extractVersionFromTopic(topic);
            KafkaMatchingEvent event = record.value();
            CreateMatchingCommand command = KafkaMatchingEvent.commandFromEvent(event);

            // V6D 버전만 리액티브하게 처리
            if (version == MatchingVersion.V6D) {
                log.info("V6D 버전 감지: 리액티브 처리 사용");
                return Mono.from(metricsCollector.recordProcessingReactive(() ->
                                matchingService.processMatchingReactive(command, version), version))
                        .then(Mono.fromRunnable(() -> record.receiverOffset().acknowledge()))
                        .then();
            } else {
                // 나머지 버전은 기존 동기 방식으로 처리
                log.info("V6D 외 버전 감지: 동기 처리 사용");
                return Mono.fromRunnable(() -> {
                    metricsCollector.recordProcessing(() -> {
                        matchingService.processMatching(command, version);
                    }, version);
                    record.receiverOffset().acknowledge();
                }).then();
            }
        });
    }

    private MatchingVersion extractVersionFromTopic(String topic) {
        String versionCode = topic.substring(topic.lastIndexOf('.') + 1);
        return MatchingVersion.fromCode(versionCode);
    }
}