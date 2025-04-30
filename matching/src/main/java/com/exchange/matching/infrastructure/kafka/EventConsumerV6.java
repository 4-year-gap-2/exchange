package com.exchange.matching.infrastructure.kafka;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventConsumerV6 {

    private final MeterRegistry meterRegistry;

    private final Counter processedCounter;
    private final Timer processingTimer;

    // 생성자를 통해 MeterRegistry 주입 및 메트릭 초기화
    public EventConsumerV6(MeterRegistry meterRegistry) {
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
            topics = {"matching-to-matching.execute-matching-event-callback"},
            containerFactory = "recoveryEventKafkaListenerContainerFactory",
            concurrency = "3"
    )
    public void matchingCallback(ConsumerRecord<String, String> record, Acknowledgment ack) {
        // 타이머 시작
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            String key = record.key();
            String value = record.value();

            log.info("메시지 수신: key={}, value={}", key, value);

            // 카운터 증가
            processedCounter.increment();

            ack.acknowledge();

            // 필요한 경우 JSON을 파싱하여 객체로 변환
            // Map<String, Object> data = objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {});
        } finally {
            // 타이머 종료 및 기록
            sample.stop(processingTimer);
        }
    }

    @KafkaListener(
            topics = {"matching-to-matching.execute-unmatched-event-callback"},
            containerFactory = "recoveryEventKafkaListenerContainerFactory",
            concurrency = "3"
    )
    public void unMatchingCallback(ConsumerRecord<String, String> record, Acknowledgment ack) {
        // 타이머 시작
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            String key = record.key();
            String value = record.value();

            log.info("메시지 수신: key={}, value={}", key, value);

            // 카운터 증가
            processedCounter.increment();

            ack.acknowledge();

            // 필요한 경우 JSON을 파싱하여 객체로 변환
            // Map<String, Object> data = objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {});
        } finally {
            // 타이머 종료 및 기록
            sample.stop(processingTimer);
        }
    }
}