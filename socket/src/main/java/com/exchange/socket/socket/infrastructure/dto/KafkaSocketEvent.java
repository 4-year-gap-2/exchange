package com.exchange.socket.socket.infrastructure.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class KafkaSocketEvent {
    private String content;
    private UUID sender;
}
