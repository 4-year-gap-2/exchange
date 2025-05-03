package com.exchange.receive.config;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * 특정 이벤트 타입에 대한 Kafka 설정 클래스
 */
@Configuration(enforceUniqueMethods = false)
public class KafkaConfig {

    private final KafkaCommonConfig kafkaCommonConfig;

    public KafkaConfig(KafkaCommonConfig kafkaCommonConfig) {
        this.kafkaCommonConfig = kafkaCommonConfig;
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
}