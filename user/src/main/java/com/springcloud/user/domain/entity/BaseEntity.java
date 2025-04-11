package com.springcloud.user.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Comment("레코드 생성 일시")
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    @Comment("레코드 생성자")
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    @Comment("레코드 수정 일시")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    @Comment("레코드 수정자")
    private String updatedBy;

    @Column(name = "deleted_at")
    @Temporal(TemporalType.TIMESTAMP)
    @Comment("레코드 삭제 일시")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 50)
    @Comment("레코드 삭제자")
    private String deletedBy;

    // 엔터티 논리 삭제 시 수동 호출
    public void delete() {
        this.deletedBy = getCurrentUser();
        this.deletedAt = LocalDateTime.now();
    }

    // 엔터티 생성 시 호출
    @PrePersist
    public void prePersist() {
        this.createdBy = getCurrentUser();
        this.createdAt = LocalDateTime.now();
    }

    // 엔터티 업데이트 시 호출
    @PreUpdate
    public void preUpdate() {
        this.updatedBy = getCurrentUser();
        this.updatedAt = LocalDateTime.now();
    }

    private String getCurrentUser() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) return "system";

        String username = attributes.getRequest().getHeader("X-USERNAME");

        if (username == null || username.isEmpty()) return "system";

        return username;
    }
}
