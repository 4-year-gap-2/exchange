package com.exchange.order.application.service;

import com.exchange.order.application.command.CreateOrderCommand;
import com.exchange.order.application.result.FindOrderResult;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {
    FindOrderResult createOrder(CreateOrderCommand command);
}
