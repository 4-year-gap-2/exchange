package com.exchange.matching.domain.entiry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@Table(name = "p_matching")
public class MatchingTest {
    @Id
    @Column(name = "matching_id")
    @Comment("매칭 고유키 테스트 용도")
    private UUID Id;
}
