package com.exchange.order_completed.application.service;

import com.exchange.order_completed.application.command.CreateOrderStoreCommand;
import com.exchange.order_completed.common.exception.DuplicateOrderCompletionException;
import com.exchange.order_completed.domain.entity.CompletedOrder;
import com.exchange.order_completed.domain.postgresEntity.Chart;
import com.exchange.order_completed.domain.repository.CompletedOrderReader;
import com.exchange.order_completed.domain.repository.CompletedOrderStore;
import com.exchange.order_completed.infrastructure.dto.KafkaBalanceIncreaseEvent;
import com.exchange.order_completed.infrastructure.external.KafkaEventPublisher;
import com.exchange.order_completed.infrastructure.postgesql.repository.ChartRepositoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.WriteResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCompletedFacade {

    private final CompletedOrderStore completedOrderStore;
    private final CompletedOrderReader completedOrderReader;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final ChartRepositoryStore chartRepositoryStore;
    private final CassandraTemplate cassandraTemplate;
    private final RedissonClient redissonClient;

    public void completeOrder(CreateOrderStoreCommand command, Integer attempt) {
        String lockKey = "order:" + command.orderId() + ":lock";
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;

        try {
            // 최대 5초 대기, 락 획득 시 30초 뒤 자동 해제
            acquired = lock.tryLock(5, 30, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("주문 완료 정보 저장을 위한 락 획득에 실패했습니다. orderId: " + command.orderId());
            }

            CompletedOrder persistentOrder = completedOrderReader.findByUserIdAndOrderId(command.userId(), command.orderId());
            if (persistentOrder != null) {
                throw new DuplicateOrderCompletionException("이미 완료된 주문입니다. orderId: " + command.orderId());
            }

            CompletedOrder newCompletedOrder = command.toEntity();
            Chart chart = command.toChartData();
            completedOrderStore.save(newCompletedOrder);
            chartRepositoryStore.save(chart);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("락 획득 중 인터럽트가 발생했습니다.", e);

        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        kafkaEventPublisher.publishMessage(KafkaBalanceIncreaseEvent.from(command));
    }

    @Deprecated(forRemoval = true)
    public void completeOrder2(CreateOrderStoreCommand command) {
        CompletedOrder newCompletedOrder = command.toEntity();

        // 경량 트랜잭션 LWT: IF NOT EXISTS 옵션 생성
        InsertOptions options = InsertOptions.builder()
                .withIfNotExists()
                .build();

        // 내부적으로 INSERT ~ IF NOT EXISTS 실행
        WriteResult result = cassandraTemplate.insert(newCompletedOrder, options);

        if (!result.wasApplied()) {
            log.info("이미 완료된 주문입니다. orderId={}", newCompletedOrder.getOrderId());
            return;
        }

        kafkaEventPublisher.publishMessage(KafkaBalanceIncreaseEvent.from(command));
    }
}
