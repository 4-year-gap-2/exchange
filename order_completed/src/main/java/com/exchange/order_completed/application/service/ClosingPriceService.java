package com.exchange.order_completed.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClosingPriceService {

    private final CassandraOperations cassandraTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String CLOSING_PRICE_KEY_PREFIX = "market:closing_price:";
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    /**
     * 일일 종가 설정 스케줄러
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul") // 한국 시간 자정
    public void updateDailyClosingPrices() {
        // 한국 시간으로 어제 날짜 계산
        LocalDate yesterday = LocalDate.now(KST_ZONE).minusDays(1);
        List<String> tradingPairs = getAllActiveTradingPairs();

        for (String tradingPair : tradingPairs) {
            try {
                updateClosingPriceForPair(tradingPair, yesterday);
            } catch (Exception e) {
                log.error("{}의 종가 업데이트 중 오류 발생: {}", tradingPair, e.getMessage(), e);
            }
        }

        log.info("일일 종가 업데이트 완료");
    }

    /**
     * 특정 거래쌍의 종가 업데이트
     * 중요: 그날 거래가 없으면 이전 종가 유지
     */
    private void updateClosingPriceForPair(String tradingPair, LocalDate date) {
        // 1. 해당 날짜의 마지막 거래 조회 (한국 시간 기준)
        BigDecimal lastTradePrice = findLastTradePrice(tradingPair, date);

        // 2. 해당 날짜에 거래가 없는 경우
        if (lastTradePrice == null) {
            log.info("{} 날짜에 {}의 거래 내역이 없습니다 (한국 시간). 이전 종가를 유지합니다.", date, tradingPair);

            // 2.1. 기존 종가 유지 (Redis에서 확인)
            String existingPrice = redisTemplate.opsForValue().get(CLOSING_PRICE_KEY_PREFIX + tradingPair);

            if (existingPrice != null) {
                // 종가는 그대로지만, 마지막 날짜는 업데이트 하지 않음 (이전 날짜 유지)
                log.info("{}의 이전 종가 {}를 유지합니다", tradingPair, existingPrice);
                return;
            } else {
                // Redis에 종가가 없는 경우, Cassandra에서 마지막 종가 조회
                BigDecimal lastStoredClosingPrice = findLastStoredClosingPrice(tradingPair);

                if (lastStoredClosingPrice != null) {
                    redisTemplate.opsForValue().set(
                            CLOSING_PRICE_KEY_PREFIX + tradingPair,
                            lastStoredClosingPrice.toString()
                    );
                    log.info("{}의 마지막 저장된 종가 {}를 검색하여 가져왔습니다",
                            tradingPair, lastStoredClosingPrice);
                    return;
                } else {
                    // 이전 종가도 없는 경우 처리 (초기 상태 등)
                    log.warn("{}에 대한 종가 이력이 없습니다. 기본값으로 설정합니다.", tradingPair);
                    // 기본값 또는 다른 대체 가격 사용 (예: 현재 주문서의 중간가 등)
                    return;
                }
            }
        }

        // 3. 새 종가 저장 (Redis)
        redisTemplate.opsForValue().set(
                CLOSING_PRICE_KEY_PREFIX + tradingPair,
                lastTradePrice.toString()
        );

        // 4. Cassandra에 종가 이력 저장
        saveClosingPriceHistory(tradingPair, date, lastTradePrice);

        log.info("{}의 종가를 {} (날짜: {}, 한국 시간)로 업데이트했습니다", tradingPair, lastTradePrice, date);
    }

    /**
     * 특정 날짜의 마지막 거래 가격 조회 (한국 시간 기준)
     */
    private BigDecimal findLastTradePrice(String tradingPair, LocalDate date) {
        // 한국 시간으로 날짜 범위 설정
        ZonedDateTime startOfDayKST = date.atStartOfDay(KST_ZONE);
        ZonedDateTime endOfDayKST = date.atTime(23, 59, 59).atZone(KST_ZONE);

        // UTC로 변환
        LocalDateTime startOfDayUTC = startOfDayKST.toLocalDateTime();
        LocalDateTime endOfDayUTC = endOfDayKST.toLocalDateTime();

        // UTC 시간을 Instant로 변환
        Instant startInstant = startOfDayUTC.toInstant(ZoneOffset.UTC);
        Instant endInstant = endOfDayUTC.toInstant(ZoneOffset.UTC);

        try {
            // 인덱스를 사용한 쿼리
            String cql = "SELECT price, created_at FROM matched_order " +
                    "WHERE trading_pair = ? AND year_month_date = ? ALLOW FILTERING";

            List<Map<String, Object>> trades = cassandraTemplate.getCqlOperations()
                    .queryForList(cql, tradingPair, date);

            if (trades.isEmpty()) {
                return null;
            }

            // 시간 필터링 및 최신 거래 찾기
            return trades.stream()
                    .filter(trade -> {
                        Instant createdAt = (Instant) trade.get("created_at");
                        return !createdAt.isBefore(startInstant) && !createdAt.isAfter(endInstant);
                    })
                    .max(Comparator.comparing(trade -> (Instant) trade.get("created_at")))
                    .map(trade -> (BigDecimal) trade.get("price"))
                    .orElse(null);
        } catch (Exception e) {
            log.error("마지막 거래 가격 조회 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 마지막으로 저장된 종가 조회
     */
    private BigDecimal findLastStoredClosingPrice(String tradingPair) {
        // 종가 이력 테이블에서 마지막 종가 조회
        String cql = "SELECT price FROM closing_price_history " +
                "WHERE trading_pair = ? ORDER BY date DESC LIMIT 1";

        try {
            List<Map<String, Object>> results = cassandraTemplate.getCqlOperations()
                    .queryForList(cql, tradingPair);

            if (!results.isEmpty()) {
                Map<String, Object> row = results.get(0);
                Object priceObj = row.get("price");
                if (priceObj != null) {
                    if (priceObj instanceof BigDecimal) {
                        return (BigDecimal) priceObj;
                    } else {
                        return new BigDecimal(priceObj.toString());
                    }
                }
            }
        } catch (Exception e) {
            log.error("마지막 저장된 종가 조회 중 오류 발생: {}", e.getMessage(), e);
        }

        return null;
    }

    /**
     * 종가 이력 저장
     */
    private void saveClosingPriceHistory(String tradingPair, LocalDate date, BigDecimal price) {
        String cql = "INSERT INTO closing_price_history (trading_pair, date, price, created_at) " +
                "VALUES (?, ?, ?, ?)";

        try {
            cassandraTemplate.getCqlOperations().execute(
                    cql,
                    tradingPair,
                    date,
                    price,
                    Instant.now()
            );

            log.info("{}의 종가 이력 저장 완료 (날짜: {}, 가격: {})", tradingPair, date, price);
        } catch (Exception e) {
            log.error("종가 이력 저장 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 모든 활성 거래쌍 조회
     */
    private List<String> getAllActiveTradingPairs() {
        // TODO Mysql의 코인엔티티로 페어 만들어서 가져오기
        List<String> defaultPairs = Arrays.asList("BTC/KRW", "ETH/KRW");
        log.warn("기본 목록을 사용합니다: {}", defaultPairs);
        return defaultPairs;
    }
}