package com.exchange.matching.infrastructure.dto;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.dto.enums.OrderType;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class KafkaMatchingEvent implements  Serializer , Deserializer {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private String tradingPair;
    private OrderType orderType;
    private BigDecimal price;
    private BigDecimal quantity;
    private UUID userId;
    private UUID orderId;

    public KafkaMatchingEvent(String tradingPair, OrderType orderType, BigDecimal price, BigDecimal quantity, UUID userId, UUID orderId) {
        this.tradingPair = tradingPair;
        this.orderType = orderType;
        this.price = price;
        this.quantity = quantity;
        this.userId = userId;
        this.orderId = orderId;
    }

    public static CreateMatchingCommand commandFromEvent(KafkaMatchingEvent event) {
        return new CreateMatchingCommand(
                event.tradingPair,
                event.orderType,
                event.price,
                event.quantity,
                event.userId,
                event.orderId
        );
    }

    public static KafkaMatchingEvent fromCommand(CreateMatchingCommand command) {
        return new KafkaMatchingEvent(
                command.tradingPair(),
                command.orderType(),
                command.price(),
                command.quantity(),
                command.userId(),
                UUID.randomUUID()
        );
    }

    @Override
    public void configure(Map configs, boolean isKey) {
        Serializer.super.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String s, Object data) {

        try {
            if (data == null) {
                return null;
            }
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing OrderCreateEvent", e);
        }
    }

    @Override
    public byte[] serialize(String topic, Headers headers, Object data) {
        return Serializer.super.serialize(topic, headers, data);
    }

    @Override
    public void close() {
        Serializer.super.close();
    }

    @Override
    public Object deserialize(String s, byte[] bytes) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, KafkaMatchingEvent.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON", e);
        }
    }

    @Override
    public Object deserialize(String topic, Headers headers, byte[] data) {
        return Deserializer.super.deserialize(topic, headers, data);
    }

    @Override
    public Object deserialize(String topic, Headers headers, ByteBuffer data) {
        return Deserializer.super.deserialize(topic, headers, data);
    }
}