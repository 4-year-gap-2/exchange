package com.springcloud.user.domain.entity;

import lombok.Getter;

@Getter
public enum CoinType {
    SOL("솔라나"),
    ETH("이더리움"),
    BTC("비트코인");

    private final String description;

    CoinType(String description) {this.description = description; }
}
