package com.springcloud.user.infrastructure.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

@Getter
public class KafkaUserBalanceDecreaseEvent  implements Serializer, Deserializer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private  UUID orderId; //주문 아이디
    private UUID userId; //유저 아이디
    private String orderType; // buy/sell
    private BigDecimal price; //총 가격
    private BigDecimal amount; // 수량
    private String symbol; //거래소 명칭

    @Override
    public Object deserialize(String s, byte[] bytes) {
        return null;
    }

    @Override
    public Object deserialize(String topic, Headers headers, byte[] data) {
        return Deserializer.super.deserialize(topic, headers, data);
    }

    @Override
    public Object deserialize(String topic, Headers headers, ByteBuffer data) {
        return Deserializer.super.deserialize(topic, headers, data);
    }

    @Override
    public void configure(Map configs, boolean isKey) {
        Serializer.super.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String s, Object o) {
        return new byte[0];
    }

    @Override
    public byte[] serialize(String topic, Headers headers, Object data) {
        return Serializer.super.serialize(topic, headers, data);
    }

    @Override
    public void close() {
        Serializer.super.close();
    }
}
