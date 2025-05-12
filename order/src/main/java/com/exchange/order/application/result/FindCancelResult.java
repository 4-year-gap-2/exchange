package com.exchange.order.application.result;

import com.exchange.order.presentation.request.CancelOrderRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FindCancelResult {
    private String orderId; //주문 아이디
    private String userId; //유저 아이디
    private String orderType; // buy/sell
    private String quantity; // 수량
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
