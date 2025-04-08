package com.exchange.matching.infrastructure.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MatchingRepositoryStoreImpl {
    private final JPAQueryFactory queryFactory;
}
