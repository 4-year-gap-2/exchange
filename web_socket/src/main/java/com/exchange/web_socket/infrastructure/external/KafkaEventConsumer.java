package com.exchange.web_socket.infrastructure.external;


import com.exchange.web_socket.dto.CompletedOrderChangeEvent;
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
            topics = "cassandra.exchange.matched_order",
            containerFactory = "messageKafkaListenerContainerFactory")
    public void notificationChart(CompletedOrderChangeEvent event) {

        messagingTemplate.convertAndSend("/topic/notifications/BTC-KRW", event.getAfter());
    }

    @KafkaListener(
            topics = "user-to-socket.execute-socket",
            containerFactory = "messageBalanceKafkaListenerContainerFactory")
    public void notificationBalance(MessageEvent event) {

        String alertMessage = "재산이 부족합니다. 주문을 처리할 수 없습니다.";

        messagingTemplate.convertAndSendToUser(event.getReceiver(), "/topic/notifications", alertMessage);
    }

    @KafkaListener(
            topics = "cassandra.exchange.matched_order",
            containerFactory = "messageKafkaListenerContainerFactory"
    )
    public void notificationMatch(CompletedOrderChangeEvent event) {

        String userId = event.getAfter().getUserId().toString();
        String tradingPair = event.getAfter().getTrading_pair().getValue();
        String quantity = event.getAfter().getQuantity().getValue().toPlainString();


        String alertMessage = String.format("%s 주문 %s개 체결이 완료되었습니다.", tradingPair, quantity);


        messagingTemplate.convertAndSendToUser(userId, "/topic/notifications", alertMessage);
    }

}
