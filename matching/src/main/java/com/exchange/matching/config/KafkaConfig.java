package com.exchange.matching.config;

import com.exchange.matching.infrastructure.dto.KafkaMatchingEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    private final KafkaCommonConfig kafkaCommonConfig;

    public KafkaConfig(KafkaCommonConfig kafkaCommonConfig) {
        this.kafkaCommonConfig = kafkaCommonConfig;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaMatchingEvent> matchingEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, KafkaMatchingEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                kafkaCommonConfig.createCustomConsumerFactory(new TypeReference<>() {
                }, "matching-service")
        );

        return factory;
    }

    @Bean
    public KafkaTemplate<String, KafkaMatchingEvent> matchingEventKafkaTemplate() {
        ProducerFactory<String, KafkaMatchingEvent> factory =
                kafkaCommonConfig.createCustomProducerFactory(new TypeReference<>() {
                });
        return new KafkaTemplate<>(factory);
    }
}