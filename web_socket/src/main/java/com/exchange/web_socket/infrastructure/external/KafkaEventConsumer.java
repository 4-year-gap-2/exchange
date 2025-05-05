package com.exchange.web_socket.infrastructure.external;



import com.exchange.web_socket.dto.MessageEvent;
import com.exchange.web_socket.application.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class KafkaEventConsumer {


    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            topics = "order-matched-to-socket.execute-notification.match",
            containerFactory = "messageKafkaListenerContainerFactory")
    public void savaChart(MessageEvent event) {

        messagingTemplate.convertAndSend("/topic/notifications", event.getMessage());
    }
}
