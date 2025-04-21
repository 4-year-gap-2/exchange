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
public class KafkaUserBalanceDecreaseEvent {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private UUID orderId; //주문 아이디
    private UUID userId; //유저 아이디
    private OrderType orderType; // buy/sell
    private BigDecimal price; //총 가격
    private BigDecimal quantity; // 수량
    private String tradingPair; //거래소 명칭

}
