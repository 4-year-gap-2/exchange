package com.springcloud.user.domain.entity;

import lombok.Getter;

@Getter
public enum UserRole {

    MASTER(Authority.MASTER),
    HUB_MANAGER(Authority.MANAGER);

    private final String authority;

    UserRole(String authority) {
        this.authority = authority;
    }

    public static class Authority {
        public static final String MASTER = "ROLE_MASTER";
        public static final String MANAGER = "ROLE_MANAGER";
    }
}