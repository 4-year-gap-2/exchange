package com.exchange.order_completed.application.command;

import com.exchange.order_completed.domain.cassandra.entity.OrderType;
import com.exchange.order_completed.infrastructure.dto.CompletedOrderChangeEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChartCommand {

    private BigDecimal price;

    private BigDecimal amount;

    private String pair; // 예: "BTC-USD"

    private OrderType transactionType;

    private LocalDateTime createdAt;

    public static ChartCommand fromEvent(CompletedOrderChangeEvent value) {
        BigDecimal price = null;
        if (value.getAfter().getPrice() != null && value.getAfter().getPrice().getValue() != null) {
            try {
                price = new BigDecimal(String.valueOf(value.getAfter().getPrice().getValue()));
            } catch (NumberFormatException e) {
                // Handle the case where the price value is not a valid number
                System.err.println("Error parsing price: " + value.getAfter().getPrice().getValue());
                price = BigDecimal.ZERO; // Or some other default value/error handling
            }
        }

        BigDecimal amount = null;
        if (value.getAfter().getQuantity() != null && value.getAfter().getQuantity().getValue() != null) {
            try {
                amount = new BigDecimal(String.valueOf(value.getAfter().getQuantity().getValue()));
            } catch (NumberFormatException e) {
                // Handle the case where the quantity value is not a valid number
                System.err.println("Error parsing quantity: " + value.getAfter().getQuantity().getValue());
                amount = BigDecimal.ZERO;
            }
        }

        String pair = null;
        if (value.getAfter().getTrading_pair() != null && value.getAfter().getTrading_pair().getValue() != null) {
            pair = value.getAfter().getTrading_pair().getValue().toString();
        }

        LocalDateTime createdAt = null;
        if (value.getAfter().getCreated_at() != null && value.getAfter().getCreated_at().getValue() != null) {
            try {
                long createdAtMillis = Long.parseLong(String.valueOf(value.getAfter().getCreated_at().getValue()));
                createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAtMillis), ZoneOffset.UTC);
            } catch (NumberFormatException e) {

                System.err.println("Error parsing createdAt: " + value.getAfter().getCreated_at().getValue());
                createdAt = LocalDateTime.now(ZoneOffset.UTC);
            }
        }

        return new ChartCommand(
                price,
                amount,
                pair,
                OrderType.valueOf(value.getAfter().getType().getValue()),
                createdAt
        );
    }
}