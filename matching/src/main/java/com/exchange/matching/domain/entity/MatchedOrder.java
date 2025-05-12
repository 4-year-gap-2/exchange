package com.exchange.matching.domain.entity;

import com.exchange.matching.application.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "matched_order")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MatchedOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "matched_order_id", nullable = false, unique = true)
    @Comment("체결 주문 고유키")
    private UUID CompletedOrderId;

    @Column(name = "seller_id", nullable = false)
    @Comment("판매자 ID")
    private UUID sellerId;

    @Column(name = "buyer_id", nullable = false)
    @Comment("구매자 ID")
    private UUID buyerId;

    @Column(name = "order_id", nullable = false)
    @Comment("주문 ID")
    private UUID orderId;

    @Column(name = "trading_pair", nullable = false)
    @Comment("거래쌍 (BTC/KRW)")
    private String tradingPair;

    //    @Column(name = "price", precision = 9, scale = 8, nullable = false)
    @Column(name = "price", nullable = false)
    @Comment("주문 가격")
    private BigDecimal price;

    @Column(name = "quantity", precision = 9, scale = 6, nullable = false)
    @Comment("주문 수량 (남은 수량)")
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @Comment("주문 유형 (BUY or SELL)")
    private OrderType type;

    @Column(name = "created_at", nullable = false)
    @Comment("체결 주문 생성 시간")
    private LocalDateTime createdAt;
}
