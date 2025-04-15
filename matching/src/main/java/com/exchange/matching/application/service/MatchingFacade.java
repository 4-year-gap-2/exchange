package com.exchange.matching.application.service;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.common.aop.TimeTrace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
@Slf4j
public class MatchingFacade {

    private final RedissonClient redissonClient;
    private final MatchingServiceV2 matchingServiceV2;
    private final MatchingServiceV4 matchingServiceV4;
    private final MatchingServiceV1A matchingServiceV1A;

    public void matchV1(CreateMatchingCommand createMatchingCommand) {
        matchingServiceV1A.matchOrders(createMatchingCommand);
    }

    @TimeTrace
    public void matchV2(CreateMatchingCommand createMatchingCommand) {
        final String lockName = createMatchingCommand.tradingPair() + createMatchingCommand.orderType() + ":lock";
        final RLock lock = redissonClient.getLock(lockName);

        try {
            if (createMatchingCommand.price().doubleValue() == 7500.00) throw new IllegalArgumentException();
            if (!lock.tryLock(100, 500, TimeUnit.MILLISECONDS)) {
                System.out.println("락 획득 실패");
                throw new IllegalArgumentException();
            }
            log.info("체결 시작");
            matchingServiceV2.matchOrders(createMatchingCommand);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (lock != null && lock.isLocked()) {
                lock.unlock();
            }
        }
    }

    public void matchV4(CreateMatchingCommand createMatchingCommand) {
        matchingServiceV4.matchOrders(createMatchingCommand);
    }
}
