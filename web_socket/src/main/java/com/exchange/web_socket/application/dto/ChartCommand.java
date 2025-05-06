package com.exchange.web_socket.application.dto;

import com.exchange.web_socket.infrastructure.dto.CompletedOrderChangeEvent;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class ChartCommand {

    private BigDecimal price;
    private BigDecimal quantity;
    private String tradingPair;

    @Override
    public String toString() {
        return "ChartCommand{" +
                "price=" + price +
                ", quantity=" + quantity +
                ", tradingPair='" + tradingPair + '\'' +
                '}';
    }

    public static ChartCommand from(CompletedOrderChangeEvent event) {
        return new ChartCommand(
                event.getAfter().getPrice().getValue(),
                event.getAfter().getQuantity().getValue(),
                event.getAfter().getTrading_pair().getValue()
        );
    }
}
