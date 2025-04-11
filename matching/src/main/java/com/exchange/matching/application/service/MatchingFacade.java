package com.exchange.matching.application.service;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.service.MatchingService;
import com.exchange.matching.domain.service.MatchingServiceV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Comment;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static org.apache.el.parser.ELParserConstants.EMPTY;

@RequiredArgsConstructor
@Component
@Slf4j
public class MatchingFacade {
    private final MatchingServiceV2 matchingServiceV2;
    private final RedissonClient redissonClient;


    public void match(CreateMatchingCommand createMatchingCommand){
        final String lockName = createMatchingCommand.tradingPair() + createMatchingCommand.orderType() + ":lock";
        final RLock lock = redissonClient.getLock(lockName);
        final String worker = Thread.currentThread().getName();

        try {
            if (!lock.tryLock(1, 5, TimeUnit.SECONDS)) {
//                throw new IllegalArgumentException();
//                 findMatchingOrder(stockCode,orderType); 다음 조건 주문으로 조회
                System.out.println("다음 주문 조회");
            }
            log.info("현재 진행중인 스레드 : {}", worker);
            matchingServiceV2.matchOrders(createMatchingCommand);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (lock != null && lock.isLocked()) {
                lock.unlock();
            }
        }


    }

}
