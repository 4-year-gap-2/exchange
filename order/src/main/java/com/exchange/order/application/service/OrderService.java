package com.exchange.order.application.service;

import com.exchange.order.application.command.CreateOrderCommand;
import com.exchange.order.application.result.FindCancelResult;
import com.exchange.order.application.result.FindOrderResult;
import com.exchange.order.presentation.request.CancelOrderRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface OrderService {
    FindOrderResult createOrder(CreateOrderCommand command);

    FindCancelResult cancelOrder(UUID userId, CancelOrderRequest cancelOrderRequest);
}
