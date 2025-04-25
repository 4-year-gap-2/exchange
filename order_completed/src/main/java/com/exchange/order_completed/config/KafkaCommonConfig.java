package com.exchange.order_completed.config;

import com.exchange.order_completed.common.exception.DuplicateOrderCompletionException;
import com.exchange.order_completed.infrastructure.dto.KafkaOrderStoreEvent;
import com.exchange.order_completed.util.CustomJsonDeserializer;
import com.exchange.order_completed.util.CustomJsonSerializer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaCommonConfig {

    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.host}")
    private String kafkaHost;

    @Value("${spring.kafka.username}")
    private String kafkaName;

    @Value("${spring.kafka.password}")
    private String kafkaPassword;

    @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    public KafkaCommonConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 기본 Producer 설정 맵
     */
    public Map<String, Object> getBaseProducerConfig() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // 안정적인 메시지 전송을 위한 설정 추가
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // SASL 인증 관련 설정 추가
        addSaslConfig(configProps);
        return configProps;
    }

    /**
     * 기본 Consumer 설정 맵
     */
    public Map<String, Object> getBaseConsumerConfig(String groupId) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":9092");
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // SASL 인증 관련 설정 추가
        addSaslConfig(configProps);
        return configProps;
    }

    /**
     * 수동 커밋 방식의 Consumer 설정 맵 생성
     */
    public Map<String, Object> getManualCommitConsumerConfig(String groupId) {
        Map<String, Object> configProps = getBaseConsumerConfig(groupId);
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        return configProps;
    }

    /**
     * 자동 커밋 방식의 Consumer 설정 맵 생성
     */
    public Map<String, Object> getAutoCommitConsumerConfig(String groupId) {
        Map<String, Object> configProps = getBaseConsumerConfig(groupId);
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        configProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 5000); // 자동 커밋 주기 설정 (5초)
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        return configProps;
    }

    /**
     * 커스텀 직렬화기를 사용하는 Producer Factory 생성
     */
    public <T> ProducerFactory<String, T> createCustomProducerFactory(TypeReference<T> typeReference) {
        Map<String, Object> configProps = getBaseProducerConfig();
        return new DefaultKafkaProducerFactory<>(
                configProps,
                new StringSerializer(),
                new CustomJsonSerializer<>(objectMapper, typeReference)
        );
    }

    /**
     * 커스텀 역직렬화기를 사용하는 Consumer Factory 생성 (수동 커밋)
     */
    public <T> ConsumerFactory<String, T> createManualCommitConsumerFactory(TypeReference<T> typeReference, String groupId) {
        Map<String, Object> configProps = getManualCommitConsumerConfig(groupId);
        return new DefaultKafkaConsumerFactory<>(
                configProps,
                new StringDeserializer(),
                new CustomJsonDeserializer<>(objectMapper, typeReference)
        );
    }

    /**
     * 커스텀 역직렬화기를 사용하는 Consumer Factory 생성 (자동 커밋)
     */
    public <T> ConsumerFactory<String, T> createAutoCommitConsumerFactory(TypeReference<T> typeReference, String groupId) {
        Map<String, Object> configProps = getAutoCommitConsumerConfig(groupId);
        return new DefaultKafkaConsumerFactory<>(
                configProps,
                new StringDeserializer(),
                new CustomJsonDeserializer<>(objectMapper, typeReference)
        );
    }

    /**
     * 수동 커밋 리스너 컨테이너 팩토리 생성 - 재사용 가능한 메서드
     */
    public <T> ConcurrentKafkaListenerContainerFactory<String, T> createManualCommitListenerFactory(
            TypeReference<T> typeReference, String groupId, int concurrency, DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createManualCommitConsumerFactory(typeReference, groupId));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setConcurrency(concurrency);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    /**
     * 자동 커밋 리스너 컨테이너 팩토리 생성 - 재사용 가능한 메서드
     */
    public <T> ConcurrentKafkaListenerContainerFactory<String, T> createAutoCommitListenerFactory(
            TypeReference<T> typeReference, String groupId, int concurrency, DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createAutoCommitConsumerFactory(typeReference, groupId));
        factory.setConcurrency(concurrency);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    /**
     * SASL 인증 설정을 추가
     */
    private void addSaslConfig(Map<String, Object> configProps) {
        configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        configProps.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        configProps.put(SaslConfigs.SASL_JAAS_CONFIG,
                "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username=\"" + kafkaName + "\" password=\"" + kafkaPassword + "\";");
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, KafkaOrderStoreEvent> template) {
        // 1초 간격으로 최대 3회 재시도
        FixedBackOff backOff = new FixedBackOff(1_000L, 3L);

        // 예외별 DLT 전송 여부 결정
        // 최대 재시도 횟수 초과 시 보상 트랜잭션 큐(Dead Letter Queue)로 메시지 이동
        // 3회 재시도 실패 시 order_completed-to-user.execute-order-info-save-compensation 토픽으로 publish
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(template,
                        (record, ex) -> {
                            // 1) 래퍼를 풀어서 실제 원인 확인
                            Throwable cause = ex.getCause() instanceof ListenerExecutionFailedException
                                    ? ex.getCause().getCause()
                                    : ex.getCause();

                            // 2) DuplicateOrderCompletionException 이면 DLT 스킵
                            if (cause instanceof DuplicateOrderCompletionException) {
                                return null;
                            }
                            // 3) 그 외는 compensation 토픽으로 전송
                            return new TopicPartition(
                                    "order_completed-to-user.execute-order-info-save-compensation",
                                    record.partition()
                            );
                        }
                );

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // 특정 예외를 재시도하지 않도록 설정
        handler.addNotRetryableExceptions(DuplicateOrderCompletionException.class);

        // 재시도가 모두 실패한 메시지의 오프셋을 커밋해 컨슈머 그룹 오프셋을 다음 메시지로 이동
        // 보상 트랜잭션 큐로 이동한 메시지는 다시 처리하지 않도록 설정
        handler.setCommitRecovered(true);
        handler.setAckAfterHandle(true);
        handler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.error("Retry #{} for record {} failed", deliveryAttempt, record, ex);
        });

        return handler;
    }
}