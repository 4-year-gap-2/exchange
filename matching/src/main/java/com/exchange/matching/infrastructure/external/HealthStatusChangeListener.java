package com.exchange.matching.infrastructure.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthStatusChangeListener {

    private final RetryQueueConsumer retryQueueConsumer;

    @EventListener
    public void handleHealthStatusChange(HealthStatusChangeEvent event) {
        if (event.isHealthy()) {
            log.info("Receive 서버 정상 retry 대기열 처리 시작");
            retryQueueConsumer.startProcessing();
        } else {
            log.info("Receive 서버 종료 retry 대기열 처리 중지");
            retryQueueConsumer.stopProcessing();
        }
    }
}