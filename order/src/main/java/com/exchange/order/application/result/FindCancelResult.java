package com.exchange.order.application.result;

import com.exchange.order.application.enums.OrderType;
import com.exchange.order.presentation.request.CancelOrderRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class FindCancelResult {
    private UUID orderId; //주문 아이디
    private UUID userId; //유저 아이디
    private OrderType orderType; // buy/sell
    private BigDecimal quantity; // 수량
    private String tradingPair; //거래소 명칭

    public static FindCancelResult fromResult(CancelOrderRequest request) {
        return new FindCancelResult(
                request.getOrderId(),
                request.getUserId(),
                request.getOrderType(),
                request.getQuantity(),
                request.getTradingPair()
        );
    }
}
