package com.exchange.order_completed.presentation.dto;

import com.exchange.order_completed.domain.cassandra.entity.MatchedOrder;
import com.exchange.order_completed.domain.cassandra.entity.OrderType;
import com.exchange.order_completed.domain.cassandra.entity.UnmatchedOrder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class TradeDataResponse {
    private String tradingPair;
    private OrderType orderType;
    private BigDecimal price;
    private BigDecimal quantity;
    private Instant createdAt;  // 타입 변경

    // DTO 변환 메서드 예시
    public static TradeDataResponse fromMatchedEntity(MatchedOrder order) {
        TradeDataResponse dto = new TradeDataResponse();
        dto.setTradingPair(order.getTradingPair());
        dto.setOrderType(order.getOrderType());
        dto.setPrice(order.getPrice());
        dto.setQuantity(order.getQuantity());
        dto.setCreatedAt(order.getCreatedAt());  // 직접 할당
        return dto;
    }

    // DTO 변환 메서드 예시
    public static TradeDataResponse fromUnmatchedEntity(UnmatchedOrder order) {
        TradeDataResponse dto = new TradeDataResponse();
        dto.setTradingPair(order.getTradingPair());
        dto.setOrderType(order.getOrderType());
        dto.setPrice(order.getPrice());
        dto.setQuantity(order.getQuantity());
        dto.setCreatedAt(order.getCreatedAt());  // 직접 할당
        return dto;
    }

}
