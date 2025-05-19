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
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("cold_data_orders")
public class ColdDataOrders {

    @PrimaryKeyColumn(name = "trading_pair", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    @Comment("거래 쌍 (BTC-USD)")
    private String tradingPair;

    @PrimaryKeyColumn(name = "order_type", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    @Comment("거래 유형 (BUY 또는 SELL)")
    @CassandraType(type = CassandraType.Name.TEXT)
    private OrderType orderType;

    @PrimaryKeyColumn(name = "price_order", ordinal = 2, type = PrimaryKeyType.PARTITIONED)
    @Comment("가격 정렬 방향 (ASC 또는 DESC)")
    private String priceOrder;

    @PrimaryKeyColumn(name = "price", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    private BigDecimal price;

    @PrimaryKeyColumn(name = "order_id", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
    private UUID orderId;

    @Column("quantity")
    private BigDecimal quantity;

    @Column("timestamp")
    private Long timestamp;

    @Column("user_id")
    private UUID userId;

    @Column("order_state")
    @Comment("거래 유형 (PENDING 또는 CANCEL)")
    @CassandraType(type = CassandraType.Name.TEXT)
    private OrderState orderState;
}