package com.springcloud.user.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "p_user")
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Comment("사용자 식별자")
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, unique = true)
    @Comment("로그인 아이디")
    private String username;

    @Column(nullable = false)
    @Comment("로그인 비밀번호")
    private String password;

    @Column(nullable = false)
    @Comment("전화번호")
    private String phone;

    @Column(nullable = false)
    @Comment("이메일")
    private String email;

    @Column(nullable = false)
    @Comment("입출금 계좌번호")
    private String bankAccountNumber;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    @Comment("사용자 권한")
    private UserRole role;

    // 수정: mappedBy = "user" (UserBalance의 필드명과 일치)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserBalance> balances = new ArrayList<>();

    public UserBalance getBalanceByWallet(String wallet) {
        return balances.stream()
                .filter(b -> b.getWallet().equals(wallet))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지갑 주소를 찾을 수 없습니다: " + wallet));
    }

    // 도메인 로직: 자산 계좌 조회 메서드
    public UserBalance getBalance(String coinName) {
        return balances.stream()
                .filter(b -> b.getCoin().getCoinName().equals(coinName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당하는 코인에 대한 자산이 존재하지 않습니다"));
    }
}
