package com.exchange.order.config;


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


//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, KafkaUserBalanceIncreaseEvent> matchingEventKafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, KafkaUserBalanceIncreaseEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
//
//        factory.setConsumerFactory(
//                kafkaCommonConfig.createCustomConsumerFactory(new TypeReference<>() {
//                }, "user-service")
//        );
//
//        return factory;
//    }

    @Bean
    public KafkaTemplate<String, KafkaUserBalanceDecreaseEvent> BalanceEventKafkaTemplate() {
        ProducerFactory<String, KafkaUserBalanceDecreaseEvent> factory =
                kafkaCommonConfig.createCustomProducerFactory(new TypeReference<>() {
                });
        return new KafkaTemplate<>(factory);
    }

//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, MatchCompensatorEvent> matchingCompensatorEventKafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, MatchCompensatorEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
//
//        factory.setConsumerFactory(
//                kafkaCommonConfig.createCustomConsumerFactory(new TypeReference<>() {
//                }, "user-service")
//        );
//
//        return factory;
//    }
//
//    @Bean
//    public KafkaTemplate<String, MatchCompensatorEvent> matchingCompensatorEventKafkaTemplate() {
//        ProducerFactory<String, MatchCompensatorEvent> factory =
//                kafkaCommonConfig.createCustomProducerFactory(new TypeReference<>() {
//                });
//        return new KafkaTemplate<>(factory);
//    }
}