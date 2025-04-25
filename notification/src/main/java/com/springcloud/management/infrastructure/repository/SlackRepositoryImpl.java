package com.springcloud.management.infrastructure.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SlackRepositoryImpl {
    private final JPAQueryFactory queryFactory;
}
