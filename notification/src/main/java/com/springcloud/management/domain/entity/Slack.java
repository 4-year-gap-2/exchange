package com.springcloud.management.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@Table(name = "p_slack")
public class Slack extends BaseEntity {

    @Id
    @Column(name = "slack_id")
    @Comment("슬랙 고유키")
    private UUID id;

    @Comment("유저 ID")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Embedded
    private Contents contents;

    public static Slack createSlack(UUID userId, String message) {
        return Slack.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .contents(Contents.create(message))
                .build();
    }
}

