package com.exchange.order.presentation.request;

import com.exchange.order.application.command.CreateOrderCommand;

import com.exchange.order.application.enums.OrderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class CreateOrderRequest {
    private OrderType orderType; // buy/sell
    @DecimalMin("0.0001")
    private BigDecimal price; //총 가격
    @DecimalMin("0.0001")
    private BigDecimal quantity; // 수량
    @Pattern(regexp = "^[A-Z]{3,4}/[A-Z]{3,4}$", message = "거래 쌍은 'BTC/USD'와 같은 형식이어야 합니다")
    private String tradingPair; //거래소 명칭

    public CreateOrderCommand toCommand(UUID userId) {
        return CreateOrderCommand.builder()
                .orderId(UUID.randomUUID())
                .userId(userId)
                .orderType(orderType)
                .price(price)
                .quantity(quantity)
                .tradingPair(tradingPair).build();
    }
}
