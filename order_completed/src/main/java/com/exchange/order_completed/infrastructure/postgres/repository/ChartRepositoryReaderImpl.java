package com.exchange.order_completed.infrastructure.postgres.repository;

import com.exchange.order_completed.domain.postgres.entity.TradeDataInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class ChartRepositoryReaderImpl implements ChartRepositoryReader{

    private final JdbcTemplate jdbcTemplate;

    public List<TradeDataInfo> searchDataFromView(String viewName, String timeColumnName) {

        String sql = "SELECT " + timeColumnName + ", pair, first_price, last_price, max_price, min_price, amount FROM " + viewName;

        RowMapper<TradeDataInfo> rowMapper = (rs, rowNum) -> {
            TradeDataInfo tradeData = new TradeDataInfo();
            tradeData.setMinute(rs.getObject(timeColumnName, LocalDateTime.class));
            tradeData.setPair(rs.getString("pair"));
            tradeData.setFirstPrice(rs.getBigDecimal("first_price"));
            tradeData.setLastPrice(rs.getBigDecimal("last_price"));
            tradeData.setMaxPrice(rs.getBigDecimal("max_price"));
            tradeData.setMinPrice(rs.getBigDecimal("min_price"));
            tradeData.setAmount(rs.getBigDecimal("amount"));
            return tradeData;
        };

        return jdbcTemplate.query(sql, rowMapper);
    }
}