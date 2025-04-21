package com.springcloud.user.application.command;

import com.springcloud.user.application.enums.OrderType;
import com.springcloud.user.infrastructure.dto.MatchCompensatorEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class UserBalanceRollBackCommand {
    private UUID orderId;
    private String tradingPair;
    private BigDecimal price;
    private BigDecimal quantity;
    private UUID userId;
    private OrderType orderType;
    public static UserBalanceRollBackCommand commandFromEvent(MatchCompensatorEvent event){
        return new UserBalanceRollBackCommand(
                event.getOrderId(),
                event.getTradingPair(),
                event.getPrice(),
                event.getQuantity(),
                event.getUserId(),
                event.getOrderType()
        );
    }
}
