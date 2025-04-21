package com.exchange.order_completed.config;

import com.exchange.order_completed.infrastructure.dto.KafkaBalanceIncreaseEvent;
import com.exchange.order_completed.infrastructure.dto.KafkaOrderStoreEvent;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
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

    // 공통 설정
    @Bean
    public Map<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // 이벤트 클래스 경로
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.exchange.order_completed.infrastructure.dto");

        configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        configProps.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        configProps.put(SaslConfigs.SASL_JAAS_CONFIG,
                "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username=\"" + kafkaName + "\" password=\"" + kafkaPassword + "\";");

        return configProps;
    }

    // OrderStoreEvent용
    @Bean
    public ProducerFactory<String, KafkaOrderStoreEvent> orderStoreProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerFactory());
    }

    @Bean
    public KafkaTemplate<String, KafkaOrderStoreEvent> orderStoreKafkaTemplate() {
        return new KafkaTemplate<>(orderStoreProducerFactory());
    }

    // BalanceIncreaseEvent
    @Bean
    public ProducerFactory<String, KafkaBalanceIncreaseEvent> balanceIncreaseProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerFactory());
    }

    @Bean
    public KafkaTemplate<String, KafkaBalanceIncreaseEvent> balanceIncreaseKafkaTemplate() {
        return new KafkaTemplate<>(balanceIncreaseProducerFactory());
    }
}
