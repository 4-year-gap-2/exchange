package com.exchange.matching;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.application.service.MatchingFacade;
import com.exchange.matching.application.service.MatchingServiceV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;

@SpringBootTest
class LockTest {

    private RedissonClient redissonClient;
    private MatchingServiceV2 matchingService;
    private MatchingFacade matchingFacade;
    private CreateMatchingCommand testCommand;

    @BeforeEach
    void setUp() {
        // 테스트용 Redisson 클라이언트 (로컬 메모리 기반)

        // Mocked MatchingService
        matchingService = Mockito.mock(MatchingServiceV2.class);
//        matchingFacade = new MatchingFacade(matchingService, redissonClient);

        // 테스트용 주문 객체
        testCommand = new CreateMatchingCommand(
                "BTC-USD", OrderType.BUY, new BigDecimal("10000"), new BigDecimal("1"), UUID.randomUUID()
        );
    }

    @Test
    void testDistributedLockingBehavior() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            matchingFacade.match(testCommand);
        }, "Thread-1");

        Thread t2 = new Thread(() -> {
            matchingFacade.match(testCommand);
        }, "Thread-2");

        t1.start();
        Thread.sleep(100); // 락 먼저 잡도록 약간 대기
        t2.start();

        t1.join();
        t2.join();

        // matchOrders는 한번만 호출돼야 함 (락이 한 번만 허용되므로)
        verify(matchingService, times(1)).matchOrders(testCommand);
    }
}
