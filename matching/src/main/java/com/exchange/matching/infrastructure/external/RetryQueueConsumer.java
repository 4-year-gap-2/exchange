package com.exchange.matching.infrastructure.external;

import com.exchange.matching.infrastructure.dto.KafkaMatchingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryQueueConsumer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ReceiveServerHealthMonitor healthMonitor;
    private final KafkaListenerEndpointRegistry registry;

    @KafkaListener(
            topics = "matching-to-matching.execute-receiver-unavailable.retry",
            containerFactory = "retryQueueListenerContainerFactory",
            id = "retry-queue-consumer"
    )
    public void consume(ConsumerRecord<String, KafkaMatchingEvent> record) {
        // 서버가 아직 다운되어 있으면 처리하지 않음
        if (!healthMonitor.isHealthy()) {
            log.debug("Receive 서버가 여전히 다운중. 재시도 처리");
            return;
        }

        try {
            // 원본 토픽으로 재전송
            kafkaTemplate.send("user-to-matching.execute-order-delivery.v6d",
                    record.key(),
                    record.value());

            log.info("재시도 메시지를 원래 메시지로 복구");
        } catch (Exception e) {
            log.error("재시도 메시지 처리 실패", e);
        }
    }

    public void startProcessing() {
        registry.getListenerContainer("retry-queue-consumer").start();
        log.info("재시도 큐 시작");
    }

    public void stopProcessing() {
        registry.getListenerContainer("retry-queue-consumer").stop();
        log.info("재시도 큐 중지");
    }
}
