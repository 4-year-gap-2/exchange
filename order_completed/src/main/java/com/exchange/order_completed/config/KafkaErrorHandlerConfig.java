package com.exchange.order_completed.config;

import com.exchange.order_completed.common.exception.DuplicateMatchedOrderInformationException;
import com.exchange.order_completed.common.exception.DuplicateUnmatchedOrderInformationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
public class KafkaErrorHandlerConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> template) {
        // 1초 간격으로 최대 3회 재시도
        FixedBackOff backOff = new FixedBackOff(1_000L, 3L);

        // 예외별 DLT 전송 여부 결정
        // 최대 재시도 횟수 초과 시 보상 트랜잭션 큐(Dead Letter Queue)로 메시지 이동
        // 3회 재시도 실패 시 order_completed-to-user.execute-order-info-save-compensation 토픽으로 publish
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(template,
                        (record, ex) -> {
                            // 1) 래퍼를 풀어서 실제 원인 확인
                            Throwable cause = ex.getCause() instanceof ListenerExecutionFailedException
                                    ? ex.getCause().getCause()
                                    : ex.getCause();

                            // 2) 특정 예외는 null을 반환하여 DLT 스킵
                            if (cause instanceof DuplicateMatchedOrderInformationException) {
                                return null;
                            }
                            if (cause instanceof DuplicateUnmatchedOrderInformationException) {
                                return null;
                            }
                            // 3) 그 외는 compensation 토픽으로 전송
                            return new TopicPartition(
                                    "order_completed-to-user.execute-order-info-save-compensation",
                                    record.partition()
                            );
                        }
                );

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // 특정 예외를 재시도하지 않도록 설정
        handler.addNotRetryableExceptions(DuplicateMatchedOrderInformationException.class);
        handler.addNotRetryableExceptions(DuplicateUnmatchedOrderInformationException.class);

        // 재시도가 모두 실패한 메시지의 오프셋을 커밋해 컨슈머 그룹 오프셋을 다음 메시지로 이동
        // 보상 트랜잭션 큐로 이동한 메시지는 다시 처리하지 않도록 설정
        handler.setCommitRecovered(true);
        handler.setAckAfterHandle(true);
        handler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.error("Retry #{} for record {} failed", deliveryAttempt, record, ex);
        });

        return handler;
    }
}
