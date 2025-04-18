package com.springcloud.user.application.command;

import com.springcloud.user.application.result.FindUserBalanceResult;
import com.springcloud.user.domain.entity.Coin;
import com.springcloud.user.domain.entity.User;
import com.springcloud.user.domain.entity.UserBalance;
import com.springcloud.user.domain.repository.CoinRepository;
import com.springcloud.user.domain.repository.UserBalanceRepository;
import com.springcloud.user.domain.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserBalanceCommandService {

    private final UserRepository userRepository;
    private final CoinRepository coinRepository;
    private final UserBalanceRepository userBalanceRepository;

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
