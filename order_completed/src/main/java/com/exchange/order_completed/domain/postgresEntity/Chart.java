package com.exchange.order_completed.domain.postgresEntity;


import com.exchange.order_completed.domain.entiry.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Chart {

    @Id
    private UUID chartId;

    @Column("price")
    private BigDecimal price;

    @Column("amount")
    private BigDecimal amount;

    @Column("transaction_type")
    private String transactionType; // "BUY" 또는 "SELL"

    @Column("pair")
    private String pair; // 예: "BTC-USD"

    @CreatedDate
    @jakarta.persistence.Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Comment("레코드 생성 일시")
    private LocalDateTime createdAt;

}
