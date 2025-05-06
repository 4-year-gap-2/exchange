package com.springcloud.user.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.type.descriptor.jdbc.TinyIntAsSmallIntJdbcType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
@Entity
@Table(name = "p_coins")
public class Coin extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID coinId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_id", nullable = false)
    @Comment("수수료")
    private Fee fee;

    @Column(nullable = false)
    @Comment("코인명")
    private String coinName;

    @Column(nullable = false)
    @Comment("심볼명")
    private String symbol;

    @Comment("코인 설명")
    private String description;

    @Comment("활성화여부")
    private boolean isActive;

    @Comment("코인을 가지는 유저밸런스")
    @OneToMany(mappedBy = "coin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserBalance> balances = new ArrayList<>();



}
