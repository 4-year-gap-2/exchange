package com.exchange.web_socket.infrastructure.external;


import com.exchange.web_socket.application.MessageService;
import com.exchange.web_socket.application.dto.ChartCommand;
import com.exchange.web_socket.infrastructure.dto.CompletedOrderChangeEvent;
import com.exchange.web_socket.infrastructure.dto.MessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class KafkaEventConsumer {


    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @KafkaListener(
            topics = "cassandra.exchange.matched_order",
            containerFactory = "messageKafkaListenerContainerFactory")
    public void notificationChart(CompletedOrderChangeEvent event) {

        ChartCommand chartCommand = ChartCommand.from(event);
        messageService.sendChartData(chartCommand);

    }

    @KafkaListener(
            topics = "user-to-socket.execute-balance-decrease-fail",
            containerFactory = "messageBalanceKafkaListenerContainerFactory")
    public void notificationBalance(MessageEvent event) {

        messageService.sendDecreaseBalanceFail(event.getUserId().toString());

    }

    @KafkaListener(
            topics = "cassandra.exchange.matched_order",
            containerFactory = "messageKafkaListenerContainerFactory"
    )
    public void notificationMatch(CompletedOrderChangeEvent event) {

        String userId = event.getAfter().getUser_id().getValue().toString();
        String tradingPair = event.getAfter().getTrading_pair().getValue().replace('/','-');
        String quantity = event.getAfter().getQuantity().getValue().toPlainString();
        String alertMessage = String.format("%s 주문 %s개 체결이 완료되었습니다.", tradingPair, quantity);

        messageService.sendMatchNotification(userId,alertMessage);

    }

}
