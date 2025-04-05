package com.exchange.matching.domain.entiry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 카산드라 전용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("transactions")
public class TransactionV1 {

    @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String userId;

    @PrimaryKeyColumn(name = "transaction_date", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private LocalDateTime transactionDate;

    @PrimaryKeyColumn(name = "transaction_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID transactionId;

    @Column("price")
    private BigDecimal price;

    @Column("amount")
    private BigDecimal amount;

    @Column("transaction_type")
    private String transactionType; // "BUY" 또는 "SELL"

    @Column("pair")
    private String pair; // 예: "BTC-USD"
}
