package com.exchange.order.presentation.response;

import com.exchange.order.application.enums.OrderType;
import com.exchange.order.application.result.FindCancelResult;
import com.exchange.order.application.result.FindOrderResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CancelOrderResponse {
    private UUID orderId; //주문 아이디
    private UUID userId; //유저 아이디
    private OrderType orderType; // buy/sell
    private BigDecimal quantity; // 수량
    private String tradingPair; //거래소 명칭

    public static CancelOrderResponse fromResult(FindCancelResult result) {
        return new CancelOrderResponse(
                result.getOrderId(),
                result.getUserId(),
                result.getOrderType(),
                result.getQuantity(),
                result.getTradingPair()
        );
    }
}
