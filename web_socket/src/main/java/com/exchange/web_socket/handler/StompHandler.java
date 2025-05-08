package com.exchange.web_socket.handler;

import com.exchange.web_socket.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {
    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);

                if (!jwtUtil.validateToken(token)) {
                    throw new IllegalArgumentException("Invalid JWT token");
                }

                Authentication auth = jwtUtil.getAuthentication(token);
                accessor.setUser(auth); // 사용자 인증 정보 주입
            }

            // 토큰이 없으면 인증 정보 없이 연결 → 공개 채널만 접근 가능
        }

        return message;
    }
}
