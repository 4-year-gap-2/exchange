package com.exchange.order_completed.application.command;

import com.exchange.order_completed.presentation.dto.TradeDataRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TradeDataCommand {
    private String tradingPair;
    private String orderType;
    private BigDecimal price;
    private BigDecimal quantity;



    public static TradeDataCommand fromRequest(TradeDataRequest request) {
        return new TradeDataCommand(
                request.getTradingPair(),
                request.getOrderType(),
                request.getPrice(),
                request.getQuantity());
    }
}
