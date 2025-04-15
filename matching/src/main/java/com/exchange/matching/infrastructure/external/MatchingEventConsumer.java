package com.exchange.matching.infrastructure.external;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.service.MatchingFacade;
import com.exchange.matching.infrastructure.dto.KafkaMatchingEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Component
public class MatchingEventConsumer {

    private final MatchingFacade matchingFacade;
    private final MeterRegistry meterRegistry;

    private final Counter processedCounter;
    private final Timer processingTimer;

    // 생성자를 통해 MeterRegistry 주입 및 메트릭 초기화
    public MatchingEventConsumer(MatchingFacade matchingFacade, MeterRegistry meterRegistry) {
        this.matchingFacade = matchingFacade;
        this.meterRegistry = meterRegistry;

        // Counter 초기화
        this.processedCounter = Counter.builder("matching_events_processed_total")
                .description("Total number of matching events processed")
                .register(meterRegistry);

        // Timer 초기화 (Summary 대신 Timer 사용 권장)
        this.processingTimer = Timer.builder("matching_processing_time")
                .description("Time taken to process matching events")
                .register(meterRegistry);
    }

    @KafkaListener(
            topics = {"matching-events-tps-v2"},
            groupId = "matching-service",
            concurrency = "3"  // 3개의 스레드로 병렬 처리
    )
    public void consumeV2(ConsumerRecord<String, KafkaMatchingEvent> record) {
        // 타이머 시작
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            String topic = record.topic();
            KafkaMatchingEvent event = record.value();

            System.out.println("topic 이름: " + topic);

            CreateMatchingCommand command = KafkaMatchingEvent.commandFromEvent(event);
            matchingFacade.matchV2(command);

            // 카운터 증가
            processedCounter.increment();
        } finally {
            // 타이머 종료 및 기록
            sample.stop(processingTimer);
        }
    }


    //    @RetryableTopic(
    //            attempts = "3", // 최대 3회 재시도
    //            backoff = @Backoff(delay = 10 * 1000, multiplier = 3, maxDelay = 10 * 60 * 1000), // 재시도 간격 설정
    //            include = IllegalArgumentException.class // 모든 예외에 대해 재시도
    //    )
    @KafkaListener(
            topics = {"matching-events-tps-v4"},
            groupId = "matching-service",
            concurrency = "3"  // 3개의 스레드로 병렬 처리
    )
    public void consumeV4(ConsumerRecord<String, KafkaMatchingEvent> record) {
        // 타이머 시작
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            String topic = record.topic();
            KafkaMatchingEvent event = record.value();

            System.out.println("topic 이름: " + topic);

            CreateMatchingCommand command = KafkaMatchingEvent.commandFromEvent(event);
            matchingFacade.matchV4(command);

            // 카운터 증가
            processedCounter.increment();
        } finally {
            // 타이머 종료 및 기록
            sample.stop(processingTimer);
        }
    }
}