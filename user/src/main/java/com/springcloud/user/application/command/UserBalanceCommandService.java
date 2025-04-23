package com.springcloud.user.application.command;

import com.springcloud.user.application.enums.OrderType;
import com.springcloud.user.application.result.FindUserBalanceResult;
import com.springcloud.user.domain.entity.Coin;
import com.springcloud.user.domain.entity.User;
import com.springcloud.user.domain.entity.UserBalance;
import com.springcloud.user.domain.repository.CoinRepository;
import com.springcloud.user.domain.repository.UserBalanceRepository;
import com.springcloud.user.domain.repository.UserRepository;
import com.springcloud.user.infrastructure.dto.KafkaOrderFormEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserBalanceCommandService {

    private final UserRepository userRepository;
    private final CoinRepository coinRepository;
    private final UserBalanceRepository userBalanceRepository;
    private final KafkaTemplate<String, KafkaOrderFormEvent> kafkaTemplate;

    // UserBalance 생성 로직 (User 객체 파라미터 필요)
    @Transactional
    public FindUserBalanceResult createBalance(CreateWalletCommand command, UUID userId) {
        // 0. 중복 검증 (응용 계층)
        validateDuplicateWallet(userId, command.getCoinId());
        // 1. 애그리거트 루트 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        // 2. Coin 엔티티 조회
        Coin coin = coinRepository.findById(command.getCoinId())
                .orElseThrow(() -> new IllegalArgumentException(String.valueOf(command.getCoinId())));
        //3. userBalance 객체 저장
        UserBalance savedBalance = userBalanceRepository.save(buildUserBalance(user, coin));

        return FindUserBalanceResult.from(savedBalance);
    }

    // 중복 검증 메서드 분리
    private void validateDuplicateWallet(UUID userId, UUID coinId) {
        if (userBalanceRepository.existsByUser_UserIdAndCoin_CoinId(userId, coinId)) {
            throw new IllegalArgumentException("이미 해당 코인 지갑이 존재합니다");
        }
    }

    private UserBalance buildUserBalance(User user, Coin coin) {
        return UserBalance.builder()
                .user(user)  // 외부에서 전달받은 User 사용
                .coin(coin)
                .wallet(UUID.randomUUID().toString().substring(0, 8))
                .totalBalance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .build();
    }

    // 자산 증가 로직(외부 거래소 -> 우리 거래소
    @Transactional
    public FindUserBalanceResult incrementBalance(UpdateIncrementBalanceCommand command) {
//        // 1. 애그리거트 루트로 유저밸런스 조회
//        User user = userRepository.findByWallet(command.getWallet())
//                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));
//
//        UserBalance balance = user.getBalanceByWallet(command.getWallet());

        //비관적 락 적용을 위해 애그리거트로 바로 진입
        // 1. 비관적 락으로 조회
        UserBalance balance = userBalanceRepository.findByWalletWithLock(command.getWallet())
                .orElseThrow(() -> new IllegalArgumentException(command.getWallet()));
        // 2. userbalance 잔액 증가
        balance.increase(command.getAmount());
        //Result에 함수 생성해서 코드 간결화
        //return new FindUserBalanceResult(balance.getBalanceId(),balance.getUser().getUserId(),balance.getCoin().getCoinId(),balance.getTotalBalance(),balance.getAvailableBalance(),balance.getWallet());
        return FindUserBalanceResult.from(balance);
    }

    //Kafka 에서 데이터 받아서 처리
    //자산 차감 로직
    @Transactional
    public void internalDecrementBalance(DecreaseBalanceCommand command) {
        log.info("Kafka 메시지 수신 및 자산 차감 로직 시작: {}", command);
        try {
            // 1. Symbol 분리 (BTC/KRW → ["BTC", "KRW"])
            String[] currencies = command.getTradingPair().split("/");

            if (currencies.length != 2) {
                throw new IllegalArgumentException("잘못된 심볼 형식:"+command.getTradingPair());
            }

            // 2. 주문 유형에 따라 확인할 화폐 결정
            String targetCoin = command.getOrderType().equals(OrderType.BUY)
                    ? currencies[1] // 매수 → KRW 확인
                    : currencies[0]; // 매도 → BTC 확인

            // 3. 필요 금액 계산
            BigDecimal requiredAmount = command.getOrderType().equals(OrderType.BUY)
                    ? command.getPrice()   // 매수 → 총 가격
                    : command.getQuantity(); // 매도 → 수량

            User user = userRepository.findById(command.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다: "+ command.getUserId()));
            //비관적 락 적용
            UserBalance balance = userBalanceRepository.findByUserAndCoinSymbolForUpdate(user, targetCoin)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자산입니다: " + targetCoin));

            //자산의 사용 가능 금액
            BigDecimal availableBalance = balance.getAvailableBalance();

            // 6. 잔액 검증 및 잔액 차감(도메인에서)
            balance.decrease(requiredAmount);
            log.error("잔액 차감 완료");
            kafkaTemplate.send("user-to-matching.execute-order-delivery",KafkaOrderFormEvent.fromEvent(command));
            log.error("매칭서버로 주문서 전달 완료");
        } catch (Exception e) {
            log.error("자산 차감 중 문제 발생 유저에게 문제 사항 전송",e);
            //소켓 서버로 문제 보내기
            throw new RuntimeException();
        }

    }


    @Transactional
    public void internalIncrementBalance(IncreaseBalanceCommand command) {

        try {
            String[] parts = command.getTradingPair().split("/");

            if (parts.length != 2) {
                throw new IllegalArgumentException("잘못된 트레이딩 페어 형식: " + command.getTradingPair());
            }

            UserBalance buyerBalance = userBalanceRepository
                    .findUserBalanceWithUserAndCoin(command.getBuyer(), parts[0])
                    .orElseThrow(() -> new IllegalArgumentException(command.getSeller().toString() + "는 존재하지 않는 자산"));

            UserBalance sellerBalance = userBalanceRepository
                    .findUserBalanceWithUserAndCoin(command.getSeller(), parts[1])
                    .orElseThrow(() -> new IllegalArgumentException(command.getSeller().toString() + "는 존재하지 않는 자산"));

            buyerBalance.increase(command.getQuantity());

            //이건 확인 필요
            sellerBalance.increase(command.getQuantity().multiply(command.getPrice()));
        } catch (Exception e) {
            log.error("자산 증가 중 문제 발생 유저에게 문제 사항 전송",e);
            //소켓 서버로 문제 보내기
            throw new RuntimeException();
        }
    }
}
