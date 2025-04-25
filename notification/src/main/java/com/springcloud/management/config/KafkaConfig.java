package com.springcloud.management.config;


import com.fasterxml.jackson.core.type.TypeReference;
import com.springcloud.management.infrastructure.external.dto.NotificationKafkaEvent;
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
    public ConcurrentKafkaListenerContainerFactory<String, NotificationKafkaEvent> matchingEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, NotificationKafkaEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                kafkaCommonConfig.createCustomConsumerFactory(new TypeReference<>() {
                }, "user-service")
        );

        return factory;
    }

    @Bean
    public KafkaTemplate<String, NotificationKafkaEvent> matchingEventKafkaTemplate() {
        ProducerFactory<String, NotificationKafkaEvent> factory =
                kafkaCommonConfig.createCustomProducerFactory(new TypeReference<>() {
                });
        return new KafkaTemplate<>(factory);
    }

}