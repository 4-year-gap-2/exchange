package com.springcloud.user.config;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.user.util.CustomJsonDeserializer;
import com.springcloud.user.util.CustomJsonSerializer;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;

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

    public KafkaCommonConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 기본 Producer 설정 맵
     */
    public Map<String, Object> getBaseProducerConfig() {
        Map<String, Object> configProps = new HashMap<>();

        // 기본 설정
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":9092");

        // SASL 인증 관련 설정 추가
        addSaslConfig(configProps);

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
     * 기본 Consumer 설정 맵
     */
    public Map<String, Object> getBaseConsumerConfig(String groupId) {
        Map<String, Object> configProps = new HashMap<>();

        // 기본 설정
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":9092");
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // SASL 인증 관련 설정 추가
        addSaslConfig(configProps);

        return configProps;
    }

    /**
     * 커스텀 역직렬화기를 사용하는 Consumer Factory 생성
     */
    public <T> ConsumerFactory<String, T> createCustomConsumerFactory(TypeReference<T> typeReference, String groupId) {
        Map<String, Object> configProps = getBaseConsumerConfig(groupId);
        return new DefaultKafkaConsumerFactory<>(
                configProps,
                new StringDeserializer(),
                new CustomJsonDeserializer<>(objectMapper, typeReference)
        );
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
