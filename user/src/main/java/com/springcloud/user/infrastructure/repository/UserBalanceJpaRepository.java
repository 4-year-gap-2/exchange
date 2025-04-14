package com.springcloud.user.infrastructure.repository;

import com.springcloud.user.domain.entity.UserBalance;
import com.springcloud.user.domain.repository.UserBalanceRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserBalanceJpaRepository extends JpaRepository<UserBalance, UUID>, UserBalanceRepository {
}
