package com.exchange.order_completed.domain.cassandra.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
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

    @PrimaryKeyColumn(name = "shard", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private int shard;

    @PrimaryKeyColumn(name = "year_month_date", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private LocalDate yearMonthDate;

    @PrimaryKeyColumn(name = "order_id", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    private UUID orderId;

    @Column("created_at")
    private Instant createdAt;

    @Column("price")
    private BigDecimal price;

    @Column("quantity")
    private BigDecimal quantity;

    @Column("order_type")
    @Comment("거래 유형 (BUY 또는 SELL)")
    @CassandraType(type = CassandraType.Name.TEXT)
    private OrderType orderType;

    @Column("order_state")
    @Comment("거래 유형 (PENDING 또는 CANCEL)")
    @CassandraType(type = CassandraType.Name.TEXT)
    private OrderState orderState;

    @Column("trading_pair")
    @Comment("거래 쌍 (BTC-USD)")
    private String tradingPair;

}