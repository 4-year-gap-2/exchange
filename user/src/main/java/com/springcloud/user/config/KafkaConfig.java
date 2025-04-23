package com.springcloud.user.config;


import com.fasterxml.jackson.core.type.TypeReference;
import com.springcloud.user.infrastructure.dto.KafkaOrderFormEvent;
import com.springcloud.user.infrastructure.dto.KafkaUserBalanceDecreaseEvent;
import com.springcloud.user.infrastructure.dto.KafkaUserBalanceIncreaseEvent;
import com.springcloud.user.infrastructure.dto.MatchCompensatorEvent;
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

    // 주문 시 자산 감소
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaUserBalanceDecreaseEvent> orderEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, KafkaUserBalanceDecreaseEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                kafkaCommonConfig.createCustomConsumerFactory(new TypeReference<>() {
                }, "user-service")
        );

        return factory;
    }

    // 체결 시 자산 증가
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaUserBalanceIncreaseEvent> matchingEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, KafkaUserBalanceIncreaseEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                kafkaCommonConfig.createCustomConsumerFactory(new TypeReference<>() {
                }, "user-service")
        );

        return factory;
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
        ConcurrentKafkaListenerContainerFactory<String, MatchCompensatorEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                kafkaCommonConfig.createCustomConsumerFactory(new TypeReference<>() {
                }, "user-service")
        );

        return factory;
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