package com.springcloud.user.application.query;

import com.springcloud.user.application.result.FindUserBalanceResult;
import com.springcloud.user.common.exception.CustomNotFoundException;
import com.springcloud.user.domain.entity.User;
import com.springcloud.user.domain.entity.UserBalance;
import com.springcloud.user.domain.repository.UserBalanceRepository;
import com.springcloud.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserBalanceQueryService {
    private final UserBalanceRepository userBalanceRepository;
    private final UserRepository userRepository;

    //자산 조회
    public Page<FindUserBalanceResult> findBalance(UUID userId, int page, int size) {
        // 1. User(애그리거트 루트) 먼저 조회 (존재하지 않으면 에러)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomNotFoundException("해당 유저는 존재하지 않습니다."));
        // 2. User 엔티티를 기준으로 페이징 쿼리 실행
        Pageable pageable = PageRequest.of(page, size, Sort.by("availableBalance").descending());
        Page<UserBalance> balancePage = userBalanceRepository.findByUser(user, pageable);

        // 3. 페이징 처리 반환(Page의 map 메서드 활용)
        return balancePage.map(FindUserBalanceResult::from);
    }
//    // 이 부분 고민 : 코인 레파지토리를 주입받는데 이 부분이 유저파사트로 가야하나?
//    public CheckBalanceResult checkAvailableBalance(CheckAvailableBalanceQuery query) {
//        // 1. Symbol 분리 (BTC/KRW → ["BTC", "KRW"])
//        String[] currencies = query.getSymbol().split("/");
//        if (currencies.length != 2) {
//            return new CheckBalanceResult(false, "유효하지 않은 심볼 형식: " + query.getSymbol());
//        }
//
//        // 2. 주문 유형에 따라 확인할 화폐 결정
//        String targetCoin = query.getOrderType().equalsIgnoreCase("BUY")
//                ? currencies[1] // 매수 → KRW 확인
//                : currencies[0]; // 매도 → BTC 확인
//
//        // 2-1. 코인 ID 조회 (Coin 엔티티에서)
//        Coin targetCurrency = coinRepository.findBySymbol(targetCoin)
//                .orElseThrow(() -> new IllegalArgumentException("코인을 찾을 수 없습니다: " + targetCoin));
//
//        // 3. 필요 금액 계산
//        BigDecimal requiredAmount = query.getOrderType().equalsIgnoreCase("BUY")
//                ? query.getPrice()   // 매수 → 총 가격
//                : query.getAmount(); // 매도 → 수량
//
//        // Good: User 애그리거트 루트를 통해 밸런스 조회
//        User user = userRepository.findById(query.getUserId())
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다: "+query.getUserId()));
//
//        UserBalance balance = user.getBalance(targetCurrency.getCoinId()); // 애그리거트 내부 접근
//
//        BigDecimal availableBalance = balance.getAvailableBalance();
//
//        // 6. 잔액 검증 및 결과 반환
//        if (availableBalance.compareTo(requiredAmount) >= 0) {
//            return new CheckBalanceResult(true,"거래 가능, 거래 후 잔고 : "+availableBalance.subtract(requiredAmount));
//        } else {
//            return new CheckBalanceResult(false, String.format( // 포맷 문자열 추가
//                    "[%s] 잔액 부족 (보유: %s, 필요: %s)",
//                    targetCurrency,
//                    availableBalance.stripTrailingZeros().toPlainString(),
//                    requiredAmount.stripTrailingZeros().toPlainString()
//            ));
//
//        }
//    }
}
