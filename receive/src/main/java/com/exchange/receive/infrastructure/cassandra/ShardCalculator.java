package com.exchange.receive.infrastructure.cassandra;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;

@Component
public class ShardCalculator {
    private final int shardCount;

    public ShardCalculator(@Value("${cassandra.shard.count:10}") int shardCount) {
        this.shardCount = shardCount;
    }

    public int calculateShard(UUID orderId) {
        return Math.abs(orderId.hashCode()) % shardCount;
    }
}