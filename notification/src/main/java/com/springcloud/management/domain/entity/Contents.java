package com.springcloud.management.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contents {

    @Column(columnDefinition = "TEXT",nullable = false)
    @Comment("메시지 내용")
    private String message;

    @Column(name = "sent_at")
    @Comment("발송시간")
    private LocalDateTime sentAt;

    public static Contents create(String message) {
        return new Contents(message,LocalDateTime.now());
    }

    private Contents(String message, LocalDateTime sentAt) {
        this.message = message;
        this.sentAt = sentAt;
    }
}
