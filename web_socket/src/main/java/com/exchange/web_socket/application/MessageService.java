package com.exchange.web_socket.application;

import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    @SendTo("/topic/messages")
    public String sendMessage(String message) {
        return message;
    }
}
