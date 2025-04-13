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

// 지갑에 들어가야하는 거 아닌가?
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    @Comment("코인 타입") //추후 코인 테이블로 분류 UUID로 적용할 것
    private CoinType coinType;

    @Column(nullable = false)
    @Comment("총 자산")
    private BigDecimal totalBalance;

    @Column(nullable = false)
    @Comment("사용 가능 자산")
    private BigDecimal availableBalance;

    @Column(nullable = false, unique = true)
    @Comment("지갑 주소")
    private String wallet;
}
