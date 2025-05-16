package com.exchange.order.application.service;

import com.exchange.order.application.command.CreateOrderCommand;
import com.exchange.order.application.result.FindCancelResult;
import com.exchange.order.application.result.FindOrderResult;
import com.exchange.order.presentation.request.CancelOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderCommandService orderCommandService;

    @Override
    public FindOrderResult createOrder(CreateOrderCommand command) {
        return orderCommandService.createOrder(command);
    }

    @Override
    public FindCancelResult cancelOrder(UUID userId, CancelOrderRequest cancelOrderRequest) {
        return orderCommandService.cancelOrder(userId, cancelOrderRequest);
    }
}
