package com.exchange.order_completed.config;

import com.exchange.order_completed.util.CustomJsonDeserializer;
import com.exchange.order_completed.util.CustomJsonSerializer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;

import java.util.HashMap;
import java.util.Map;

@Configuration
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
            TypeReference<T> typeReference, String groupId, int concurrency) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createManualCommitConsumerFactory(typeReference, groupId));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setConcurrency(concurrency);
        return factory;
    }

    /**
     * 수동 커밋 리스너 컨테이너 팩토리 생성 (커스텀 에러 핸들러 적용, 헤더 활성화) - 재사용 가능한 메서드
     */
    public <T> ConcurrentKafkaListenerContainerFactory<String, T> createManualCommitListenerFactory(
            TypeReference<T> typeReference, String groupId, int concurrency, DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createManualCommitConsumerFactory(typeReference, groupId));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setConcurrency(concurrency);
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setDeliveryAttemptHeader(true);
        return factory;
    }

    /**
     * 자동 커밋 리스너 컨테이너 팩토리 생성 - 재사용 가능한 메서드
     */
    public <T> ConcurrentKafkaListenerContainerFactory<String, T> createAutoCommitListenerFactory(
            TypeReference<T> typeReference, String groupId, int concurrency) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createAutoCommitConsumerFactory(typeReference, groupId));
        factory.setConcurrency(concurrency);
        return factory;
    }

    /**
     * 자동 커밋 리스너 컨테이너 팩토리 생성 (커스텀 에러 핸들러 적용) - 재사용 가능한 메서드
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
}