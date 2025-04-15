package com.exchange.matching.domain.entiry;

import com.exchange.matching.application.dto.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activated_orders_b")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ActivatedOrderB {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "activated_order_id", nullable = false, unique = true)
    @Comment("주문 고유키")
    private UUID ActivatedOrderId;

    @Column(name = "user_id", nullable = false)
    @Comment("주문자 ID")
    private UUID userId;

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
    @Comment("주문 생성 시간")
    private LocalDateTime createdAt;

    @Version
    @Column(name = "version", nullable = false)
    @Comment("낙관적 락을 위한 버전 필드")
    private Long version;
}