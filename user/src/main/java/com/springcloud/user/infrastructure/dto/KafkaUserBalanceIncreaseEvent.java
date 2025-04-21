package com.springcloud.user.infrastructure.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.user.application.enums.OrderType;
import lombok.Getter;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

@Getter
public class KafkaUserBalanceIncreaseEvent{

    private UUID orderId;
    private String tradingPair;
    private BigDecimal price;
    private BigDecimal quantity;
    private UUID buyer;
    private UUID seller;
    private OrderType orderType;
}
