package com.springcloud.user.config;


import com.fasterxml.jackson.core.type.TypeReference;
import com.springcloud.user.infrastructure.dto.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    private static final int DEFAULT_CONCURRENCY = 3;
    private static final int RECOVERY_CONCURRENCY = 2;

    private final KafkaCommonConfig kafkaCommonConfig;


    public KafkaConfig(KafkaCommonConfig kafkaCommonConfig) {
        this.kafkaCommonConfig = kafkaCommonConfig;
    }

    // 주문 시 자산 감소
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaUserBalanceDecreaseEvent> balanceDecreaseKafkaListenerContainerFactory() {

        return kafkaCommonConfig.createManualCommitListenerFactory(new TypeReference<>() {}, "user-service",DEFAULT_CONCURRENCY);
    }

    // 체결 시 자산 증가
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaUserBalanceIncreaseEvent> matchingEventKafkaListenerContainerFactory() {
        return kafkaCommonConfig.createAutoCommitListenerFactory(new TypeReference<>() {}, "user-service",DEFAULT_CONCURRENCY);



    }

    // 유저 -> 자산감소 실패 큐
    @Bean
    public KafkaTemplate<String, KafkaInsufficientBalanceEvent> insufficientBalanceEventKafkaTemplate() { //KafkaTemplate 빈 메서드명은 소문자로 시작하도록 변경
        ProducerFactory<String, KafkaInsufficientBalanceEvent> factory =
                kafkaCommonConfig.createCustomProducerFactory(new TypeReference<>() {
                });
        return new KafkaTemplate<>(factory);
    }

    // 유저 -> 매칭 주문서 전달
    @Bean
    public KafkaTemplate<String, KafkaOrderFormEvent> orederEventKafkaTemplate() { //KafkaTemplate 빈 메서드명은 소문자로 시작하도록 변경
        ProducerFactory<String, KafkaOrderFormEvent> factory =
                kafkaCommonConfig.createCustomProducerFactory(new TypeReference<>() {
                });
        return new KafkaTemplate<>(factory);
    }

    // 체결 실패 보상
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MatchCompensatorEvent> matchingCompensatorEventKafkaListenerContainerFactory() {
        return kafkaCommonConfig.createAutoCommitListenerFactory(new TypeReference<>() {}, "user-service",DEFAULT_CONCURRENCY);
    }

    // 사용 안 함
    @Bean
    public KafkaTemplate<String, MatchCompensatorEvent> matchingCompensatorEventKafkaTemplate() {
        ProducerFactory<String, MatchCompensatorEvent> factory =
                kafkaCommonConfig.createCustomProducerFactory(new TypeReference<>() {
                });
        return new KafkaTemplate<>(factory);
    }
}