package com.exchange.order_completed.application.service;

import com.exchange.order_completed.application.command.ChartCommand;
import com.exchange.order_completed.application.command.CreateMatchedOrderStoreCommand;
import com.exchange.order_completed.application.command.CreateTestOrderStoreCommand;
import com.exchange.order_completed.application.command.CreateUnmatchedOrderStoreCommand;
import com.exchange.order_completed.common.exception.DuplicateMatchedOrderInformationException;
import com.exchange.order_completed.common.exception.DuplicateUnmatchedOrderInformationException;
import com.exchange.order_completed.domain.cassandra.entity.MatchedOrder;
import com.exchange.order_completed.domain.cassandra.entity.OrderState;
import com.exchange.order_completed.domain.cassandra.entity.OrderType;
import com.exchange.order_completed.domain.cassandra.entity.UnmatchedOrder;
import com.exchange.order_completed.domain.postgres.entity.Chart;
import com.exchange.order_completed.domain.repository.MatchedOrderReader;
import com.exchange.order_completed.domain.repository.MatchedOrderStore;
import com.exchange.order_completed.domain.repository.UnmatchedOrderReader;
import com.exchange.order_completed.domain.repository.UnmatchedOrderStore;
import com.exchange.order_completed.infrastructure.postgres.repository.ChartRepositoryStore;
import com.exchange.order_completed.presentation.dto.PagedResult;
import com.exchange.order_completed.presentation.dto.TradeDataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCompletedService {

    private final MatchedOrderStore matchedOrderStore;
    private final MatchedOrderReader matchedOrderReader;
    private final UnmatchedOrderReader unmatchedOrderReader;
    private final UnmatchedOrderStore unmatchedOrderStore;
    private final ChartRepositoryStore chartRepositoryStore;

    public void completeOrderEach(CreateTestOrderStoreCommand command, Integer attempt) {
        MatchedOrder buyOrderEntity = command.toBuyOrderEntity();
        MatchedOrder sellOrderEntity = command.toSellOrderEntity();
        matchedOrderStore.save(buyOrderEntity);
        matchedOrderStore.save(sellOrderEntity);
    }

    public void completeOrderBatch(CreateTestOrderStoreCommand command, Integer attempt) {
        matchedOrderStore.saveBatch(command);
    }

    public void completeMatchedOrder(CreateMatchedOrderStoreCommand command, int shard, LocalDate yearMonthDate, Integer attempt) {
        MatchedOrder persistentMatchedOrder = matchedOrderReader.findMatchedOrder(command.userId(), shard, yearMonthDate, attempt);

        if (persistentMatchedOrder != null) {
            throw new DuplicateMatchedOrderInformationException("이미 저장된 체결 주문입니다. orderId: " + command.idempotencyId());
        }

        MatchedOrder newMatchedOrder = command.toEntity(shard, yearMonthDate);
        UnmatchedOrder persistentUnmatchedOrder = unmatchedOrderReader.findUnmatchedOrder(command.userId(), shard, yearMonthDate, command.orderId(), attempt);

        if (persistentUnmatchedOrder == null) {
            matchedOrderStore.save(newMatchedOrder);
        } else {
            updateUnmatchedOrderQuantity(newMatchedOrder, persistentUnmatchedOrder);
            // 카산드라 배치 쿼리 수행
            matchedOrderStore.saveMatchedOrderAndUpdateUnmatchedOrder(newMatchedOrder, persistentUnmatchedOrder);
        }
    }

    public void updateUnmatchedOrderQuantity(MatchedOrder matchedOrder, UnmatchedOrder unmatchedOrder) {
        BigDecimal matchedOrderQuantity = matchedOrder.getQuantity();
        BigDecimal unmatchedOrderQuantity = unmatchedOrder.getQuantity();

        // 체결된 주문의 수량이 미체결 주문의 수량보다 적은 경우
        if (matchedOrderQuantity.compareTo(unmatchedOrderQuantity) < 0) {
            // 체결된 주문의 수량을 미체결 주문의 수량에서 빼줌
            unmatchedOrder.setQuantity(unmatchedOrderQuantity.subtract(matchedOrderQuantity));

            // 체결된 주문의 수량이 미체결 주문의 수량과 같은 경우
        } else if (matchedOrderQuantity.compareTo(unmatchedOrderQuantity) == 0) {
            // 미체결 주문의 수량을 0으로 설정
            unmatchedOrder.setQuantity(BigDecimal.ZERO);
        }
    }

    public void completeUnmatchedOrder(CreateUnmatchedOrderStoreCommand command, LocalDate yearMonthDate, int shard, Integer attempt) {
        UnmatchedOrder persistentMatchedOrder = unmatchedOrderReader.findUnmatchedOrder(command.userId(), shard, yearMonthDate, command.orderId(), attempt);

        if (persistentMatchedOrder != null) {
            throw new DuplicateUnmatchedOrderInformationException("이미 저장된 미체결 주문입니다. orderId: " + command.orderId());
        }

        UnmatchedOrder newUnmatchedOrder = command.toEntity(shard, yearMonthDate);
//        com.exchange.order_completed.domain.mongodb.entity.UnmatchedOrder newUnmatchedOrder = command.toMongoEntity();
        unmatchedOrderStore.save(newUnmatchedOrder);
    }

    @Transactional
    public void saveChart(ChartCommand command) {
        Chart chart = Chart.from(command);
        chartRepositoryStore.save(chart);
    }

    public PagedResult<TradeDataResponse> findMatchedOrderHistory(
            UUID userId,
            Instant cursor,
            int size,
            String orderType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        // 1. 날짜 범위 계산
        LocalDate today = LocalDate.now();
        LocalDate fromDate; //조회 시작일
        LocalDate toDate; //조회 마지막일

        if (startDate == null && endDate == null) {
            // 1) 시작일과 종료일이 모두 없는 경우: 오늘 하루만 조회
            fromDate = today;
            toDate = today;
        } else if (startDate != null && endDate == null) {
            // 2) 시작일만 있는 경우: 시작일부터 최대 3개월 조회
            fromDate = startDate;
            // 시작일 + 3개월이 오늘을 넘으면 오늘까지, 아니면 시작일 + 3개월까지
            toDate = startDate.plusMonths(3).isAfter(today) ? today : startDate.plusMonths(3);
        } else if (startDate == null && endDate != null) {
            // 3) 종료일만 있는 경우: 종료일 기준 3개월 전부터 종료일까지 조회
            toDate = endDate;
            // 종료일 - 3개월이 시스템 최소 날짜(예: 2000-01-01)보다 이전이면 최소 날짜부터, 아니면 종료일 - 3개월부터
            fromDate = endDate.minusMonths(3).isBefore(LocalDate.of(2000, 1, 1))
                    ? LocalDate.of(2020, 1, 1)
                    : endDate.minusMonths(3);
        } else {
            // 4) 시작일과 종료일이 모두 있는 경우: 해당 구간 전체 조회
            fromDate = startDate;
            toDate = endDate;
        }

        // 2. shard 값 준비 (1, 2, 3)
        int shard1 = 1, shard2 = 2, shard3 = 3;

        // 3. Repository에서 한 번에 범위 조회
        List<MatchedOrder> allOrders = matchedOrderReader.findByUserIdAndShardInAndYearMonthDateRange(
                userId, shard1, shard2, shard3, fromDate, toDate
        );

        // ordertype이 있다면? 앱에서 필터링
        if (orderType != null) {
            allOrders = allOrders.stream()
                    .filter(order -> orderType.equalsIgnoreCase(order.getOrderType()))
                    .toList();
        }

        // 4. 커서/정렬/페이징 처리
        List<MatchedOrder> filtered = allOrders.stream()
                .filter(order -> cursor == null || order.getCreatedAt().isBefore(cursor))
                .limit(size + 1)
                .collect(Collectors.toList());

        boolean hasNext = filtered.size() > size;
        List<MatchedOrder> pageData = hasNext ?
                filtered.subList(0, size) :
                filtered;

        List<TradeDataResponse> responseList = pageData.stream()
                .map(TradeDataResponse::fromMatchedEntity)
                .collect(Collectors.toList());

        Instant nextCursor = responseList.isEmpty() ?
                null :
                responseList.get(responseList.size() - 1).getCreatedAt();

        return new PagedResult<>(
                responseList,
                nextCursor != null ? LocalDateTime.ofInstant(nextCursor, ZoneId.systemDefault()) : null,
                hasNext
        );
    }

    public PagedResult<TradeDataResponse> findUnmatchedOrderHistory(UUID userId, Instant cursor, int size, String orderType, LocalDate startDate, LocalDate endDate, String orderState) {
        // 1. 날짜 범위 계산
        LocalDate today = LocalDate.now();
        LocalDate fromDate; //조회 시작일
        LocalDate toDate; //조회 마지막일

        if (startDate == null && endDate == null) {
            // 1) 시작일과 종료일이 모두 없는 경우: 오늘 하루만 조회
            fromDate = today;
            toDate = today;
        } else if (startDate != null && endDate == null) {
            // 2) 시작일만 있는 경우: 시작일부터 최대 3개월 조회
            fromDate = startDate;
            // 시작일 + 3개월이 오늘을 넘으면 오늘까지, 아니면 시작일 + 3개월까지
            toDate = startDate.plusMonths(3).isAfter(today) ? today : startDate.plusMonths(3);
        } else if (startDate == null && endDate != null) {
            // 3) 종료일만 있는 경우: 종료일 기준 3개월 전부터 종료일까지 조회
            toDate = endDate;
            // 종료일 - 3개월이 시스템 최소 날짜(예: 2000-01-01)보다 이전이면 최소 날짜부터, 아니면 종료일 - 3개월부터
            fromDate = endDate.minusMonths(3).isBefore(LocalDate.of(2000, 1, 1))
                    ? LocalDate.of(2020, 1, 1)
                    : endDate.minusMonths(3);
        } else {
            // 4) 시작일과 종료일이 모두 있는 경우: 해당 구간 전체 조회
            fromDate = startDate;
            toDate = endDate;
        }

        // 2. shard 값 준비 (1, 2, 3)
        int shard1 = 1, shard2 = 2, shard3 = 3;

        // 3. Repository에서 한 번에 범위 조회
        List<UnmatchedOrder> allOrders = unmatchedOrderReader.findByUserIdAndShardInAndYearMonthDateRange(
                userId, shard1, shard2, shard3, fromDate, toDate
        );

        // ordertype이 있다면? 앱에서 필터링
        if (orderType != null) {
            allOrders = allOrders.stream()
                    .filter(order -> order.getOrderType() != null &&
                            orderType.equalsIgnoreCase(order.getOrderType()))
                    .toList();
        }

        if (orderState != null) {
            allOrders = allOrders.stream()
                    .filter(order -> order.getOrderState() != null &&
                            orderState.equalsIgnoreCase(order.getOrderState().name()))
                    .toList();
        }

        // 4. 커서/정렬/페이징 처리
        List<UnmatchedOrder> filtered = allOrders.stream()
                .filter(order -> cursor == null || order.getCreatedAt().isBefore(cursor))
                .limit(size + 1)
                .collect(Collectors.toList());

        boolean hasNext = filtered.size() > size;
        List<UnmatchedOrder> pageData = hasNext ?
                filtered.subList(0, size) :
                filtered;

        List<TradeDataResponse> responseList = pageData.stream()
                .map(TradeDataResponse::fromUnmatchedEntity)
                .collect(Collectors.toList());

        Instant nextCursor = responseList.isEmpty() ?
                null :
                responseList.get(responseList.size() - 1).getCreatedAt();

        return new PagedResult<>(
                responseList,
                nextCursor != null ? LocalDateTime.ofInstant(nextCursor, ZoneId.systemDefault()) : null,
                hasNext
        );

    }
}
