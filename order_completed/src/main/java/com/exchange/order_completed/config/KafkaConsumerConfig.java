package com.exchange.order_completed.config;

import com.exchange.order_completed.infrastructure.dto.KafkaOrderStoreEvent;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.host}")
    private String kafkaHost;

    @Value("${spring.kafka.username}")
    private String kafkaName;

    @Value("${spring.kafka.password}")
    private String kafkaPassword;

    @Bean
    public ConsumerFactory<String, KafkaOrderStoreEvent> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":9092");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.exchange.order_completed.infrastructure");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.exchange.order_completed.infrastructure.dto.KafkaOrderStoreEvent");

        configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        configProps.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        configProps.put(SaslConfigs.SASL_JAAS_CONFIG,
                "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username=\"" + kafkaName + "\" password=\"" + kafkaPassword + "\";");

        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), new JsonDeserializer<>(KafkaOrderStoreEvent.class));
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, KafkaOrderStoreEvent> template) {
        // 1초 간격으로 최대 3회 재시도
        FixedBackOff backOff = new FixedBackOff(1_000L, 3L);

        // 3회 재시도 실패 시 4yearGap.match.OrderCompletedCompensatorEvent.compensation 토픽으로 publish
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template, (record, ex) ->
                new TopicPartition("4yearGap.match.OrderCompletedCompensatorEvent.compensation", record.partition())
        );

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        // 특정 예외는 재시도하지 않도록 설정 가능
        // handler.addNotRetryableExceptions(BadRequestException.class);

        // 재시도가 모두 실패한 메시지의 오프셋을 커밋해 컨슈머 그룹 오프셋을 다음 메시지로 이동
        // 보상 트랜잭션 큐로 이동한 메시지는 다시 처리하지 않도록 설정
        handler.setCommitRecovered(true);
        handler.setAckAfterHandle(true);

        return handler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaOrderStoreEvent> kafkaListenerContainerFactory(DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, KafkaOrderStoreEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
//        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}