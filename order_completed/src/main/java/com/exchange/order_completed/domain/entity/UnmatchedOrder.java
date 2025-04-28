package com.exchange.order_completed.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("unmatched_order")
public class UnmatchedOrder {

    @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    @PrimaryKeyColumn(name = "order_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private UUID orderId;

    @PrimaryKeyColumn(name = "created_at", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    @Column("created_at")
    private Instant createdAt;

    @Column("created_date")
    private LocalDate createdDate;

    @Column("price")
    private BigDecimal price;

    @Column("quantity")
    private BigDecimal quantity;

    @Column("order_type")
    @Comment("거래 유형 (BUY 또는 SELL)")
    private String orderType;

    @Column("trading_pair")
    @Comment("거래 쌍 (BTC-USD)")
    private String tradingPair;

}