package com.exchange.order.config;


import com.exchange.order.infrastructure.dto.KafkaOrderCancelEvent;
import com.exchange.order.infrastructure.dto.KafkaUserBalanceDecreaseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    private final KafkaCommonConfig kafkaCommonConfig;


    public KafkaConfig(KafkaCommonConfig kafkaCommonConfig) {
        this.kafkaCommonConfig = kafkaCommonConfig;
    }

    @Bean
    public KafkaTemplate<String, KafkaUserBalanceDecreaseEvent> balanceEventKafkaTemplate() {
        ProducerFactory<String, KafkaUserBalanceDecreaseEvent> factory =
                kafkaCommonConfig.createCustomProducerFactory(new TypeReference<>() {
                });
        return new KafkaTemplate<>(factory);
    }

    //주문 취소 큐 주문 -> 매칭
    @Bean
    public KafkaTemplate<String, KafkaOrderCancelEvent> cancelEventKafkaTemplate () {
        ProducerFactory<String, KafkaOrderCancelEvent> factory =
                kafkaCommonConfig.createCustomProducerFactory(new TypeReference<>() {
                });
        return new KafkaTemplate<>(factory);
    }
}