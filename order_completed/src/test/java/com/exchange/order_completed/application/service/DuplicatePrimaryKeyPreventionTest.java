package com.exchange.order_completed.application.service;

import com.exchange.order_completed.domain.entity.MatchedOrder;
import com.exchange.order_completed.infrastructure.cassandra.repository.MatchedOrderReaderRepository;
import com.exchange.order_completed.infrastructure.cassandra.repository.MatchedOrderStoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DuplicatePrimaryKeyPreventionTest {

    @Autowired
    private MatchedOrderReaderRepository readerRepository;

    @Autowired
    private MatchedOrderStoreRepository storeRepository;

    private static final LocalDateTime dateTime = LocalDateTime.now();

    private static final UUID orderId = UUID.randomUUID();

    private static final UUID userId = UUID.randomUUID();


    private MatchedOrder buildOrder() {
        return MatchedOrder.builder()
                .userId(userId)
                .orderId(orderId)
                .createdAt(dateTime)
                .price(BigDecimal.TEN)
                .quantity(BigDecimal.ONE)
                .orderType("BUY")
                .tradingPair("BTC-USD")
                .build();
    }

    @Test
    @DisplayName("동일한 PK를 가진 주문을 저장하려고 시도하면 하나만 저장되어야 한다.")
    void shouldPersistOnlyOneRowWhenSavingWithDuplicatePrimaryKey() throws InterruptedException {
        MatchedOrder order1 = buildOrder();
        MatchedOrder order2 = buildOrder();
        order2.setQuantity(BigDecimal.valueOf(10000));  // Key가 아닌 필드가 다른 값을 가질 경우 갱신되는지 확인

        storeRepository.save(order1);
        Thread.sleep(5000);
        storeRepository.save(order2);

        List<MatchedOrder> matchedOrderList = readerRepository.findAll();

        assertEquals(1, matchedOrderList.size(), "Order1, 2의 Key 3개가 동일하기 때문에 1개만 저장되어야 한다.");
    }
}
