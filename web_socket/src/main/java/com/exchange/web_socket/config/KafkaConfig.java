package com.exchange.web_socket.config;

import com.exchange.web_socket.infrastructure.dto.CompletedOrderChangeEvent;
import com.exchange.web_socket.infrastructure.dto.MessageEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

/**
 * 특정 이벤트 타입에 대한 Kafka 설정 클래스
 */
@Configuration(enforceUniqueMethods = false)
public class KafkaConfig {

    private static final int DEFAULT_CONCURRENCY = 10;

    private final KafkaCommonConfig kafkaCommonConfig;

    public KafkaConfig(KafkaCommonConfig kafkaCommonConfig) {
        this.kafkaCommonConfig = kafkaCommonConfig;
    }


    @Bean("messageKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, CompletedOrderChangeEvent> messageKafkaListenerContainerFactory() {

        return kafkaCommonConfig.createAutoCommitListenerFactory(
                new TypeReference<>() {
                }, "web_socket-service", DEFAULT_CONCURRENCY);
    }

    @Bean("messageBalanceKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, MessageEvent> messageBalanceKafkaListenerContainerFactory() {

        return kafkaCommonConfig.createAutoCommitListenerFactory(
                new TypeReference<>() {
                }, "web_socket-service", DEFAULT_CONCURRENCY);
    }
}