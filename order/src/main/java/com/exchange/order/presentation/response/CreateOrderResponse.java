package com.exchange.order.presentation.response;

import com.exchange.order.application.enums.OrderType;
import com.exchange.order.application.result.FindOrderResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CreateOrderResponse {
    private UUID orderId; //주문 아이디
    private UUID userId; //유저 아이디
    private OrderType orderType; // buy/sell
    private BigDecimal price; //총 가격
    private BigDecimal quantity; // 수량
    private String tradingPair; //거래소 명칭

    public static CreateOrderResponse fromResponse(FindOrderResult result) {
        return new CreateOrderResponse(
                result.getOrderId(),
                result.getUserId(),
                result.getOrderType(),
                result.getPrice(),
                result.getQuantity(),
                result.getTradingPair()
        );
    }
}
