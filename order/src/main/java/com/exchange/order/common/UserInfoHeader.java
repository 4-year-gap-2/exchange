package com.exchange.order.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UserInfoHeader {

    private final UUID userId;
    private final String username;
    private final UserRole userRole;

    public UserInfoHeader(HttpServletRequest request) {
//        this.userId = UUID.fromString(request.getHeader("X-USER-ID"));
//        this.username = request.getHeader("X-USERNAME");
//        this.userRole = UserRole.valueOf(request.getHeader("X-USER-ROLE"));
        String userIdHeader = request.getHeader("X-USER-ID");
        if (userIdHeader == null || userIdHeader.isEmpty()) {
            throw new IllegalArgumentException("X-USER-ID 헤더가 필요합니다");
        }
        try {
            this.userId = UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("X-USER-ID 헤더는 유효한 UUID 형식이어야 합니다", e);
        }

        String username = request.getHeader("X-USERNAME");
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("X-USERNAME 헤더가 필요합니다");
        }
        this.username = username;

        String roleHeader = request.getHeader("X-USER-ROLE");
        if (roleHeader == null || roleHeader.isEmpty()) {
            throw new IllegalArgumentException("X-USER-ROLE 헤더가 필요합니다");
        }
        try {
            this.userRole = UserRole.valueOf(roleHeader);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("X-USER-ROLE 헤더는 유효한 UserRole 값이어야 합니다", e);
        }


    }
}

