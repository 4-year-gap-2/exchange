package com.springcloud.management.infrastructure.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.springcloud.management.domain.entity.QSlack;
import com.springcloud.management.domain.entity.Slack;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SlackRepositoryImpl implements SlackRepositoryCustom {
    private final JPAQueryFactory queryFactory;

}
