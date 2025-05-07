package com.exchange.order.application.service;

import com.exchange.order.application.command.CreateOrderCommand;
import com.exchange.order.application.result.FindOrderResult;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface OrderService {
    FindOrderResult createOrder(CreateOrderCommand command);

    FindOrderResult cancelOrder(UUID userId, UUID orderId);
}
