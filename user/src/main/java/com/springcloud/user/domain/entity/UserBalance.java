package com.springcloud.user.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Builder
@Setter
@Table(name = "p_user_balances")
public class UserBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID balanceId;// 별도의 PK 사용

    @ManyToOne  // 다대일 관계
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "coin_id", nullable = false)
    @Comment("코인 타입") //추후 코인 테이블로 분류 UUID로 적용할 것
    private Coin coin;

    @Column(nullable = false)
    @Comment("총 자산")
    private BigDecimal totalBalance;

    @Column(nullable = false)
    @Comment("사용 가능 자산")
    private BigDecimal availableBalance;

    @Column(nullable = false, unique = true)
    @Comment("지갑 주소")
    private String wallet;

    public void increase(BigDecimal amount) {
        // 1. 금액 유효성 검증은 프레젠테이션 계층에서 어노테이션으로 진행(생략)
        // 2. 잔액 증가
        this.totalBalance = this.totalBalance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
    }
}
