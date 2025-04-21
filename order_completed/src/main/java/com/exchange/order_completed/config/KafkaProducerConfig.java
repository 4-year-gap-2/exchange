package com.exchange.order_completed.config;

import com.exchange.order_completed.infrastructure.dto.KafkaOrderStoreEvent;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

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
    public ProducerFactory<String, KafkaOrderStoreEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        configProps.put("spring.json.trusted.packages", "com.exchange.order_completed.infrastructure.dto");

        configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        configProps.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        configProps.put(SaslConfigs.SASL_JAAS_CONFIG,
                "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username=\"" + kafkaName + "\" password=\"" + kafkaPassword + "\";");

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, KafkaOrderStoreEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
