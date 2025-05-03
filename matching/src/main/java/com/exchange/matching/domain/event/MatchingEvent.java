package com.exchange.matching.domain.event;

import com.exchange.matching.application.enums.OrderType;
import com.exchange.matching.domain.service.MatchingServiceV5.MatchingOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * 매칭 이벤트 정보를 저장하는 클래스
 */
@Getter
@NoArgsConstructor
public class MatchingEvent {
    private UUID eventId;
    private MatchingEventType eventType;
    private String tradingPair;
    private OrderType orderType;
    private BigDecimal price;
    private BigDecimal quantity;
    private UUID userId;
    private UUID orderId;
    private UUID matchedOrderId;
    private BigDecimal matchedPrice;
    private BigDecimal matchedQuantity;
    private BigDecimal remainingQuantity;
    private long timestamp;
    private String correlationId; // 동일 주문 처리 흐름을 식별하기 위한 상관 관계 ID

    private MatchingEvent(Builder builder) {
        this.eventId = UUID.randomUUID();
        this.eventType = builder.eventType;
        this.tradingPair = builder.tradingPair;
        this.orderType = builder.orderType;
        this.price = builder.price;
        this.quantity = builder.quantity;
        this.userId = builder.userId;
        this.orderId = builder.orderId;
        this.matchedOrderId = builder.matchedOrderId;
        this.matchedPrice = builder.matchedPrice;
        this.matchedQuantity = builder.matchedQuantity;
        this.remainingQuantity = builder.remainingQuantity;
        this.timestamp = Instant.now().toEpochMilli();
        this.correlationId = builder.correlationId;
    }

    /**
     * 주문 접수 이벤트 생성
     */
    public static MatchingEvent orderReceived(MatchingOrder order, String correlationId) {
        return new Builder()
                .eventType(MatchingEventType.ORDER_RECEIVED)
                .tradingPair(order.getTradingPair())
                .orderType(order.getOrderType())
                .price(order.getPrice())
                .quantity(order.getQuantity())
                .userId(order.getUserId())
                .orderId(order.getOrderId())
                .correlationId(correlationId)
                .build();
    }

    /**
     * 미체결 이벤트 생성
     */
    public static MatchingEvent orderUnmatched(MatchingOrder order, String correlationId) {
        return new Builder()
                .eventType(MatchingEventType.ORDER_UNMATCHED)
                .tradingPair(order.getTradingPair())
                .orderType(order.getOrderType())
                .price(order.getPrice())
                .quantity(order.getQuantity())
                .userId(order.getUserId())
                .orderId(order.getOrderId())
                .correlationId(correlationId)
                .build();
    }

    /**
     * 주문 체결 이벤트 생성
     */
    public static MatchingEvent orderMatched(MatchingOrder order, MatchingOrder oppositeOrder,
                                             BigDecimal matchedQuantity, BigDecimal matchPrice, String correlationId) {
        return new Builder()
                .eventType(MatchingEventType.ORDER_MATCHED)
                .tradingPair(order.getTradingPair())
                .orderType(order.getOrderType())
                .price(order.getPrice())
                .quantity(order.getQuantity())
                .userId(order.getUserId())
                .orderId(order.getOrderId())
                .matchedOrderId(oppositeOrder.getOrderId())
                .matchedPrice(matchPrice)
                .matchedQuantity(matchedQuantity)
                .correlationId(correlationId)
                .build();
    }

    /**
     * 잔여 주문 이벤트 생성
     */
    public static MatchingEvent orderRemaining(MatchingOrder order, BigDecimal remainingQuantity, String correlationId) {
        return new Builder()
                .eventType(MatchingEventType.ORDER_REMAINING)
                .tradingPair(order.getTradingPair())
                .orderType(order.getOrderType())
                .price(order.getPrice())
                .quantity(order.getQuantity())
                .userId(order.getUserId())
                .orderId(order.getOrderId())
                .remainingQuantity(remainingQuantity)
                .correlationId(correlationId)
                .build();
    }

    /**
     * 처리 완료 이벤트 생성
     */
    public static MatchingEvent processingCompleted(String correlationId) {
        return new Builder()
                .eventType(MatchingEventType.PROCESSING_COMPLETED)
                .correlationId(correlationId)
                .build();
    }

    /**
     * 빌더 클래스
     */
    public static class Builder {
        private MatchingEventType eventType;
        private String tradingPair;
        private OrderType orderType;
        private BigDecimal price;
        private BigDecimal quantity;
        private UUID userId;
        private UUID orderId;
        private UUID matchedOrderId;
        private BigDecimal matchedPrice;
        private BigDecimal matchedQuantity;
        private BigDecimal remainingQuantity;
        private String correlationId;

        public Builder eventType(MatchingEventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder tradingPair(String tradingPair) {
            this.tradingPair = tradingPair;
            return this;
        }

        public Builder orderType(OrderType orderType) {
            this.orderType = orderType;
            return this;
        }

        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder quantity(BigDecimal quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder orderId(UUID orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder matchedOrderId(UUID matchedOrderId) {
            this.matchedOrderId = matchedOrderId;
            return this;
        }

        public Builder matchedPrice(BigDecimal matchedPrice) {
            this.matchedPrice = matchedPrice;
            return this;
        }

        public Builder matchedQuantity(BigDecimal matchedQuantity) {
            this.matchedQuantity = matchedQuantity;
            return this;
        }

        public Builder remainingQuantity(BigDecimal remainingQuantity) {
            this.remainingQuantity = remainingQuantity;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public MatchingEvent build() {
            return new MatchingEvent(this);
        }
    }
}