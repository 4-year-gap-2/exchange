package com.springcloud.user.application.command;

import com.springcloud.user.application.command.UpdateIncrementBalanceCommand;
import com.springcloud.user.application.command.UserBalanceCommandService;
import com.springcloud.user.domain.entity.Coin;
import com.springcloud.user.domain.entity.User;
import com.springcloud.user.domain.entity.UserBalance;
import com.springcloud.user.domain.repository.CoinRepository;
import com.springcloud.user.domain.repository.UserBalanceRepository;
import com.springcloud.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional // 테스트 종료 후 자동 롤백
class UserBalanceCommandServiceTest {

    @Autowired
    private UserBalanceCommandService balanceCommandService;
    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoinRepository coinRepository;

    private final String testWallet = "test-wallet-123"; // 테스트용 지갑 주소

    @BeforeEach
    void setup() {
        // 최소한의 데이터 생성 (필수 필드만 초기화)
        User user = User.builder()
                .email("test@example.com")
                .password("password")
                .username("test-user")
                .bankAccountNumber("123-456-789") // 👈 추가
                .build();

        Coin coin = new Coin();
        coin.setSymbol("TEST-COIN");
        Coin savedCoin = coinRepository.save(coin);

        userBalanceRepository.save(UserBalance.builder()
                .user(user)
                .coin(savedCoin)
                .wallet(testWallet)
                .totalBalance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .build());
    }

    @Test
    @DisplayName("동시 10개 요청 시 잔고 증가 테스트")
    void testConcurrentIncrement() throws InterruptedException {
        // Given
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When: 10개 쓰레드에서 동시에 +1 요청
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    balanceCommandService.incrementBalance(
                            new UpdateIncrementBalanceCommand(testWallet, BigDecimal.ONE)
                    );
                } catch (Exception e) {
                    fail("테스트 실패: " + e.getMessage());
                }
            });
        }

        // Then
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        UserBalance result = userBalanceRepository.findByWalletWithLock(testWallet).get();
        assertThat(result.getTotalBalance()).isEqualByComparingTo("10");
    }
}
