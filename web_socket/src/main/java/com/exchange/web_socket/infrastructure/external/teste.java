package com.exchange.web_socket.infrastructure.external;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.access.AccessDeniedException;

import java.security.Principal;
import java.util.UUID;

public class teste {

    @SubscribeMapping("/user/{userId}/topic/notifications")
    public void subscribeToUserTopic(@DestinationVariable UUID userId, Principal principal) {
        if (principal == null || !principal.getName().equals(userId.toString())) {
            throw new AccessDeniedException("Unauthorized subscription.");
        }
    }
}
