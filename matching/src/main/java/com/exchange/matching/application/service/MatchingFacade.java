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


    @TimeTrace
    public String match(CreateMatchingCommand createMatchingCommand){
        final String lockName = createMatchingCommand.tradingPair() + createMatchingCommand.orderType() + ":lock";
        final RLock lock = redissonClient.getLock(lockName);
        String value = "";

        try {
            if (createMatchingCommand.price().doubleValue() == 7500.00) throw new IllegalArgumentException();
            if (!lock.tryLock(100, 500, TimeUnit.MILLISECONDS)) {
                System.out.println("락 획득 실패");
                throw new IllegalArgumentException();
            }
            log.info("체결 시작");
            value = matchingServiceV2.matchOrders(createMatchingCommand);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (lock != null && lock.isLocked()) {
                lock.unlock();
            }
        }
        return value;
    }
}
