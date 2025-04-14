package com.exchange.matching.domain.entiry;

import com.exchange.matching.application.dto.enums.OrderStatus;
import com.exchange.matching.application.dto.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "trading_pair", nullable = false)
    private String tradingPair;       // "BTC/KRW"

    //    @Column(name = "price", precision = 9, scale = 8, nullable = false)
    @Column(name = "price", nullable = false)
    private BigDecimal price;         // 주문 가격 (지정가)

    @Column(name = "quantity", precision = 9, scale = 6, nullable = false)
    private BigDecimal quantity;      // 주문 수량 (남은 수량 의미)

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private OrderType type;           // BUY or SELL

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;       // PENDING(미체결) 또는 COMPLETED(완료)
}