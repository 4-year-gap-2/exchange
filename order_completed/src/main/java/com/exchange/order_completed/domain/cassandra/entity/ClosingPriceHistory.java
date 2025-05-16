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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("closing_price_history")
public class ClosingPriceHistory {

    @PrimaryKeyColumn(name = "trading_pair", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    @Comment("거래 쌍 (예: BTC/USD)")
    private String tradingPair;

    @PrimaryKeyColumn(name = "date", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    @Comment("종가 날짜")
    private LocalDate date;

    @Column("price")
    @Comment("종가 가격")
    private BigDecimal price;

    @Column("created_at")
    @Comment("레코드 생성 시간")
    private Instant createdAt;
}