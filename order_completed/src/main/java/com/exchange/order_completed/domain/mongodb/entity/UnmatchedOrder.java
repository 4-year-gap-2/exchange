package com.exchange.order_completed.domain.mongodb.entity;

import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Document(collection = "unmatched_order")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnmatchedOrder {

    private UUID userId;

    private UUID orderId;

    @Field(name = "created_at")
    private Instant createdAt;

    @Field(name = "created_date")
    private LocalDate createdDate;

    @Field(name = "price")
    private BigDecimal price;

    @Field(name = "quantity")
    private BigDecimal quantity;

    @Field(name = "order_type")
    @Comment("거래 유형 (BUY 또는 SELL)")
    private String orderType;

    @Field(name = "trading_pair")
    @Comment("거래 쌍 (BTC-USD)")
    private String tradingPair;

}