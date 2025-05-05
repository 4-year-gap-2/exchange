package com.exchange.order_completed.application.service;

import com.exchange.order_completed.application.command.ChartCommand;
import com.exchange.order_completed.application.command.CreateMatchedOrderStoreCommand;
import com.exchange.order_completed.application.command.CreateTestOrderStoreCommand;
import com.exchange.order_completed.application.command.CreateUnmatchedOrderStoreCommand;
import com.exchange.order_completed.common.exception.DuplicateMatchedOrderInformationException;
import com.exchange.order_completed.common.exception.DuplicateUnmatchedOrderInformationException;
import com.exchange.order_completed.domain.entity.MatchedOrder;
import com.exchange.order_completed.domain.entity.UnmatchedOrder;
import com.exchange.order_completed.domain.postgresEntity.Chart;
import com.exchange.order_completed.domain.repository.MatchedOrderReader;
import com.exchange.order_completed.domain.repository.MatchedOrderStore;
import com.exchange.order_completed.domain.repository.UnmatchedOrderReader;
import com.exchange.order_completed.domain.repository.UnmatchedOrderStore;
import com.exchange.order_completed.infrastructure.postgesql.repository.ChartRepositoryStore;
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
import java.util.Comparator;
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
    private final OrderCacheService orderCacheService;

    public void completeOrderEach(CreateTestOrderStoreCommand command, Integer attempt) {
        MatchedOrder buyOrderEntity = command.toBuyOrderEntity();
        MatchedOrder sellOrderEntity = command.toSellOrderEntity();
        matchedOrderStore.save(buyOrderEntity);
        matchedOrderStore.save(sellOrderEntity);
    }

    public void completeOrderBatch(CreateTestOrderStoreCommand command, Integer attempt) {
        matchedOrderStore.saveBatch(command);
    }

    public void completeMatchedOrder(CreateMatchedOrderStoreCommand command, LocalDate yearMonthDate, Integer attempt) {
        MatchedOrder persistentMatchedOrder = matchedOrderReader.findMatchedOrder(command.userId(), yearMonthDate, command.idempotencyId(), attempt);

        if (persistentMatchedOrder != null) {
            throw new DuplicateMatchedOrderInformationException("이미 저장된 체결 주문입니다. orderId: " + command.idempotencyId());
        }

        MatchedOrder newMatchedOrder = command.toEntity(yearMonthDate);
        UnmatchedOrder persistentUnmatchedOrder = unmatchedOrderReader.findUnmatchedOrder(command.userId(), command.orderId(), attempt);

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

    public void completeUnmatchedOrder(CreateUnmatchedOrderStoreCommand command, Integer attempt) {
        UnmatchedOrder persistentMatchedOrder = unmatchedOrderReader.findUnmatchedOrder(command.userId(), command.orderId(), attempt);

        if (persistentMatchedOrder != null) {
            throw new DuplicateUnmatchedOrderInformationException("이미 저장된 미체결 주문입니다. orderId: " + command.orderId());
        }

        UnmatchedOrder newUnmatchedOrder = command.toEntity();
        unmatchedOrderStore.save(newUnmatchedOrder);
    }

    @Transactional
    public void saveChart(ChartCommand command) {
        Chart chart = Chart.from(command);
        chartRepositoryStore.save(chart);
    }

    public PagedResult<TradeDataResponse> findTradeOrderHistory(
            UUID userId,
            Instant cursor,
            int size,
            String orderType
    ) {
        List<MatchedOrder> allOrders = orderCacheService.getCachedOrders(userId);
        log.info("사용자 {}의 전체 주문 {}건 조회 완료", userId, allOrders.size());

        List<MatchedOrder> filtered = allOrders.parallelStream()
                .filter(order -> cursor == null || order.getCreatedAt().isBefore(cursor))
                .filter(order -> order.getOrderType().equalsIgnoreCase(orderType))
                .sorted(Comparator.comparing(MatchedOrder::getCreatedAt).reversed())
                .limit(size + 1)
                .collect(Collectors.toList());

        boolean hasNext = filtered.size() > size;
        List<MatchedOrder> pageData = hasNext ?
                filtered.subList(0, size) :
                filtered;

        List<TradeDataResponse> responseList = pageData.stream()
                .map(TradeDataResponse::fromEntity)
                .collect(Collectors.toList());

        Instant nextCursor = responseList.isEmpty() ?
                null :
                responseList.get(responseList.size() - 1).getCreatedAt();

        return new PagedResult<>(
                responseList,
                nextCursor != null ?
                        LocalDateTime.ofInstant(nextCursor, ZoneId.systemDefault()) :
                        null,
                hasNext
        );
    }
}
