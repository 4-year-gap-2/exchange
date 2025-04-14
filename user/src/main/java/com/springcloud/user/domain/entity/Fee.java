package com.springcloud.user.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
@Entity
@Table(name = "p_fees")
public class Fee extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID feeId;

    @Column(nullable = false)
    @Comment("수수료율")
    private BigDecimal feeValue;

    @Comment("수수료에 대한 코인")
    @OneToMany(mappedBy = "fee")
    private List<Coin> coins = new ArrayList<>();

}
