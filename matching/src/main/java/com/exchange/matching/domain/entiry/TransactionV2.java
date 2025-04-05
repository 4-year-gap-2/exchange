package com.exchange.matching.domain.entiry;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RDBMS 전용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions",
        indexes = {
                @Index(name = "idx_user_date", columnList = "user_id, transaction_date")
        })
public class TransactionV2 {
    @Id
    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "price", precision = 19, scale = 6)
    private BigDecimal price;

    @Column(name = "amount", precision = 19, scale = 6)
    private BigDecimal amount;

    @Column(name = "transaction_type", length = 10)
    private String transactionType; // "BUY" 또는 "SELL"

    @Column(name = "pair", length = 20)
    private String pair; // 예: "BTC-USD"
}