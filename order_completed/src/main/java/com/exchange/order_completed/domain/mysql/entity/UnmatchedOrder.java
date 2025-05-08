package com.exchange.order_completed.domain.mysql.entity;

import com.exchange.order_completed.domain.cassandra.entity.OrderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UnmatchedOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID userId;

    private UUID orderId;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "order_type")
    @Comment("거래 유형 (BUY 또는 SELL)")
    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Column(name = "trading_pair")
    @Comment("거래 쌍 (BTC-USD)")
    private String tradingPair;
}
