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
}
