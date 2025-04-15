package com.springcloud.user.infrastructure.repository;

import com.springcloud.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    Optional<User> findById(UUID userId);

//    @Query("SELECT u FROM User u " +
//            "JOIN FETCH u.balances b " +  // 페치 조인으로 N+1 문제 해결
//            "WHERE b.wallet = :wallet")
//    Optional<User> findByBalanceWallet(@Param("wallet") String wallet);

}
