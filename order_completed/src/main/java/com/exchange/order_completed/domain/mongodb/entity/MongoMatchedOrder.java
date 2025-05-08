package com.exchange.order_completed.domain.mongodb.entity;

import com.exchange.order_completed.domain.cassandra.entity.OrderType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Document(collection = "matched_order")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MongoMatchedOrder {

    private UUID userId;

    private LocalDate yearMonthDate;

    private UUID idempotencyId;

    @Field(name = "created_at")
    private Instant createdAt;

    @Field(name = "order_id")
    private UUID orderId;

    @Field(name = "price")
    private BigDecimal price;

    @Field(name = "quantity")
    private BigDecimal quantity;

    @Field(name = "order_type")
    @Comment("거래 유형 (BUY 또는 SELL)")
    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Field(name = "trading_pair")
    @Comment("거래 쌍 (BTC/USD)")
    private String tradingPair;

}
