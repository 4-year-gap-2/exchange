package com.exchange.web_socket.application;

import com.exchange.web_socket.application.dto.ChartCommand;
import lombok.RequiredArgsConstructor;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final SimpMessagingTemplate messagingTemplate;


    @Override
    public void sendChartData(ChartCommand command) {
        messagingTemplate.convertAndSend("/topic/notifications/" + command.getTradingPair(), command.toString());

    }

    @Override
    public void sendDecreaseBalanceFail(String userId) {

        String alertMessage = "재산이 부족합니다. 주문을 처리할 수 없습니다.";

        messagingTemplate.convertAndSendToUser(userId, "/topic/notifications", alertMessage);

    }

    @Override
    public void sendMatchNotification(String userId,String message) {

        messagingTemplate.convertAndSendToUser(userId, "/topic/notifications", message);
    }

}
