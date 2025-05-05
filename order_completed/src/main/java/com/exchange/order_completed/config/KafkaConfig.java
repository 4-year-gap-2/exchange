package com.exchange.order_completed.config;

import com.exchange.order_completed.infrastructure.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;

/**
 * 특정 이벤트 타입에 대한 Kafka 설정 클래스
 */
@Configuration(enforceUniqueMethods = false)
public class KafkaConfig {

    private static final int DEFAULT_CONCURRENCY = 10;
    private static final int RECOVERY_CONCURRENCY = 2;

    private final KafkaCommonConfig kafkaCommonConfig;

    public KafkaConfig(KafkaCommonConfig kafkaCommonConfig) {
        this.kafkaCommonConfig = kafkaCommonConfig;
    }


    @Bean("chartKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, CompletedOrderChangeEvent> chartKafkaListenerContainerFactory() {
        return kafkaCommonConfig.createAutoCommitListenerFactory(
                new TypeReference<>() {
                }, "matching-service", DEFAULT_CONCURRENCY);
    }

    /**
     * 미체결 주문 이벤트 리스너 컨테이너 팩토리 (커스텀 에러 핸들러 적용)
     */
    @Bean("unmatchedOrderKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, KafkaUnmatchedOrderStoreEvent> unmatchedOrderKafkaListenerContainerFactory(DefaultErrorHandler errorHandler) {
        return kafkaCommonConfig.createManualCommitListenerFactory(
                new TypeReference<>() {
                }, "matching-service", DEFAULT_CONCURRENCY, errorHandler);
    }

    /**
     * 체결 주문 이벤트 리스너 컨테이너 팩토리 (커스텀 에러 핸들러 적용)
     */
    @Bean("matchedOrderKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, KafkaMatchedOrderStoreEvent> matchedOrderKafkaListenerContainerFactory(DefaultErrorHandler errorHandler) {
        return kafkaCommonConfig.createManualCommitListenerFactory(
                new TypeReference<>() {
                }, "matching-service", 20, errorHandler);
    }

    /**
     * 체결 주문 이벤트 리스너 컨테이너 팩토리
     */
    @Bean("defaultCompleteOrderKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, KafkaMatchedOrderStoreEvent> matchedOrderKafkaListenerContainerFactory() {
        return kafkaCommonConfig.createManualCommitListenerFactory(
                new TypeReference<>() {
                }, "matching-service", DEFAULT_CONCURRENCY);
    }

    /**
     * 복구 이벤트 리스너 컨테이너 팩토리 (커스텀 에러 핸들러 적용)
     */
    @Bean("recoveryEventKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> recoveryEventKafkaListenerContainerFactory(DefaultErrorHandler errorHandler) {
        return kafkaCommonConfig.createManualCommitListenerFactory(
                new TypeReference<>() {
                }, "recovery-service", RECOVERY_CONCURRENCY, errorHandler);
    }

    /**
     * 복구 이벤트 리스너 컨테이너 팩토리
     */
    @Bean("defaultRecoveryEventKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> recoveryEventKafkaListenerContainerFactory() {
        return kafkaCommonConfig.createManualCommitListenerFactory(
                new TypeReference<>() {
                }, "recovery-service", RECOVERY_CONCURRENCY);
    }

    /**
     * 제네릭 객체 전송용 프로듀서 템플릿
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(
                kafkaCommonConfig.createCustomProducerFactory(new TypeReference<>() {
                }));
    }

    /**
     * 자산 증가 이벤트 전용 프로듀서 템플릿
     */
    @Bean
    public KafkaTemplate<String, KafkaBalanceIncreaseEvent> balanceIncreaseKafkaTemplate() {
        return new KafkaTemplate<>(
                kafkaCommonConfig.createCustomProducerFactory(new TypeReference<>() {
                }));
    }

    /**
     * 주문 완료 이벤트 전용 프로듀서 템플릿
     */
    @Bean
    public KafkaTemplate<String, KafkaMatchedOrderStoreEvent> orderStoreKafkaTemplate() {
        return new KafkaTemplate<>(
                kafkaCommonConfig.createCustomProducerFactory(new TypeReference<>() {
                }));
    }
}