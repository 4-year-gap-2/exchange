package com.exchange.order.application.service;

import com.exchange.order.application.command.CreateOrderCommand;
import com.exchange.order.application.command.OrderCommandService;
import com.exchange.order.application.result.FindOrderResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderCommandService orderCommandService;

    @Override
    public FindOrderResult createOrder(CreateOrderCommand command) {
        return orderCommandService.createOrder(command);
    }
}
