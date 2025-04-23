package com.exchange.order.infrastructure.dto;

import com.exchange.order.application.command.CreateOrderCommand;
import com.exchange.order.application.enums.OrderType;
import com.exchange.order.application.result.FindOrderResult;
import com.exchange.order.presentation.response.CreateOrderResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class KafkaUserBalanceDecreaseEvent {
    private UUID orderId; //주문 아이디
    private UUID userId; //유저 아이디
    private OrderType orderType; // buy/sell
    private BigDecimal price; //총 가격
    private BigDecimal quantity; // 수량
    private String tradingPair; //거래소 명칭

    public static KafkaUserBalanceDecreaseEvent fromCommand(CreateOrderCommand command) {
        return new KafkaUserBalanceDecreaseEvent(
                command.getOrderId(),
                command.getUserId(),
                command.getOrderType(),
                command.getPrice(),
                command.getQuantity(),
                command.getTradingPair()
        );
    }

}

