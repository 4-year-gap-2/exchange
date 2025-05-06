package com.exchange.web_socket.application;

import com.exchange.web_socket.application.dto.ChartCommand;

public interface MessageService {
    void sendChartData(ChartCommand command);
    void sendDecreaseBalanceFail(String message);
    void sendMatchNotification(String userId,String message);
}
