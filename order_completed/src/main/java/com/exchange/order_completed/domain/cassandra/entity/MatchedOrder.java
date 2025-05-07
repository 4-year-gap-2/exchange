package com.exchange.order_completed.domain.cassandra.entity;

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
@Table("matched_order")
public class MatchedOrder {

    @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    @PrimaryKeyColumn(name = "year_month_date", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private LocalDate yearMonthDate;

    @PrimaryKeyColumn(name = "idempotency_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID idempotencyId;

    @Column("created_at")
    private Instant createdAt;

    @Column("order_id")
    private UUID orderId;

    @Column("price")
    private BigDecimal price;

    @Column("quantity")
    private BigDecimal quantity;

    @Column("order_type")
    @Comment("거래 유형 (BUY 또는 SELL)")
    private String orderType;

    @Column("trading_pair")
    @Comment("거래 쌍 (BTC/USD)")
    private String tradingPair;

}
