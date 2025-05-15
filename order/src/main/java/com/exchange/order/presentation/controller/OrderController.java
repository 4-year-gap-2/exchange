package com.exchange.order.presentation.controller;

import com.exchange.order.application.command.CreateOrderCommand;
import com.exchange.order.application.result.FindCancelResult;
import com.exchange.order.application.result.FindOrderResult;
import com.exchange.order.application.service.OrderService;
import com.exchange.order.common.UserInfoHeader;
import com.exchange.order.common.response.ResponseDto;
import com.exchange.order.presentation.request.CancelOrderRequest;
import com.exchange.order.presentation.request.CreateOrderRequest;
import com.exchange.order.presentation.response.CancelOrderResponse;
import com.exchange.order.presentation.response.CreateOrderResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Description;
import org.springframework.http.HttpStatus;
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

    @Description("주문")
    @PostMapping
    public CreateOrderResponse createOrder(HttpServletRequest request, @RequestBody CreateOrderRequest orderRequest) {
        // 권한 체크
        UserInfoHeader userInfo = new UserInfoHeader(request);
        CreateOrderCommand command = orderRequest.toCommand(userInfo.getUserId());
        FindOrderResult result = orderService.createOrder(command);
        CreateOrderResponse response = CreateOrderResponse.fromResponse(result);
        return ResponseEntity.ok(response).getBody();
    }
    @Description("주문 취소")
    @PostMapping("/cancel")
    public ResponseEntity<ResponseDto<CancelOrderResponse>> cancelOrder(HttpServletRequest request, @RequestBody CancelOrderRequest cancelOrderRequest) {
        UserInfoHeader userInfo = new UserInfoHeader(request);
        FindCancelResult result = orderService.cancelOrder(userInfo.getUserId(), cancelOrderRequest);
        CancelOrderResponse response = CancelOrderResponse.fromResult(result);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success(response));

    }
}
