package com.springcloud.user.application.command;

import com.springcloud.user.application.enums.OrderType;
import com.springcloud.user.infrastructure.dto.KafkaUserBalanceIncreaseEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class IncreaseBalanceCommand {

    private UUID orderId;
    private String tradingPair;
    private BigDecimal price;
    private BigDecimal quantity;
    private UUID buyer;
    private UUID seller;
    private OrderType orderType;

    public static IncreaseBalanceCommand commandFromEvent(KafkaUserBalanceIncreaseEvent event) {
        return new IncreaseBalanceCommand(
                event.getOrderId(),
                event.getTradingPair(),
                event.getPrice(),
                event.getQuantity(),
                event.getBuyer(),
                event.getSeller(),
                event.getOrderType());
    }
}
