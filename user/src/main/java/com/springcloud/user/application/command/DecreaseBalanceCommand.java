package com.springcloud.user.application.command;

import com.springcloud.user.application.enums.OrderType;
import com.springcloud.user.infrastructure.dto.KafkaUserBalanceDecreaseEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class DecreaseBalanceCommand {
    private UUID orderId;
    private UUID userId; //유저 아이디
    private OrderType orderType; // buy/sell
    private BigDecimal price; //총 가격
    private BigDecimal quantity; // 수량
    private String tradingPair; //거래소 명칭

    public static DecreaseBalanceCommand commandFromEvent(KafkaUserBalanceDecreaseEvent event) {

        return new DecreaseBalanceCommand(
                event.getOrderId(),
                event.getUserId(),
                event.getOrderType(),
                event.getPrice(),
                event.getQuantity(),
                event.getTradingPair());
    }
}
