package com.exchange.order_completed.presentation.external;

import com.exchange.order_completed.application.TimeInterval;
import com.exchange.order_completed.application.service.OrderCompletedService;
import com.exchange.order_completed.application.service.TradeService;
import com.exchange.order_completed.common.UserInfoHeader;
import com.exchange.order_completed.common.response.ResponseDto;
import com.exchange.order_completed.domain.cassandra.entity.OrderState;
import com.exchange.order_completed.domain.cassandra.entity.OrderType;
import com.exchange.order_completed.domain.postgres.entity.TradeDataInfo;
import com.exchange.order_completed.presentation.dto.PagedResult;
import com.exchange.order_completed.presentation.dto.TradeDataResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TradeService tradeService;
    private final OrderCompletedService completedService;

    @GetMapping(value = "/chart/{pair}/{interval}")
    public ResponseEntity<ResponseDto<List<TradeDataInfo>>> getChartData(@PathVariable(value = "pair") String pair, @PathVariable(value = "interval") TimeInterval interval) {

        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success(tradeService.getTradeInfo(pair,interval)));
    }

    //체결 주문 조회
    @GetMapping("/matched")
    public ResponseEntity<ResponseDto<PagedResult<TradeDataResponse>>> findMatchedOrderHistory(
            HttpServletRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "BUY", required = false) OrderType orderType,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        UserInfoHeader userInfo = new UserInfoHeader(request);

        Instant cursorInstant = null;
        if (cursor != null) {
            cursorInstant = cursor.atZone(ZoneId.systemDefault()).toInstant();
        }
        PagedResult<TradeDataResponse> pagedResult = completedService.findMatchedOrderHistory(userInfo.getUserId(), cursorInstant, size, orderType, startDate, endDate);

        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success(pagedResult));

    }

    //체결 주문 조회
    @GetMapping("/unmatched")
    public ResponseEntity<ResponseDto<PagedResult<TradeDataResponse>>> findUnmatchedOrderHistory(
            HttpServletRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "BUY", required = false) OrderType orderType,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "PENDING", required = false) OrderState orderState
    ) {
        UserInfoHeader userInfo = new UserInfoHeader(request);

        Instant cursorInstant = null;
        if (cursor != null) {
            cursorInstant = cursor.atZone(ZoneId.systemDefault()).toInstant();
        }
        PagedResult<TradeDataResponse> pagedResult = completedService.findUnmatchedOrderHistory(userInfo.getUserId(), cursorInstant, size, orderType, startDate, endDate, String.valueOf(orderState));

        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success(pagedResult));

    }
}