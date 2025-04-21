package com.springcloud.user.infrastructure.dto;

import com.springcloud.user.application.command.DecreaseBalanceCommand;
import com.springcloud.user.application.enums.OrderType;
import com.springcloud.user.domain.entity.UserBalance;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
public class KafkaOrderFormEvent {
    private UUID orderId;
    private UUID userId; //유저 아이디
    private OrderType orderType; // buy/sell
    private BigDecimal price; //총 가격
    private BigDecimal quantity; // 수량
    private String tradingPair; //거래소 명칭

    public static KafkaOrderFormEvent fromEvent(DecreaseBalanceCommand command) {
        return new KafkaOrderFormEvent(
                command.getOrderId(),
                command.getUserId(),
                command.getOrderType(),
                command.getPrice(),
                command.getQuantity(),
                command.getTradingPair()
        );



    }
}
