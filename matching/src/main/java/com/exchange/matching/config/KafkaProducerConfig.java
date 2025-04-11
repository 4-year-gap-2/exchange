package com.exchange.matching.config;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.infrastructure.dto.KafkaMatchingEvent;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.host}")
    private String kafkaHost;

    @Value("${spring.kafka.username}")
    private String kafkaName;

    @Value("${spring.kafka.password}")
    private String kafkaPassword;

    @Bean
    public ProducerFactory<String, KafkaMatchingEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaMatchingEvent.class);

        // SASL 인증 관련 설정 추가
        configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        configProps.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        configProps.put(SaslConfigs.SASL_JAAS_CONFIG,
                "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username=\"" + kafkaName + "\" password=\"" + kafkaPassword + "\";");

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, KafkaMatchingEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}