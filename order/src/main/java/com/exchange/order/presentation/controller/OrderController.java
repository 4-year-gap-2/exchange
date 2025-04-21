package com.exchange.order.presentation.controller;

import com.exchange.order.application.command.CreateOrderCommand;
import com.exchange.order.application.result.FindOrderResult;
import com.exchange.order.application.service.OrderService;
import com.exchange.order.common.UserInfoHeader;
import com.exchange.order.presentation.request.CreateOrderRequest;
import com.exchange.order.presentation.response.CreateOrderResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    @PostMapping
    public CreateOrderResponse creteOrder(HttpServletRequest request, @RequestBody CreateOrderRequest orderRequest) {
        // 권한 체크
        UserInfoHeader userInfo = new UserInfoHeader(request);
        CreateOrderCommand command = orderRequest.toCommand(userInfo.getUserId());
        FindOrderResult result = orderService.createOrder(command);
        CreateOrderResponse response = CreateOrderResponse.fromResponse(result);
        return ResponseEntity.ok(response).getBody();
    }
}
