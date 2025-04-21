package com.exchange.order.application.result;

import com.exchange.order.application.enums.OrderType;
import com.exchange.order.infrastructure.dto.KafkaUserBalanceDecreaseEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class FindOrderResult {
    private UUID orderId; //주문 아이디
    private UUID userId; //유저 아이디
    private OrderType orderType; // buy/sell
    private BigDecimal price; //총 가격
    private BigDecimal amount; // 수량
    private String tradingPair; //거래소 명칭

    public static FindOrderResult fromResult(KafkaUserBalanceDecreaseEvent event) {
        return new FindOrderResult(
                event.getOrderId(),
                event.getUserId(),
                event.getOrderType(),
                event.getPrice(),
                event.getAmount(),
                event.getTradingPair()
        );
    }
}

